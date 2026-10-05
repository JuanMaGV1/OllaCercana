package com.ollacercana.security;

import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.domain.Rol;
import com.ollacercana.exception.AccesoDenegadoException;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
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
            return cuentaRepository.findByIdentificador(username)
                    .map(Cuenta::getId)
                    .orElseThrow(() -> new AccesoDenegadoException("No se pudo resolver el ID de cuenta del usuario actual"));
        }
    }

    public UUID getCocineraId() {
        Authentication auth = getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof CuentaUserDetails cud && cud.getCocineraId() != null) {
            return cud.getCocineraId();
        }

        Long cuentaId = getCuentaId();
        return perfilCocineraRepository.findByCuentaId(cuentaId)
                .map(PerfilCocinera::getId)
                .orElseThrow(() -> new AccesoDenegadoException("La cuenta autenticada no tiene un perfil de cocinera asociado"));
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