package com.ollacercana.persistence.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentidadEmbeddable {
    private String nombre;
    private String correo;
    private String celular;
    private String fotoUrl;
}