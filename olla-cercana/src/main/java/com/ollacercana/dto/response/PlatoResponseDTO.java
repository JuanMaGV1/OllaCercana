package com.ollacercana.dto.response;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.MedioPago;
import com.ollacercana.domain.RestriccionAlimentaria;
import com.ollacercana.domain.TipoComida;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Detalle público y respuesta de un plato")
public record PlatoResponseDTO(
        @Schema(description = "ID del plato")
        UUID id,

        @Schema(description = "ID de la cocinera titular")
        UUID cocineraId,

        @Schema(description = "Nombre del plato")
        String nombre,

        @Schema(description = "Descripción detallada")
        String descripcion,

        @Schema(description = "URL de la foto")
        String fotoUrl,

        @Schema(description = "Tipo de comida")
        TipoComida tipoComida,

        @Schema(description = "Restricciones alimentarias")
        List<RestriccionAlimentaria> restricciones,

        @Schema(description = "Porciones totales publicadas")
        Integer porcionesTotales,

        @Schema(description = "Porciones actualmente disponibles")
        Integer porcionesDisponibles,

        @Schema(description = "Precio por porción")
        BigDecimal precioPorcion,

        @Schema(description = "Estado actual del plato")
        EstadoPlato estado,

        @Schema(description = "Hora programada de disponibilidad")
        LocalDateTime horaDisponibilidad,

        @Schema(description = "Fecha de publicación")
        LocalDateTime fechaPublicacion,

        @Schema(description = "Fecha límite de vigencia / expiración")
        LocalDateTime fechaExpiracion,

        @Schema(description = "Punto de entrega aproximado o conjunto")
        String puntoEntrega,

        @Schema(description = "Latitud de la ubicación")
        Double latitud,

        @Schema(description = "Longitud de la ubicación")
        Double longitud,

        @Schema(description = "Métodos de pago que acepta la cocinera titular (OC-254). Valores permitidos: NEQUI, DAVIPLATA, EFECTIVO, TRANSFERENCIA_BANCARIA",
                allowableValues = {"NEQUI", "DAVIPLATA", "EFECTIVO", "TRANSFERENCIA_BANCARIA"},
                example = "[\"NEQUI\", \"EFECTIVO\"]")
        List<MedioPago> mediosPago,

        @Schema(description = "Nota informativa fija sobre pagos en OllaCercana (OC-254)", example = "OllaCercana no procesa dinero. El pago se realiza contra entrega")
        String notaPago
) {

    public static final String NOTA_PAGO_CONTRA_ENTREGA = "OllaCercana no procesa dinero. El pago se realiza contra entrega";

    // Constructor de compatibilidad (17 parámetros)
    public PlatoResponseDTO(
            UUID id, UUID cocineraId, String nombre, String descripcion, String fotoUrl,
            TipoComida tipoComida, List<RestriccionAlimentaria> restricciones,
            Integer porcionesTotales, Integer porcionesDisponibles, BigDecimal precioPorcion,
            EstadoPlato estado, LocalDateTime horaDisponibilidad, LocalDateTime fechaPublicacion,
            LocalDateTime fechaExpiracion, String puntoEntrega, Double latitud, Double longitud
    ) {
        this(id, cocineraId, nombre, descripcion, fotoUrl, tipoComida, restricciones,
                porcionesTotales, porcionesDisponibles, precioPorcion, estado,
                horaDisponibilidad, fechaPublicacion, fechaExpiracion, puntoEntrega,
                latitud, longitud, List.of(), NOTA_PAGO_CONTRA_ENTREGA);
    }

    public PlatoResponseDTO withMediosPago(List<MedioPago> mediosPago) {
        return new PlatoResponseDTO(
                this.id, this.cocineraId, this.nombre, this.descripcion, this.fotoUrl,
                this.tipoComida, this.restricciones, this.porcionesTotales,
                this.porcionesDisponibles, this.precioPorcion, this.estado,
                this.horaDisponibilidad, this.fechaPublicacion, this.fechaExpiracion,
                this.puntoEntrega, this.latitud, this.longitud,
                mediosPago != null ? mediosPago : List.of(),
                NOTA_PAGO_CONTRA_ENTREGA
        );
    }
}