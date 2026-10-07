package com.ollacercana.config;

import com.ollacercana.domain.Credenciales;
import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.Identidad;
import com.ollacercana.domain.Rol;
import com.ollacercana.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final CuentaRepository cuentaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!cuentaRepository.existsByCorreo("admin@ollacercana.com")) {
            log.info("Creando cuenta de administrador por defecto...");
            Cuenta admin = Cuenta.builder()
                    .identidad(new Identidad("Administrador", "admin@ollacercana.com", "3000000000", null))
                    .credenciales(new Credenciales(passwordEncoder.encode("Admin123!"), null, true))
                    .roles(Set.of(Rol.ADMIN))
                    .build();
            cuentaRepository.save(admin);
            log.info("Cuenta ADMIN creada con éxito.");
        }
    }
}
