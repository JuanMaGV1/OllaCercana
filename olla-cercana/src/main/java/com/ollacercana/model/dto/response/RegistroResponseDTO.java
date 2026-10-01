package com.ollacercana.model.dto.response;

import com.ollacercana.model.domain.EstadoCuenta;
import com.ollacercana.model.domain.Rol;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroResponseDTO {

    private UUID id;
    private String correo;
    private Rol rol;
    private EstadoCuenta estado;
}