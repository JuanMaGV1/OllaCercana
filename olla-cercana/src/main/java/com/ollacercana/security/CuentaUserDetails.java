package com.ollacercana.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ollacercana.model.domain.Cuenta;

import java.util.Collection;
import java.util.UUID;

@Getter
public class CuentaUserDetails implements UserDetails {

    private final Long cuentaId;
    private final String username;
    private final String password;
    private final UUID cocineraId;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CuentaUserDetails(
            Long cuentaId,
            String username,
            String password,
            UUID cocineraId,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities) {
        this.cuentaId = cuentaId;
        this.username = username;
        this.password = password;
        this.cocineraId = cocineraId;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}