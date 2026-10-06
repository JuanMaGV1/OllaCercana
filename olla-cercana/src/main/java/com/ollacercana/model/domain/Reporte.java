package com.ollacercana.model.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Reporte {
    private UUID id;
    private UUID reservaId;
    private EstadoReporte estado;
    private LocalDateTime fechaCreacion;
}