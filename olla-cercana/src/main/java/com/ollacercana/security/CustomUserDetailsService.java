package com.ollacercana.security;

import com.ollacercana.model.domain.EstadoCuenta;
import com.ollacercana.persistence.entity.CuentaEntity;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CuentaRepository cuentaRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;

    @Override
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {
        CuentaEntity cuenta = cuentaRepository.findByIdentificador(identificador)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No se encontró cuenta con el identificador: " + identificador));

        boolean habilitada = cuenta.getEstado() != EstadoCuenta.BLOQUEADO;

        Collection<GrantedAuthority> authorities = (cuenta.getRoles() == null)
                ? Collections.emptyList()
                : cuenta.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.name()))
                .collect(Collectors.toList());

        String username = (cuenta.getIdentidad() != null && cuenta.getIdentidad().getCorreo() != null)
                ? cuenta.getIdentidad().getCorreo()
                : String.valueOf(cuenta.getId());

        String password = (cuenta.getCredenciales() != null && cuenta.getCredenciales().getContrasenaHash() != null)
                ? cuenta.getCredenciales().getContrasenaHash()
                : "";

        UUID cocineraId = perfilCocineraRepository.findByCuentaId(cuenta.getId())
                .map(PerfilCocineraEntity::getId)
                .orElse(null);

        return new CuentaUserDetails(
                cuenta.getId(),
                username,
                password,
                cocineraId,
                habilitada,
                authorities
        );
    }
}