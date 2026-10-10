package com.ollacercana.core.services.impl;

import com.ollacercana.controller.dtos.request.ConsultaPlatosRequest;
import com.ollacercana.controller.dtos.response.PaginaResponseDTO;
import com.ollacercana.controller.dtos.response.PlatoCercanoResponseDTO;
import com.ollacercana.controller.handlers.exception.ConflictoVersionException;
import com.ollacercana.controller.handlers.exception.PlatoNoEncontradoException;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.core.models.enums.TipoAjustePorciones;
import com.ollacercana.core.patterns.filter.FiltroCompuestoPlato;
import com.ollacercana.core.patterns.filter.FiltroDistanciaMaxima;
import com.ollacercana.core.patterns.filter.FiltroRestricciones;
import com.ollacercana.core.patterns.iterator.CatalogoIterator;
import com.ollacercana.core.patterns.observer.PublicadorEventosPorciones;
import com.ollacercana.core.services.PlatoService;
import com.ollacercana.core.util.GeoUtils;
import com.ollacercana.core.validators.PlatoValidator;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository repository;
    private final PlatoValidator validator;
    private final PlatoEntityMapper entityMapper;
    private PublicadorEventosPorciones publicadorEventosPorciones;
    private final PerfilCocineraRepository perfilCocineraRepository;

    @Autowired(required = false)
    public void setPublicadorEventosPorciones(PublicadorEventosPorciones publicadorEventosPorciones) {
        this.publicadorEventosPorciones = publicadorEventosPorciones;
    }

    /**
     * HU-04 · RN-02 · RN-28
     * Publica un plato calculando expiración a 4h.
     *
     * @see OC-92  PlatoService.crear() con fecha expiración +4h
     * @see OC-99  Validador con rangos RN-27 (precio, porciones)
     * @see OC-91  Validador: cocinera verificada, no pausada, máx 3 activos
     */
    @Override
    @Transactional
    public Plato crear(Plato plato) {
        log.info("Publicando nuevo plato: nombre='{}', cocineraId={}", plato.getNombre(), plato.getCocineraId());
        validator.validarParaPublicar(plato);
        plato.publicar();
        if (plato.getId() == null) plato.setId(UUID.randomUUID());

        PlatoEntity guardado = repository.save(entityMapper.toEntity(plato));
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
    @Transactional(readOnly = true)
    public List<MedioPago> obtenerMediosPago(UUID platoId) {
        Plato plato = obtenerPorId(platoId);
        if (plato.getCocineraId() == null) return List.of();
        return perfilCocineraRepository.findById(plato.getCocineraId())
                .map(PerfilCocineraEntity::getMediosPago)
                .filter(medios -> medios != null)
                .orElse(List.of());
    }

    /**
     * HU-24 · RN-27
     * Ajusta disponibilidad con control optimista de versión.
     *
     * @see OC-104 DTO AjusteDisponibilidadRequest
     * @see OC-105 @Version en PlatoEntity
     * @see OC-106 MÃ©todo ajustarDisponibilidad
     * @see OC-107 Control de versión optimista
     * @see OC-108 Validación "no menor que comprometidas"
     * @see OC-109 Endpoint PATCH /disponibilidad
     */
    @Override
    @Transactional
    public Plato ajustarDisponibilidad(UUID platoId, TipoAjustePorciones tipo, Integer cantidad, Integer version) {
        Plato plato = obtenerPorId(platoId);
        validator.validarAjusteDisponibilidad(plato, tipo, cantidad);

        int disponiblesAntes = plato.getPorcionesDisponibles();
        plato.ajustarDisponibilidad(tipo, cantidad);
        plato.setVersion(version);

        try {
            PlatoEntity guardado = repository.saveAndFlush(entityMapper.toEntity(plato));
            if (publicadorEventosPorciones != null) {
                publicadorEventosPorciones.publicarSiCambio(disponiblesAntes, entityMapper.toDomain(guardado));
            }
            return entityMapper.toDomain(guardado);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConflictoVersionException(plato.getVersion(), plato.getPorcionesTotales(),
                    plato.getPorcionesComprometidas(), plato.getEstado().name());
        }
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        if (!repository.existsById(id)) throw new PlatoNoEncontradoException(id);
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plato> buscarCercanos(Double latitudCliente, Double longitudCliente) {
        List<Plato> activos = repository.findActivosVigentes(EstadoPlato.ACTIVO, LocalDateTime.now())
                .stream().map(entityMapper::toDomain).toList();

        FiltroCompuestoPlato filtro = new FiltroCompuestoPlato();
        if (latitudCliente != null && longitudCliente != null) {
            filtro.agregar(new FiltroDistanciaMaxima(latitudCliente, longitudCliente, 2000.0));
        }
        CatalogoIterator it = new CatalogoIterator(activos, filtro);
        List<Plato> resultado = new ArrayList<>();
        while (it.hasNext()) resultado.add(it.next());
        return resultado;
    }

    /**
     * HU-06 + HU-07 · RN-05
     * Consulta paginada de platos cercanos con filtros y distancia redondeada.
     *
     * NO expone {@code puntoEntrega} — RN-05.
     * Distancia redondeada a múltiplos de 100m con {@link GeoUtils}.
     *
     * @see OC-115 Query Haversine
     * @see OC-116 consultarCercanos() en el Service
     * @see OC-117 Endpoint GET /api/v1/platos/cercanos
     * @see OC-123 Nunca exponer dirección exacta
     * @see OC-127 Filtro por tipoComida
     * @see OC-128 Filtro por restricciones
     */
@Override
@Transactional(readOnly = true)
public PaginaResponseDTO<PlatoCercanoResponseDTO> consultarCercanos(ConsultaPlatosRequest request) {

    int radio = normalizarRadio(request.getRadioMetros());

    Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
    Page<Object[]> pagina = repository.buscarCercanosConDistancia(
            request.getLat(),
            request.getLng(),
            radio,
            request.getTipoComida(),
            EstadoPlato.ACTIVO,
            pageable
    );

    List<Plato> dominio = pagina.getContent().stream()
            .map(row -> (PlatoEntity) row[0])
            .map(entityMapper::toDomain)
            .toList();

    // HU-07: aplicar filtros Composite (tipoComida ya viene filtrado desde SQL)
    FiltroCompuestoPlato filtros = new FiltroCompuestoPlato();
    if (request.getRestricciones() != null && !request.getRestricciones().isEmpty()) {
        filtros.agregar(new FiltroRestricciones(request.getRestricciones()));
    }

    List<PlatoCercanoResponseDTO> dtos = dominio.stream()
            .filter(filtros::cumple)
            .map(p -> mapearACercano(p, request.getLat(), request.getLng()))
            .sorted(Comparator.comparing(
                    PlatoCercanoResponseDTO::getDistanciaAproximada,
                    Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

    return PaginaResponseDTO.<PlatoCercanoResponseDTO>builder()
            .contenido(dtos)
            .page(pagina.getNumber())
            .size(pagina.getSize())
            .totalElementos(pagina.getTotalElements())
            .totalPaginas(pagina.getTotalPages())
            .hayMas(pagina.hasNext())
            .build();
}

private int normalizarRadio(Integer radio) {
    if (radio == null) return 2000;
    if (radio < 500) return 500;
    if (radio > 2000) return 2000;
    return radio;
}

private PlatoCercanoResponseDTO mapearACercano(Plato plato, Double lat, Double lng) {
    Integer distanciaAproximada = null;

    if (lat != null && lng != null
            && plato.getLatitud() != null && plato.getLongitud() != null) {
        double distancia = GeoUtils.calcularDistanciaEnMetros(
                lat, lng, plato.getLatitud(), plato.getLongitud());
        distanciaAproximada = GeoUtils.redondearDistanciaMultiplo100(distancia);
    }

    String conjunto = perfilCocineraRepository.findById(plato.getCocineraId())
            .map(PerfilCocineraEntity::getConjuntoResidencial)
            .orElse("Conjunto no especificado");

    return PlatoCercanoResponseDTO.builder()
            .id(plato.getId())
            .nombre(plato.getNombre())
            .fotoUrl(plato.getFotoUrl())
            .tipoComida(plato.getTipoComida())
            .restricciones(plato.getRestricciones())
            .precioPorcion(plato.getPrecioPorcion())
            .porcionesDisponibles(plato.getPorcionesDisponibles())
            .conjunto(conjunto)   // ← ahora sí viene del perfil
            .distanciaAproximada(distanciaAproximada)
            .tiempoRestante(GeoUtils.formatearTiempoRestante(plato.getFechaExpiracion()))
            .build();
}

@Override
@Transactional(readOnly = true)
public String obtenerConjuntoDePlato(UUID platoId) {
    Plato plato = obtenerPorId(platoId);
    if (plato.getCocineraId() == null) return "Conjunto no especificado";
    return perfilCocineraRepository.findById(plato.getCocineraId())
            .map(PerfilCocineraEntity::getConjuntoResidencial)
            .orElse("Conjunto no especificado");
}
}