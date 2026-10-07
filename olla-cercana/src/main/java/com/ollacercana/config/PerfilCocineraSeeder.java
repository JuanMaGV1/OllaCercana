package com.ollacercana.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Order(1)
@Profile("!test")
@RequiredArgsConstructor
public class PerfilCocineraSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM perfiles_cocinera", Integer.class
        );

        if (total != null && total == 0) {
            String sql = "INSERT INTO perfiles_cocinera " +
                    "(id, conjunto_residencial, verificada, pausada, es_destacada, promedio_calificacion, resenas_positivas) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

                                                
            jdbcTemplate.update(sql,
                    UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    "Torres del Parque", true, false, false, 0.0, 0);

                                      
            jdbcTemplate.update(sql,
                    UUID.fromString("22222222-2222-2222-2222-222222222222"),
                    "Altos de la Colina", false, false, false, 0.0, 0);

                                             
            jdbcTemplate.update(sql,
                    UUID.fromString("33333333-3333-3333-3333-333333333333"),
                    "Portal del Norte", true, true, false, 0.0, 0);
        }
    }
}