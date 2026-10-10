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
import java.util.stream.Collectors;

/**
 * Filtro JWT — intercepta cada petición antes de llegar al Controller.
 *
 * Extrae {@code Bearer <token>}, valida firma y expiración, carga
 * {@link UserDetails} y setea el {@code SecurityContext}.
 *
 * Si el token falta o es inválido, la petición sigue sin autenticar
 * (el {@link com.ollacercana.config.SecurityConfig} la rechazará con 401).
 *
 * @see OC-180 JwtAuthenticationFilter
 * @see OC-183 Leer la identidad desde el token (no headers)
 * @see OC-185 Manejo uniforme de 401/403
 */
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

        if (!jwtService.validarToken(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.extractAllClaims(jwt);
                String correo = (String) claims.get("correo");
                String identificador = (correo != null && !correo.isBlank())
                        ? correo
                        : claims.getSubject();

                UserDetails userDetails = userDetailsService.loadUserByUsername(identificador);

                Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                if (authorities == null || authorities.isEmpty()) {
                    List<String> rolesToken = jwtService.extraerRoles(jwt);
                    authorities = rolesToken.stream()
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                log.warn("No se pudo autenticar al usuario mediante JWT: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}