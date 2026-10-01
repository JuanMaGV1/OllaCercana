package com.ollacercana.service.impl;

import com.ollacercana.exception.PlatoNoEncontradoException;
import com.ollacercana.mapper.PerfilCocineraEntityMapper;
import com.ollacercana.mapper.PlatoEntityMapper;
import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.model.dto.response.PlatoCercanoResponseDTO;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import com.ollacercana.persistence.entity.PlatoEntity;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.service.PlatoService;
import com.ollacercana.utils.GeoUtils;
import com.ollacercana.validator.PlatoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository repository;
    private final PlatoEntityMapper entityMapper;
    private final PerfilCocineraEntityMapper perfilMapper;
    private final PlatoValidator validator;
    private final PerfilCocineraRepository perfilCocineraRepository;

    @Override
    @Transactional
    public Plato crear(Plato plato) {
        validator.validarParaPublicar(plato);
        plato.publicar();

        PlatoEntity entity = entityMapper.toEntity(plato);
        PlatoEntity guardado = repository.save(entity);
        return entityMapper.toDomain(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Plato obtenerPorId(UUID id) {
        return repository.findById(id)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> new PlatoNoEncontradoException(id));
    }

    @Override
    @Transactional
    public Plato ajustarDisponibilidad(UUID platoId, AjusteDisponibilidadRequest request) {
        PlatoEntity entity = repository.findById(platoId)
                .orElseThrow(() -> new PlatoNoEncontradoException(platoId));

        Plato plato = entityMapper.toDomain(entity);
        validator.validarAjusteDisponibilidad(plato, request);
        plato.ajustarDisponibilidad(request.tipo(), request.cantidad());

        PlatoEntity actualizado = entityMapper.toEntity(plato);
        PlatoEntity guardado = repository.saveAndFlush(actualizado);
        return entityMapper.toDomain(guardado);
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        if (!repository.existsById(id)) {
            throw new PlatoNoEncontradoException(id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatoCercanoResponseDTO> buscarCercanos(Double latitudCliente, Double longitudCliente) {
        List<PlatoEntity> platosActivos = repository.findActivosVigentes(EstadoPlato.ACTIVO, LocalDateTime.now());

        return platosActivos.stream()
                .map(entityMapper::toDomain)
                .map(plato -> {
                    String conjunto = perfilCocineraRepository.findById(plato.getCocineraId())
                            .map(perfilMapper::toDomain)
                            .map(PerfilCocinera::getConjuntoResidencial)
                            .orElse("Conjunto Residencial");

                    Integer distanciaRedondeada = null;
                    if (latitudCliente != null && longitudCliente != null
                            && plato.getLatitud() != null && plato.getLongitud() != null) {
                        double distanciaMetros = GeoUtils.calcularDistanciaEnMetros(
                                latitudCliente, longitudCliente,
                                plato.getLatitud(), plato.getLongitud()
                        );
                        distanciaRedondeada = GeoUtils.redondearDistanciaMultiplo100(distanciaMetros);
                    }

                    return PlatoCercanoResponseDTO.builder()
                            .id(plato.getId())
                            .nombre(plato.getNombre())
                            .fotoUrl(plato.getFotoUrl())
                            .tipoComida(plato.getTipoComida())
                            .restricciones(plato.getRestricciones())
                            .precioPorcion(plato.getPrecioPorcion())
                            .porcionesDisponibles(plato.getPorcionesDisponibles())
                            .conjunto(conjunto)
                            .distanciaAproximada(distanciaRedondeada)
                            .tiempoRestante(GeoUtils.formatearTiempoRestante(plato.getFechaExpiracion()))
                            .build();
                })
                .sorted(Comparator.comparing(
                        PlatoCercanoResponseDTO::getDistanciaAproximada,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .toList();
    }
}