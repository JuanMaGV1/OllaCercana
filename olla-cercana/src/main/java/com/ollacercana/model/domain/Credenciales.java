package com.ollacercana.model.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credenciales {

    private String contrasenaHash;
    private String tokenFCM;

    @Builder.Default
    private boolean celularVerificado = false;
}