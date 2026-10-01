package com.ollacercana.config;

import com.ollacercana.model.domain.*;
import com.ollacercana.repository.PlatoRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Order(2)
@Component
@Profile("!test")
public class PlatoSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PlatoSeeder.class);
    private final PlatoRepository platoRepository;

    public PlatoSeeder(PlatoRepository platoRepository) {
        this.platoRepository = platoRepository;
    }

    @Override
    public void run(String... args) {
        if (platoRepository.count() == 0) {
            UUID cocinera1 = UUID.fromString("11111111-1111-1111-1111-111111111111");

            Plato plato1 = Plato.builder()
                    .id(UUID.randomUUID())
                    .cocineraId(cocinera1)
                    .nombre("Bandeja Paisa Tradicional")
                    .descripcion("Frijoles campesinos, arroz, carne molida, chicharrón crocante y aguacate")
                    .fotoUrl("https://images.unsplash.com/photo-1546069901-ba9599a7e63c")
                    .tipoComida(TipoComida.ALMUERZO)
                    .restricciones(List.of(RestriccionAlimentaria.SIN_GLUTEN))
                    .porcionesTotales(8)
                    .porcionesComprometidas(2)
                    .precioPorcion(new BigDecimal("18000"))
                    .estado(EstadoPlato.ACTIVO)
                    .fechaPublicacion(LocalDateTime.now().minusMinutes(30))
                    .fechaExpiracion(LocalDateTime.now().plusHours(3).plusMinutes(30))
                    .horaDisponibilidad(LocalDateTime.now().plusHours(1))
                    .puntoEntrega("Torre 2 Apto 401, Carrera 15 #120-30")
                    .latitud(4.6800)
                    .longitud(-74.0550)
                    .build();

            Plato plato2 = Plato.builder()
                    .id(UUID.randomUUID())
                    .cocineraId(cocinera1)
                    .nombre("Ajiaco Santafereño")
                    .descripcion("Con pollo desmechado, tres tipos de papa, mazorca, alcaparras y crema de leche")
                    .fotoUrl("https://images.unsplash.com/photo-1547592180-85f173990554")
                    .tipoComida(TipoComida.ALMUERZO)
                    .restricciones(List.of())
                    .porcionesTotales(6)
                    .porcionesComprometidas(0)
                    .precioPorcion(new BigDecimal("16000"))
                    .estado(EstadoPlato.ACTIVO)
                    .fechaPublicacion(LocalDateTime.now().minusHours(1))
                    .fechaExpiracion(LocalDateTime.now().plusHours(3))
                    .horaDisponibilidad(LocalDateTime.now().plusHours(1))
                    .puntoEntrega("Portería Principal, Calle 127 #19-45")
                    .latitud(4.6850)
                    .longitud(-74.0500)
                    .build();

            platoRepository.saveAll(List.of(plato1, plato2));
            log.info("Platos de prueba creados para consultas de cercanía (RN-05).");
        }
    }
}