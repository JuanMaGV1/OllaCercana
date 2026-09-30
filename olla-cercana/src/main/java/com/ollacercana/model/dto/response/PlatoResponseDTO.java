package com.ollacercana.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.RestriccionAlimentaria;
import com.ollacercana.model.domain.TipoComida;

public record PlatoResponseDTO(
        UUID id,
        UUID cocineraId,
        String nombre,
        String descripcion,
        String fotoUrl,
        TipoComida tipoComida,
        List<RestriccionAlimentaria> restricciones,
        Integer porcionesTotales,
        Integer porcionesDisponibles,
        BigDecimal precioPorcion,
        EstadoPlato estado,
        LocalDateTime horaDisponibilidad,
        LocalDateTime fechaPublicacion,
        LocalDateTime fechaExpiracion,
        String puntoEntrega,
        Double latitud,
        Double longitud
) {}