package com.ollacercana.mapper;

import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.model.domain.Credenciales;
import com.ollacercana.model.domain.Identidad;
import com.ollacercana.persistence.entity.CuentaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CuentaEntityMapper {

    default CuentaEntity toEntity(Cuenta domain) {
        if (domain == null) return null;

        return CuentaEntity.builder()
                .id(domain.getId())
                .nombre(domain.getIdentidad() != null ? domain.getIdentidad().getNombre() : null)
                .correo(domain.getIdentidad() != null ? domain.getIdentidad().getCorreo() : null)
                .celular(domain.getIdentidad() != null ? domain.getIdentidad().getCelular() : null)
                .fotoUrl(domain.getIdentidad() != null ? domain.getIdentidad().getFotoUrl() : null)
                .contrasenaHash(domain.getCredenciales() != null ? domain.getCredenciales().getContrasenaHash() : null)
                .tokenFCM(domain.getCredenciales() != null ? domain.getCredenciales().getTokenFCM() : null)
                .celularVerificado(domain.getCredenciales() != null ? domain.getCredenciales().getCelularVerificado() : false)
                .estado(domain.getEstado())
                .roles(domain.getRoles())
                .fechaRegistro(domain.getFechaRegistro())
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