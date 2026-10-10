package com.ollacercana.config.seeders;

import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.Credenciales;
import com.ollacercana.core.models.Identidad;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.persistence.mappers.CuentaEntityMapper;
import com.ollacercana.persistence.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * HU-01 / OC-238: siembra la cuenta ADMIN por defecto (admin@ollacercana.com).
 * Aplica {@link Cuenta#inicializar()} antes de persistir para que los defaults
 * del dominio (estado, fechaRegistro, avisosActivos, celularVerificado) queden
 * rellenos y no violen las restricciones NOT NULL de la tabla.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final CuentaRepository cuentaRepository;
    private final CuentaEntityMapper cuentaMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (cuentaRepository.existsByCorreo("admin@ollacercana.com")) {
            return;
        }

        log.info("Creando cuenta de administrador por defecto...");

        Cuenta admin = Cuenta.builder()
                .identidad(new Identidad("Administrador", "admin@ollacercana.com", "3000000000", null))
                .credenciales(new Credenciales(passwordEncoder.encode("Admin123!"), null, true))
                .roles(Set.of(Rol.ADMIN))
                .build();
        admin.inicializar();

        cuentaRepository.save(cuentaMapper.toEntity(admin));
        log.info("Cuenta ADMIN creada con éxito.");
    }
}