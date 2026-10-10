package com.ollacercana.config.security;

import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de generación y validación de JWT.
 *
 * FEAT-12 — Seguridad JWT y roles — OC-169
 * Claims emitidos: {@code correo}, {@code roles}, {@code cuentaId}, {@code cocineraId}
 * Algoritmo: HS256 (HMAC-SHA256), secret en {@code application.yml} Base64.
 *
 * @see OC-170 Dependencias y configuración JWT
 * @see OC-174 Implementación real de JwtService
 * @see OC-182 Integración con SesionController
 * @see OC-188 Pruebas unitarias de seguridad
 */
@Slf4j
@Service
public class JwtService {

    private final String secret;
    private final long expirationMs;
    private final PerfilCocineraRepository perfilCocineraRepository;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs,
            PerfilCocineraRepository perfilCocineraRepository) {
        this.secret = secret;
        this.expirationMs = expirationMs;
        this.perfilCocineraRepository = perfilCocineraRepository;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generarToken(Cuenta cuenta) {
        Map<String, Object> claims = new HashMap<>();

        String correo = (cuenta.getIdentidad() != null) ? cuenta.getIdentidad().getCorreo() : null;
        claims.put("correo", correo);

        List<String> roles = (cuenta.getRoles() != null)
                ? cuenta.getRoles().stream().map(Rol::name).collect(Collectors.toList())
                : Collections.emptyList();
        claims.put("roles", roles);

        if (cuenta.getId() != null) {
            claims.put("cuentaId", cuenta.getId());
            perfilCocineraRepository.findByCuentaId(cuenta.getId())
            .ifPresent(perfilEntity -> claims.put("cocineraId", perfilEntity.getId().toString()));
        }

        Date ahora = new Date();
        Date fechaExpiracion = new Date(ahora.getTime() + expirationMs);

        String subject = (cuenta.getId() != null)
                ? cuenta.getId().toString()
                : (correo != null ? correo : "usuario");

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(ahora)
                .expiration(fechaExpiracion)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT inválido o expirado: {}", e.getMessage());
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extraerCuentaId(String token) {
        Object cuentaIdObj = extractAllClaims(token).get("cuentaId");
        if (cuentaIdObj instanceof Number n) {
            return n.longValue();
        } else if (cuentaIdObj instanceof String s) {
            return Long.parseLong(s);
        }
        String sub = extraerSubject(token);
        try {
            return Long.parseLong(sub);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String extraerCorreo(String token) {
        return (String) extractAllClaims(token).get("correo");
    }

    @SuppressWarnings("unchecked")
    public List<String> extraerRoles(String token) {
        Object rolesObj = extractAllClaims(token).get("roles");
        if (rolesObj instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }

    public UUID extraerCocineraId(String token) {
        String cocineraIdStr = (String) extractAllClaims(token).get("cocineraId");
        return (cocineraIdStr != null && !cocineraIdStr.isBlank())
                ? UUID.fromString(cocineraIdStr)
                : null;
    }
}