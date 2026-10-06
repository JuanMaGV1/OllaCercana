package com.ollacercana.model.dto.response;

import com.ollacercana.model.domain.EstadoCuenta;
import com.ollacercana.model.domain.Rol;

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