package com.ollacercana.service.impl;

import com.ollacercana.exception.ConflictoVersionException;
import com.ollacercana.exception.PlatoNoEncontradoException;
import com.ollacercana.mapper.PlatoEntityMapper;
import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.model.dto.response.PlatoCercanoResponseDTO;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.service.PlatoService;
import com.ollacercana.util.GeoUtils;
import com.ollacercana.validator.PlatoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository repository;
    private final PlatoEntityMapper entityMapper;
    private final PlatoValidator validator;
    private final PerfilCocineraRepository perfilCocineraRepository;

    @Override
    public Plato crear(Plato plato) {
        validator.validarParaPublicar(plato);
        plato.publicar();

        if (plato.getId() == null) {
            plato.setId(UUID.randomUUID());
        }

        Plato entidad = entityMapper.toEntity(plato);
        Plato guardado = repository.save(entidad);
        return entityMapper.toDomain(guardado);
    }

    @Override
    public Plato obtenerPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new PlatoNoEncontradoException(id));
    }

    @Override
    public Plato ajustarDisponibilidad(UUID platoId, AjusteDisponibilidadRequest request) {
        Plato plato = repository.findById(platoId)
                .orElseThrow(() -> new PlatoNoEncontradoException(platoId));

        validator.validarAjusteDisponibilidad(plato, request);

        plato.ajustarDisponibilidad(request.tipo(), request.cantidad());
        plato.setVersion(request.version());

        try {
            return repository.saveAndFlush(plato);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConflictoVersionException(
                    plato.getVersion(),
                    plato.getPorcionesTotales(),
                    plato.getPorcionesComprometidas(),
                    plato.getEstado().name()
            );
        }
    }

    @Override
    public void eliminar(UUID id) {
        if (!repository.existsById(id)) {
            throw new PlatoNoEncontradoException(id);
        }
        repository.deleteById(id);
    }

    @Override
    public List<PlatoCercanoResponseDTO> buscarCercanos(Double latitudCliente, Double longitudCliente) {
        // Consulta platos ACTIVOS, no expirados y con porciones disponibles > 0
        List<Plato> platosActivos = repository.findActivosVigentes(EstadoPlato.ACTIVO, LocalDateTime.now());

        return platosActivos.stream()
                .map(plato -> {
                    String conjunto = perfilCocineraRepository.findById(plato.getCocineraId())
                            .map(PerfilCocinera::getConjuntoResidencial)
                            .orElse("Conjunto Residencial");

                    Integer distanciaRedondeada = null;
                    if (latitudCliente != null && longitudCliente != null && plato.getLatitud() != null && plato.getLongitud() != null) {
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
                // Si el cliente envió coordenadas, ordenamos por cercanía ascendente
                .sorted(Comparator.comparing(
                        PlatoCercanoResponseDTO::getDistanciaAproximada,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .toList();
    }
}