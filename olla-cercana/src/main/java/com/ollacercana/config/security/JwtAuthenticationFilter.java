package com.ollacercana.config.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        if (jwtService.validarToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
            autenticarUsuario(request, jwt);
        }

        filterChain.doFilter(request, response);
    }

    private void autenticarUsuario(HttpServletRequest request, String jwt) {
        try {
            Claims claims = jwtService.extractAllClaims(jwt);
            String correo = (String) claims.get("correo");
            String identificador = (correo != null && !correo.isBlank()) ? correo : claims.getSubject();

            UserDetails userDetails = userDetailsService.loadUserByUsername(identificador);
            Collection<? extends GrantedAuthority> authorities = resolverAuthorities(userDetails, jwt);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (Exception e) {
            log.warn("No se pudo autenticar al usuario mediante JWT: {}", e.getMessage());
        }
    }

    private Collection<? extends GrantedAuthority> resolverAuthorities(UserDetails userDetails, String jwt) {
        if (userDetails.getAuthorities() != null && !userDetails.getAuthorities().isEmpty()) {
            return userDetails.getAuthorities();
        }
        return jwtService.extraerRoles(jwt).stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}