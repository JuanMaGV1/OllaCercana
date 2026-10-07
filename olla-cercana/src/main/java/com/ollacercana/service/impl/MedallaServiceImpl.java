package com.ollacercana.service.impl;

import com.ollacercana.domain.CodigoMedalla;
import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.Medalla;
import com.ollacercana.domain.MedallaUsuario;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.dto.response.MedallaUsuarioResponseDTO;
import com.ollacercana.exception.CuentaNoEncontradaException;
import com.ollacercana.repository.BalanceConjuntoProjection;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.MedallaRepository;
import com.ollacercana.repository.MedallaUsuarioRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReservaRepository;
import com.ollacercana.service.MedallaService;
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

    /**
     * noRollbackFor: este método se ejecuta dentro de la transacción que completa la reserva (observer);
     * un fallo al otorgar la insignia nunca debe deshacer el cierre de la transacción.
     */
    @Override
    @Transactional(noRollbackFor = RuntimeException.class)
    public Optional<MedallaUsuario> evaluarVecinoFiel(Long compradorId, UUID cocineraId, LocalDateTime fechaCompletada) {
        if (compradorId == null || cocineraId == null || fechaCompletada == null) {
            return Optional.empty();
        }
        // La insignia se otorga una sola vez.
        if (medallaUsuarioRepository.existsByUsuarioIdAndMedallaCodigo(compradorId, CodigoMedalla.VECINO_FIEL)) {
            return Optional.empty();
        }

        // El conteo es por mes calendario: al iniciar un nuevo mes el rango cambia y la racha vuelve a cero.
        LocalDateTime inicioMes = fechaCompletada.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime inicioMesSiguiente = inicioMes.plusMonths(1);

        long entregas = reservaRepository.contarCompletadasEnPeriodo(
                compradorId, cocineraId, EstadoReserva.COMPLETADA, inicioMes, inicioMesSiguiente);
        if (entregas < ENTREGAS_PARA_VECINO_FIEL) {
            return Optional.empty();
        }

        Medalla medalla = buscarMedalla(CodigoMedalla.VECINO_FIEL);
        MedallaUsuario otorgada = medallaUsuarioRepository.save(MedallaUsuario.builder()
                .usuarioId(compradorId)
                .medalla(medalla)
                .fechaOtorgada(fechaCompletada)
                .vigenteHasta(null)
                .build());
        log.info("Insignia VECINO_FIEL otorgada al comprador {} por {} entregas del mes con la cocinera {}",
                compradorId, entregas, cocineraId);
        return Optional.of(otorgada);
    }

    @Override
    @Transactional
    public List<String> calcularBalanceSemanal(LocalDateTime ahora) {
        LocalDateTime desde = ahora.minusDays(DIAS_BALANCE);
        List<String> premiados = new ArrayList<>();

        for (BalanceConjuntoProjection balance : platoRepository.balancePorConjunto(desde, ahora)) {
            long publicadas = balance.getPublicadas() == null ? 0 : balance.getPublicadas();
            long vendidas = balance.getVendidas() == null ? 0 : balance.getVendidas();

            // Solo cuenta si hubo publicaciones y se vendió el 100%.
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

        for (PerfilCocinera perfil : perfilCocineraRepository.findByConjuntoResidencial(conjunto)) {
            if (perfil.getCuenta() == null) {
                continue;
            }
            Long cuentaId = perfil.getCuenta().getId();
            // Si ya la tenía (semana anterior), se renueva en lugar de duplicarla.
            MedallaUsuario existente = medallaUsuarioRepository
                    .findByUsuarioIdAndMedallaCodigo(cuentaId, CodigoMedalla.CONJUNTO_OLLA_VERDE)
                    .orElse(null);
            if (existente != null) {
                existente.setFechaOtorgada(ahora);
                existente.setVigenteHasta(vence);
                medallaUsuarioRepository.save(existente);
            } else {
                medallaUsuarioRepository.save(MedallaUsuario.builder()
                        .usuarioId(cuentaId)
                        .medalla(medalla)
                        .fechaOtorgada(ahora)
                        .vigenteHasta(vence)
                        .build());
            }
        }
    }

    private Medalla buscarMedalla(CodigoMedalla codigo) {
        return medallaRepository.findById(codigo)
                .orElseThrow(() -> new IllegalStateException("La medalla " + codigo + " no existe en el catálogo"));
    }
}
