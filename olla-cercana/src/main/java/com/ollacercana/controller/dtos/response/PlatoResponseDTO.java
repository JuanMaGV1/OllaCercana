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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Detalle público y respuesta de un plato")
public class PlatoResponseDTO {

    public static final String NOTA_PAGO_CONTRA_ENTREGA =
            "OllaCercana no procesa dinero. El pago se realiza contra entrega";

    private UUID id;
    private UUID cocineraId;
    private String nombre;
    private String descripcion;
    private String fotoUrl;
    private TipoComida tipoComida;
    private List<RestriccionAlimentaria> restricciones;
    private Integer porcionesTotales;
    private Integer porcionesDisponibles;
    private BigDecimal precioPorcion;
    private EstadoPlato estado;
    private LocalDateTime horaDisponibilidad;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaExpiracion;
    private String puntoEntrega;
    private Double latitud;
    private Double longitud;

    @Schema(description = "Métodos de pago aceptados por la cocinera",
            allowableValues = {"NEQUI", "DAVIPLATA", "EFECTIVO", "TRANSFERENCIA_BANCARIA"},
            example = "[\"NEQUI\", \"EFECTIVO\"]")
    private List<MedioPago> mediosPago;

    @Schema(description = "Nota informativa sobre pagos",
            example = "OllaCercana no procesa dinero. El pago se realiza contra entrega")
    @Builder.Default
    private String notaPago = NOTA_PAGO_CONTRA_ENTREGA;

    /** Setter fluido que usa el controller para enriquecer la respuesta. */
    public PlatoResponseDTO withMediosPago(List<MedioPago> mediosPago) {
        this.mediosPago = (mediosPago != null) ? mediosPago : List.of();
        this.notaPago = NOTA_PAGO_CONTRA_ENTREGA;
        return this;
    }
}