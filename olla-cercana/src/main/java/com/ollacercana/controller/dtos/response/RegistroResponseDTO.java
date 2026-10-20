package com.ollacercana.controller.dtos.response;

import com.ollacercana.core.models.enums.EstadoCuenta;
import com.ollacercana.core.models.enums.Rol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroResponseDTO {

    private Long id;
    private String correo;
    private Rol rol;
    private EstadoCuenta estado;
}