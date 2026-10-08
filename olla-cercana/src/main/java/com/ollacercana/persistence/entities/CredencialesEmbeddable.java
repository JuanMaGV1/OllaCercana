package com.ollacercana.persistence.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredencialesEmbeddable {
    private String contrasenaHash;
    private String tokenFCM;
    private Boolean celularVerificado;
}