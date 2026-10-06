package com.ollacercana.mapper;

import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.Rol;
import com.ollacercana.dto.request.RegistroRequestDTO;
import com.ollacercana.dto.response.RegistroResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface CuentaMapper {

    @Mapping(target = "identidad.nombre", source = "nombre")
    @Mapping(target = "identidad.correo", source = "correo")
    @Mapping(target = "identidad.celular", source = "celular")
    @Mapping(target = "credenciales.contrasenaHash", source = "contrasena")
    @Mapping(target = "roles", source = "rol", qualifiedByName = "rolToRolesSet")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    Cuenta toDomain(RegistroRequestDTO request);

    @Mapping(target = "correo", source = "identidad.correo")
    @Mapping(target = "rol", source = "roles", qualifiedByName = "rolesSetToRol")
    RegistroResponseDTO toResponseDTO(Cuenta cuenta);

    @Named("rolToRolesSet")
    default Set<Rol> rolToRolesSet(Rol rol) {
        return rol != null ? Set.of(rol) : Set.of();
    }

    @Named("rolesSetToRol")
    default Rol rolesSetToRol(Set<Rol> roles) {
        return (roles != null && !roles.isEmpty()) ? roles.iterator().next() : null;
    }
}