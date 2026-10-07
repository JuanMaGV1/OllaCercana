package com.ollacercana.core.services.impl;

import com.ollacercana.controller.dtos.response.MedallaUsuarioResponseDTO;
import com.ollacercana.controller.handlers.exception.CuentaNoEncontradaException;
import com.ollacercana.core.models.Medalla;
import com.ollacercana.core.models.MedallaUsuario;
import com.ollacercana.core.models.enums.CodigoMedalla;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.services.MedallaService;
import com.ollacercana.persistence.entities.MedallaUsuarioEntity;
import com.ollacercana.persistence.mappers.MedallaEntityMapper;
import com.ollacercana.persistence.mappers.MedallaUsuarioEntityMapper;
import com.ollacercana.persistence.repository.BalanceConjuntoProjection;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.MedallaRepository;
import com.ollacercana.persistence.repository.MedallaUsuarioRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedallaServiceImpl implements MedallaService {

    private static final Logger log = LoggerFactory.getLogger(MedallaServiceImpl.class);

    public static final int ENTREGAS_PARA_VECINO_FIEL = 3;
    public static final int DIAS_VIGENCIA_OLLA_VERDE = 7;
    public static final int DIAS_BALANCE = 7;

    private final ReservaRepository reservaRepository;
    private final PlatoRepository platoRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;
    private final CuentaRepository cuentaRepository;
    private final MedallaRepository medallaRepository;
    private final MedallaUsuarioRepository medallaUsuarioRepository;
    private final MedallaEntityMapper medallaMapper;
    private final MedallaUsuarioEntityMapper medallaUsuarioMapper;

    @Override
    @Transactional(noRollbackFor = RuntimeException.class)
    public Optional<MedallaUsuario> evaluarVecinoFiel(Long compradorId, UUID cocineraId, LocalDateTime fechaCompletada) {
        if (compradorId == null || cocineraId == null || fechaCompletada == null) {
            return Optional.empty();
        }
        if (medallaUsuarioRepository.existsByUsuarioIdAndMedallaCodigo(compradorId, CodigoMedalla.VECINO_FIEL)) {
            return Optional.empty();
        }

        LocalDateTime inicioMes = fechaCompletada.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime inicioMesSiguiente = inicioMes.plusMonths(1);

        long entregas = reservaRepository.contarCompletadasEnPeriodo(
                compradorId, cocineraId, EstadoReserva.COMPLETADA, inicioMes, inicioMesSiguiente);
        if (entregas < ENTREGAS_PARA_VECINO_FIEL) {
            return Optional.empty();
        }

        Medalla medalla = buscarMedalla(CodigoMedalla.VECINO_FIEL);
        MedallaUsuarioEntity otorgadaEntity = medallaUsuarioRepository.save(
                medallaUsuarioMapper.toEntity(MedallaUsuario.builder()
                        .usuarioId(compradorId)
                        .medalla(medalla)
                        .fechaOtorgada(fechaCompletada)
                        .vigenteHasta(null)
                        .build()));
        log.info("Insignia VECINO_FIEL otorgada al comprador {} por {} entregas del mes con la cocinera {}",
                compradorId, entregas, cocineraId);
        return Optional.of(medallaUsuarioMapper.toDomain(otorgadaEntity));
    }

    @Override
    @Transactional
    public List<String> calcularBalanceSemanal(LocalDateTime ahora) {
        LocalDateTime desde = ahora.minusDays(DIAS_BALANCE);
        List<String> premiados = new ArrayList<>();

        for (BalanceConjuntoProjection balance : platoRepository.balancePorConjunto(desde, ahora)) {
            long publicadas = balance.getPublicadas() == null ? 0 : balance.getPublicadas();
            long vendidas = balance.getVendidas() == null ? 0 : balance.getVendidas();

            if (publicadas > 0 && vendidas >= publicadas) {
                otorgarOllaVerde(balance.getConjunto(), ahora);
                premiados.add(balance.getConjunto());
            }
        }
        log.info("Balance semanal: {} conjunto(s) con CONJUNTO_OLLA_VERDE", premiados.size());
        return premiados;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedallaUsuarioResponseDTO> listarVigentes(Long usuarioId, LocalDateTime ahora) {
        if (!cuentaRepository.existsById(usuarioId)) {
            throw new CuentaNoEncontradaException(usuarioId);
        }
        return medallaUsuarioRepository.findVigentes(usuarioId, ahora).stream()
                .map(m -> MedallaUsuarioResponseDTO.builder()
                        .codigo(m.getMedalla().getCodigo().name())
                        .nombre(m.getMedalla().getNombre())
                        .requisito(m.getMedalla().getRequisito())
                        .fechaOtorgada(m.getFechaOtorgada())
                        .vigenteHasta(m.getVigenteHasta())
                        .build())
                .toList();
    }

    private void otorgarOllaVerde(String conjunto, LocalDateTime ahora) {
        Medalla medalla = buscarMedalla(CodigoMedalla.CONJUNTO_OLLA_VERDE);
        LocalDateTime vence = ahora.plusDays(DIAS_VIGENCIA_OLLA_VERDE);

            for (com.ollacercana.persistence.entities.PerfilCocineraEntity perfil
                    : perfilCocineraRepository.findByConjuntoResidencial(conjunto)) {
                if (perfil.getCuenta() == null) continue;
                Long cuentaId = perfil.getCuenta().getId();

                MedallaUsuarioEntity existente = medallaUsuarioRepository
                        .findByUsuarioIdAndMedallaCodigo(cuentaId, CodigoMedalla.CONJUNTO_OLLA_VERDE)
                        .orElse(null);

                if (existente != null) {
                    existente.setFechaOtorgada(ahora);
                    existente.setVigenteHasta(vence);
                    medallaUsuarioRepository.save(existente);
                } else {
                    medallaUsuarioRepository.save(medallaUsuarioMapper.toEntity(MedallaUsuario.builder()
                            .usuarioId(cuentaId)
                            .medalla(medalla)
                            .fechaOtorgada(ahora)
                            .vigenteHasta(vence)
                            .build()));
                }
            }
    }
    private Medalla buscarMedalla(CodigoMedalla codigo) {
        return medallaRepository.findById(codigo)
                .map(medallaMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("La medalla " + codigo + " no existe en el catálogo"));
    }
}
