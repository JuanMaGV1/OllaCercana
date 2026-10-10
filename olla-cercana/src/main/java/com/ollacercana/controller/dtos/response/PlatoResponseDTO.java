package com.ollacercana.controller.dtos.response;

import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.core.models.enums.RestriccionAlimentaria;
import com.ollacercana.core.models.enums.TipoComida;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.RestriccionAlimentaria;
import com.ollacercana.core.models.enums.TipoComida;
import com.ollacercana.core.models.enums.MedioPago;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle público y respuesta de un plato")
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
        Double longitud,
        @Schema(description = "Métodos de pago que acepta la cocinera titular", allowableValues = {"NEQUI", "DAVIPLATA", "EFECTIVO", "TRANSFERENCIA_BANCARIA"}, example = "[\"NEQUI\", \"EFECTIVO\"]")
        List<MedioPago> mediosPago,
        @Schema(description = "Información sobre pagos en la plataforma", example = "OllaCercana no procesa dinero. El pago se realiza contra entrega")
        String notaPago
) {
    public static final String NOTA_PAGO_CONTRA_ENTREGA = "OllaCercana no procesa dinero. El pago se realiza contra entrega";

    public PlatoResponseDTO(UUID id, UUID cocineraId, String nombre, String descripcion, String fotoUrl,
            TipoComida tipoComida, List<RestriccionAlimentaria> restricciones, Integer porcionesTotales,
            Integer porcionesDisponibles, BigDecimal precioPorcion, EstadoPlato estado,
            LocalDateTime horaDisponibilidad, LocalDateTime fechaPublicacion, LocalDateTime fechaExpiracion,
            String puntoEntrega, Double latitud, Double longitud) {
        this(id, cocineraId, nombre, descripcion, fotoUrl, tipoComida, restricciones, porcionesTotales,
                porcionesDisponibles, precioPorcion, estado, horaDisponibilidad, fechaPublicacion,
                fechaExpiracion, puntoEntrega, latitud, longitud, List.of(), NOTA_PAGO_CONTRA_ENTREGA);
    }

    /** OC-254: copia del detalle con los medios de pago de la cocinera titular. */
    public PlatoResponseDTO withMediosPago(List<MedioPago> mediosPago) {
        return new PlatoResponseDTO(id, cocineraId, nombre, descripcion, fotoUrl, tipoComida, restricciones,
                porcionesTotales, porcionesDisponibles, precioPorcion, estado, horaDisponibilidad,
                fechaPublicacion, fechaExpiracion, puntoEntrega, latitud, longitud,
                mediosPago != null ? mediosPago : List.of(), NOTA_PAGO_CONTRA_ENTREGA);
    }
}

