package com.ollacercana.mapper;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class PlatoDtoMapper {

    /**
     * RequestDTO → Dominio.
     * Los campos generados por el sistema (id, estado, fechas, version)
     * se inicializan en el método de dominio `publicar()`.
     */
    public Plato toDomain(PlatoRequestDTO dto) {
        if (dto == null) return null;

        return Plato.builder()
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .fotoUrl(dto.fotoUrl())
                .tipoComida(dto.tipoComida())
                .restricciones(dto.restricciones())
                .porcionesTotales(dto.porcionesTotales())
                .precioPorcion(dto.precioPorcion())
                .horaDisponibilidad(dto.horaDisponibilidad())
                .puntoEntrega(dto.puntoEntrega())
                .latitud(dto.latitud())
                .longitud(dto.longitud())
                .porcionesComprometidas(0)
                .estado(EstadoPlato.ACTIVO)
                .version(0)
                .build();
    }

    /**
     * Dominio → ResponseDTO.
     */
    public PlatoResponseDTO toResponse(Plato plato) {
        if (plato == null) return null;

        return new PlatoResponseDTO(
            plato.getId(),
            plato.getCocineraId(),
            plato.getNombre(),
            plato.getDescripcion(),
            plato.getFotoUrl(),
            plato.getTipoComida(),
            plato.getRestricciones(),
            plato.getPorcionesTotales(),
            plato.getPorcionesDisponibles(),
            plato.getPrecioPorcion(),
            plato.getEstado(),
            plato.getHoraDisponibilidad(),
            plato.getFechaPublicacion(),
            plato.getFechaExpiracion(),
            plato.getLatitud(),
            plato.getLongitud(),
            plato.getPuntoEntrega()
        );
    }
}