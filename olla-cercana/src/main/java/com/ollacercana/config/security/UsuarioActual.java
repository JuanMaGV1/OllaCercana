package com.ollacercana.config.security;

import com.ollacercana.controller.handlers.exception.AccesoDenegadoException;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsuarioActual {

    private final CuentaRepository cuentaRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;

    private Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccesoDenegadoException("No hay usuario autenticado en la sesión actual");
        }
        return auth;
    }

    public Long getCuentaId() {
        Authentication auth = getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof CuentaUserDetails cud) {
            return cud.getCuentaId();
        }

        String username = auth.getName();
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            // ✅ el repo devuelve CuentaEntity, no Cuenta
            return cuentaRepository.findByIdentificador(username)
                    .map(cuentaEntity -> cuentaEntity.getId())
                    .orElseThrow(() -> new AccesoDenegadoException("No se pudo resolver el ID de cuenta del usuario actual"));
        }
    }

    public UUID getCocineraId() {
        return getCocineraIdOpt()
                .orElseThrow(() -> new AccesoDenegadoException("La cuenta autenticada no tiene un perfil de cocinera asociado"));
    }

    public Optional<UUID> getCocineraIdOpt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CuentaUserDetails cud && cud.getCocineraId() != null) {
            return Optional.of(cud.getCocineraId());
        }

        try {
            Long cuentaId = getCuentaId();
            return perfilCocineraRepository.findByCuentaId(cuentaId)
                    .map(perfilEntity -> perfilEntity.getId());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String getIdentificador() {
        return getAuthentication().getName();
    }

    public List<String> getRoles() {
        Authentication auth = getAuthentication();
        if (auth.getAuthorities() == null) {
            return Collections.emptyList();
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .toList();
    }

    public boolean tieneRol(Rol rol) {
        return getRoles().contains(rol.name());
    }
}