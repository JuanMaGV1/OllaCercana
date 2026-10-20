package com.ollacercana.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ollacercana.config.security.CustomAccessDeniedHandler;
import com.ollacercana.config.security.CustomAuthenticationEntryPoint;
import com.ollacercana.config.security.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Autenticación y registro
                        .requestMatchers(HttpMethod.POST, "/api/v1/sesiones", "/api/v1/cuentas").permitAll()

                        // 2. Documentación Swagger / OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 3. Catálogo y consultas públicas
                        .requestMatchers(HttpMethod.GET, "/api/v1/platos/cercanos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/platos/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/perfiles/destacadas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/cocineras/mapa").permitAll()

                        // 4. Calificaciones y reputación pública (HU-22)
                        .requestMatchers(HttpMethod.GET, "/api/v1/cocineras/*/calificaciones/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/cocineras/*/calificaciones/resumen").permitAll()

                        // 5. Medallas e insignias públicas (HU-21)
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/*/medallas").permitAll()

                        // 6. Motivos de reporte para formularios (HU-18)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/motivos").permitAll()

                        // El resto de peticiones requiere autenticación por rol
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:8080",
                "https://*.azurewebsites.net"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}