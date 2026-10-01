package com.ollacercana.config;

import com.ollacercana.mapper.PlatoEntityMapper;
import com.ollacercana.model.domain.*;
import com.ollacercana.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Order(2)
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class PlatoSeeder implements CommandLineRunner {

    private final PlatoRepository platoRepository;
    private final PlatoEntityMapper platoMapper;

    @Override
    public void run(String... args) {
        if (platoRepository.count() > 0) return;

        UUID cocinera1 = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Plato plato1 = Plato.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocinera1)
                .nombre("Bandeja Paisa Tradicional")
                .descripcion("Frijoles, arroz, carne, chicharrón y aguacate")
                .fotoUrl("https://images.unsplash.com/photo-1546069901")
                .tipoComida(TipoComida.ALMUERZO)
                .restricciones(List.of(RestriccionAlimentaria.SIN_GLUTEN))
                .porcionesTotales(8)
                .porcionesComprometidas(2)
                .precioPorcion(new BigDecimal("18000"))
                .estado(EstadoPlato.ACTIVO)
                .horaDisponibilidad(LocalDateTime.now().plusHours(1))
                .fechaPublicacion(LocalDateTime.now().minusMinutes(30))
                .fechaExpiracion(LocalDateTime.now().plusHours(3))
                .puntoEntrega("Torre 2 Apto 401")
                .latitud(4.6800)
                .longitud(-74.0550)
                .build();

        Plato plato2 = Plato.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocinera1)
                .nombre("Ajiaco Santafereño")
                .descripcion("Con pollo, tres papas, mazorca, alcaparras y crema")
                .fotoUrl("https://images.unsplash.com/photo-1547592180")
                .tipoComida(TipoComida.ALMUERZO)
                .restricciones(List.of())
                .porcionesTotales(6)
                .porcionesComprometidas(0)
                .precioPorcion(new BigDecimal("16000"))
                .estado(EstadoPlato.ACTIVO)
                .horaDisponibilidad(LocalDateTime.now().plusHours(1))
                .fechaPublicacion(LocalDateTime.now().minusHours(1))
                .fechaExpiracion(LocalDateTime.now().plusHours(3))
                .puntoEntrega("Portería Principal")
                .latitud(4.6850)
                .longitud(-74.0500)
                .build();

        platoRepository.saveAll(List.of(
                platoMapper.toEntity(plato1),
                platoMapper.toEntity(plato2)
        ));
        log.info("Platos de prueba creados.");
    }
}