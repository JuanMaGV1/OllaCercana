package com.ollacercana.model.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credenciales {
    private String contrasenaHash;
    private String tokenFCM;
    private Boolean celularVerificado;
}