package com.ollacercana.config.seeders;

import com.ollacercana.core.models.Medalla;
import com.ollacercana.core.models.enums.CodigoMedalla;
import com.ollacercana.persistence.mappers.MedallaEntityMapper;
import com.ollacercana.persistence.repository.MedallaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * HU-21 / OC-278: carga el catálogo inicial de medallas (solo las que falten).
 */
@Component
@Order(0)
@Profile("!test")
public class MedallaSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MedallaSeeder.class);

    private final MedallaRepository medallaRepository;
    private final MedallaEntityMapper medallaMapper;

    public MedallaSeeder(MedallaRepository medallaRepository, MedallaEntityMapper medallaMapper) {
        this.medallaRepository = medallaRepository;
        this.medallaMapper = medallaMapper;
    }

    @Override
    public void run(String... args) {
        List<Medalla> catalogo = List.of(
                Medalla.builder()
                        .codigo(CodigoMedalla.VECINO_FIEL)
                        .nombre("Vecino Fiel")
                        .requisito("Completar 3 reservas en un mismo mes con la misma cocinera")
                        .build(),
                Medalla.builder()
                        .codigo(CodigoMedalla.CONJUNTO_OLLA_VERDE)
                        .nombre("Conjunto Olla Verde")
                        .requisito("Que el conjunto residencial comercialice el 100% de las porciones "
                                + "publicadas durante la semana (vigencia de 7 días)")
                        .build());

        for (Medalla medalla : catalogo) {
            if (!medallaRepository.existsById(medalla.getCodigo())) {
                medallaRepository.save(medallaMapper.toEntity(medalla));   // ← fix
                log.info("Medalla {} cargada en el catálogo", medalla.getCodigo());
            }
        }
    }
}