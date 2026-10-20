package com.ollacercana.core.models;

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