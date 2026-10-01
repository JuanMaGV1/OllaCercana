package com.ollacercana.config;

import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import com.ollacercana.repository.PerfilCocineraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Order(1)
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class PerfilCocineraSeeder implements CommandLineRunner {

    private final PerfilCocineraRepository perfilRepository;

    @Override
    public void run(String... args) {
        if (perfilRepository.count() > 0) return;

        UUID cocinera1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID cocinera2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID cocinera3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

        PerfilCocineraEntity perfil1 = PerfilCocineraEntity.builder()
                .id(cocinera1)
                .cuentaId(cocinera1)
                .conjuntoResidencial("Torres del Parque")
                .presentacion("Comida casera tradicional")
                .verificada(true)
                .pausada(false)
                .esDestacada(false)
                .promedioCalificacion(0.0)
                .resenasPositivas(0)
                .build();

        PerfilCocineraEntity perfil2 = PerfilCocineraEntity.builder()
                .id(cocinera2)
                .cuentaId(cocinera2)
                .conjuntoResidencial("Altos de la Colina")
                .presentacion("Especialidades vegetarianas")
                .verificada(false)
                .pausada(false)
                .esDestacada(false)
                .promedioCalificacion(0.0)
                .resenasPositivas(0)
                .build();

        PerfilCocineraEntity perfil3 = PerfilCocineraEntity.builder()
                .id(cocinera3)
                .cuentaId(cocinera3)
                .conjuntoResidencial("Portal del Norte")
                .presentacion("Postres y snacks")
                .verificada(true)
                .pausada(true)
                .esDestacada(false)
                .promedioCalificacion(0.0)
                .resenasPositivas(0)
                .build();

        perfilRepository.saveAll(java.util.List.of(perfil1, perfil2, perfil3));
        log.info("Perfiles de cocinera de prueba creados.");
    }
}