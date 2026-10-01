package com.ollacercana.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credenciales {

    private String contrasenaHash;
    private String tokenFCM;
    private Boolean celularVerificado;
}