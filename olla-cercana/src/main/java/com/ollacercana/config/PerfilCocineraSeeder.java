package com.ollacercana.config;

import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.repository.PerfilCocineraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Perfiles basico para el uso y probar historia de uso 4
 */

@Component
@RequiredArgsConstructor
public class PerfilCocineraSeeder implements CommandLineRunner {

    private final PerfilCocineraRepository repository;

    @Override
    public void run(String... args) {
        repository.registrar(new PerfilCocinera(
                UUID.fromString("11111111-1111-1111-1111-111111111111"), true, false
        ));
        repository.registrar(new PerfilCocinera(
                UUID.fromString("22222222-2222-2222-2222-222222222222"), false, false
        ));
        repository.registrar(new PerfilCocinera(
                UUID.fromString("33333333-3333-3333-3333-333333333333"), true, true
        ));
    }
}