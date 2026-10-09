package com.ollacercana.core.services.impl;

import com.ollacercana.controller.dtos.request.CalificacionRequestDTO;
import com.ollacercana.controller.dtos.response.CalificacionResponseDTO;
import com.ollacercana.controller.dtos.response.PaginaResponseDTO;
import com.ollacercana.controller.dtos.response.ResumenCalificacionesDTO;
import com.ollacercana.controller.handlers.exception.AccesoDenegadoCalificacionException;
import com.ollacercana.controller.handlers.exception.CalificacionDuplicadaException;
import com.ollacercana.controller.handlers.exception.ReservaNoCompletadaException;
import com.ollacercana.controller.handlers.exception.ReservaNoEncontradaException;
import com.ollacercana.controller.mappers.CalificacionMapper;
import com.ollacercana.core.models.Calificacion;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.models.enums.EstadoCalificacion;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.patterns.moderacion.EvaluadorReputacionCalificacion;
import com.ollacercana.core.services.CalificacionService;
import com.ollacercana.core.validators.CalificacionValidator;
import com.ollacercana.persistence.entities.CalificacionEntity;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.mappers.CalificacionEntityMapper;
import com.ollacercana.persistence.mappers.ReservaEntityMapper;
import com.ollacercana.persistence.repository.CalificacionRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalificacionServiceImpl implements CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final ReservaRepository reservaRepository;
    private final PerfilCocineraRepository perfilRepository;
    private final CalificacionEntityMapper calificacionEntityMapper;
    private final ReservaEntityMapper reservaEntityMapper;
    private final CalificacionMapper calificacionMapper;
    private final CalificacionValidator calificacionValidator;
    private final EvaluadorReputacionCalificacion evaluadorReputacion;

    @Override
    @Transactional
    public CalificacionResponseDTO calificar(UUID reservaId, Long compradorId, CalificacionRequestDTO request) {
        log.info("HU-31: comprador {} calificando reserva {}", compradorId, reservaId);
        // 1. La reserva debe existir
        Reserva reserva = reservaRepository.findById(reservaId)
                .map(reservaEntityMapper::toDomain)
                .orElseThrow(() -> new ReservaNoEncontradaException(reservaId));

        // 2. RN-31.1: solo el comprador de la reserva puede calificar
        if (!reserva.getCompradorId().equals(compradorId)) {
            throw new AccesoDenegadoCalificacionException();
        }

        // 3. RN-31.2: solo reservas COMPLETADAS
        if (reserva.getEstado() != EstadoReserva.COMPLETADA) {
            throw new ReservaNoCompletadaException(reserva.getEstado());
        }

        // 4. RN-31.3: una sola calificación por reserva
        if (calificacionRepository.findByReservaId(reservaId).isPresent()) {
            throw new CalificacionDuplicadaException();
        }

        // 5. Construir y persistir
        Calificacion calificacion = Calificacion.builder()
                .reservaId(reservaId)
                .compradorId(compradorId)
                .cocineraId(reserva.getCocineraId())
                .estrellas(request.getEstrellas())
                .comentario(normalizarComentario(request.getComentario()))
                .estado(EstadoCalificacion.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .fechaLimitePublicacion(calificacionValidator.calcularFechaLimite(LocalDateTime.now()))
                .build();

        CalificacionEntity guardada = calificacionRepository.save(
                calificacionEntityMapper.toEntity(calificacion));
        Calificacion domain = calificacionEntityMapper.toDomain(guardada);

        // 6. RN-31.6: actualizar métricas del perfil
        actualizarMetricasPerfil(reserva.getCocineraId());

        log.info("HU-31: calificación {} guardada ({} estrellas) para la cocinera {}",
                domain.getId(), domain.getEstrellas(), domain.getCocineraId());

        return calificacionMapper.toResponse(domain);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponseDTO<CalificacionResponseDTO> listarPorCocinera(
            UUID cocineraId, Integer estrellas, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCreacion"));

        Page<CalificacionEntity> pagina = (estrellas != null)
                ? calificacionRepository.findByCocineraIdAndEstrellas(cocineraId, EstadoCalificacion.PUBLICADA, estrellas, pageable)
                : calificacionRepository.findByCocineraId(cocineraId, EstadoCalificacion.PUBLICADA, pageable);

        return PaginaResponseDTO.<CalificacionResponseDTO>builder()
                .contenido(pagina.getContent().stream()
                        .map(calificacionEntityMapper::toDomain)
                        .map(calificacionMapper::toResponse)
                        .toList())
                .page(pagina.getNumber())
                .size(pagina.getSize())
                .totalElementos(pagina.getTotalElements())
                .totalPaginas(pagina.getTotalPages())
                .hayMas(pagina.hasNext())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCalificacionesDTO obtenerResumen(UUID cocineraId) {
        Double promedio = calificacionRepository.promedioPorCocinera(cocineraId);
        Long total = calificacionRepository.contarTotal(cocineraId);
        Long positivas = calificacionRepository.contarPositivas(cocineraId);

        return ResumenCalificacionesDTO.builder()
                .promedio(promedio != null ? Math.round(promedio * 100.0) / 100.0 : null)
                .total(total != null ? total : 0L)
                .positivas(positivas != null ? positivas : 0L)
                .build();
    }

    // ============ Helpers privados ============

    /** RN-31.5: normaliza el comentario (trim, null si vacío). */
    private String normalizarComentario(String comentario) {
        if (comentario == null) return null;
        String t = comentario.trim();
        return t.isEmpty() ? null : t;
    }

    /** RN-31.6: recalcula el promedio y las reseñas positivas en el perfil. */
    private void actualizarMetricasPerfil(UUID cocineraId) {
    perfilRepository.findById(cocineraId).ifPresent(perfil -> {
        Double promedio = calificacionRepository.promedioPorCocinera(cocineraId);
        Long positivas = calificacionRepository.contarPositivas(cocineraId);

        PerfilCocineraEntity actualizado = perfil.toBuilder()
                .promedioCalificacion(promedio != null
                        ? Math.round(promedio * 100.0) / 100.0 : 0.0)
                .resenasPositivas(positivas != null ? positivas.intValue() : 0)
                .esDestacada(positivas != null && positivas >= 50)   // ← RN-10
                .build();

        perfilRepository.save(actualizado);
    });

}

        @Override
        @Transactional
        public int publicarPendientesVencidas(LocalDateTime ahora) {
        List<CalificacionEntity> pendientes = calificacionRepository
                .findByEstadoAndFechaLimitePublicacionLessThanEqual(
                        EstadoCalificacion.PENDIENTE, ahora);

        int publicadas = 0;
        for (CalificacionEntity entity : pendientes) {
                Calificacion c = calificacionEntityMapper.toDomain(entity);
                c.publicar(ahora);
                calificacionRepository.save(calificacionEntityMapper.toEntity(c));
                actualizarMetricasPerfil(c.getCocineraId());
                publicadas++;
        }
        if (publicadas > 0) log.info("HU-31: {} calificaciones publicadas por ventana", publicadas);
        return publicadas;
        }
}