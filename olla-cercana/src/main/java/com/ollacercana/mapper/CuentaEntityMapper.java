package com.ollacercana.mapper;

import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.model.domain.Credenciales;
import com.ollacercana.model.domain.EstadoCuenta;
import com.ollacercana.model.domain.Identidad;
import com.ollacercana.persistence.entity.CuentaEntity;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface CuentaEntityMapper {

    default CuentaEntity toEntity(Cuenta domain) {
        if (domain == null) return null;

        Identidad identidad = domain.getIdentidad();
        Credenciales credenciales = domain.getCredenciales();

        return CuentaEntity.builder()
                .id(domain.getId() != null ? domain.getId() : java.util.UUID.randomUUID())
                .nombre(identidad != null ? identidad.getNombre() : null)
                .correo(identidad != null ? identidad.getCorreo() : null)
                .celular(identidad != null ? identidad.getCelular() : null)
                .fotoUrl(identidad != null ? identidad.getFotoUrl() : null)
                .contrasenaHash(credenciales != null ? credenciales.getContrasenaHash() : null)
                .tokenFCM(credenciales != null ? credenciales.getTokenFCM() : null)
                .celularVerificado(credenciales != null
                        && Boolean.TRUE.equals(credenciales.isCelularVerificado()))
                .estado(domain.getEstado() != null
                        ? domain.getEstado()
                        : EstadoCuenta.PENDIENTE_VERIFICACION)
                .roles(domain.getRoles())
                .fechaRegistro(domain.getFechaRegistro() != null
                        ? domain.getFechaRegistro()
                        : LocalDateTime.now())
                .build();
    }

    default Cuenta toDomain(CuentaEntity entity) {
        if (entity == null) return null;

        Identidad identidad = Identidad.builder()
                .nombre(entity.getNombre())
                .correo(entity.getCorreo())
                .celular(entity.getCelular())
                .fotoUrl(entity.getFotoUrl())
                .build();

        Credenciales credenciales = Credenciales.builder()
                .contrasenaHash(entity.getContrasenaHash())
                .tokenFCM(entity.getTokenFCM())
                .celularVerificado(entity.getCelularVerificado())
                .build();

        return Cuenta.builder()
                .id(entity.getId())
                .identidad(identidad)
                .credenciales(credenciales)
                .estado(entity.getEstado())
                .roles(entity.getRoles())
                .fechaRegistro(entity.getFechaRegistro())
                .build();
    }
}