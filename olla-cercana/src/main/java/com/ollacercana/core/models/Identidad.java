package com.ollacercana.core.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Identidad {

    private String nombre;
    private String correo;
    private String celular;
    private String fotoUrl;
    
}