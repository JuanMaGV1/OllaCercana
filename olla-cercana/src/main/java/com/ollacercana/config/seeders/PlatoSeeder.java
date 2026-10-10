package com.ollacercana.config.seeders;

import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.RestriccionAlimentaria;
import com.ollacercana.core.models.enums.TipoComida;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.repository.PlatoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * OC-210: carga platos de ejemplo con distintos tipos de comida y
 * combinaciones de restricciones alimentarias para poder demostrar
 * los escenarios de HU-07 (filtros por menú y restricciones) al
 * levantar la aplicación.
 *
 * Cubre:
 *  - 5 tipos de comida: ALMUERZO, CENA, POSTRE, BEBIDA, SNACK
 *  - 4 combinaciones de restricciones: SIN_GLUTEN, SIN_LACTOSA,
 *    VEGETARIANO, multi-restricción y plato sin restricciones
 */
@Component
@Order(2)
@Profile("!test")
public class PlatoSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PlatoSeeder.class);

    private final PlatoRepository platoRepository;
    private final PlatoEntityMapper platoEntityMapper;

    public PlatoSeeder(PlatoRepository platoRepository, PlatoEntityMapper platoEntityMapper) {
        this.platoRepository = platoRepository;
        this.platoEntityMapper = platoEntityMapper;
    }

    @Override
    public void run(String... args) {
        if (platoRepository.count() > 0) {
            log.info("PlatoSeeder: ya hay {} platos en la BD, se omite la carga", platoRepository.count());
            return;
        }

        UUID cocinera1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID cocinera2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        LocalDateTime ahora = LocalDateTime.now();

        List<Plato> catalogo = List.of(
                // ==================== COCINERA 1 ====================

                // 1. ALMUERZO con SIN_GLUTEN
                construir(cocinera1,
                        "Bandeja Paisa Tradicional",
                        "Frijoles campesinos, arroz, carne molida, chicharrón crocante y aguacate",
                        TipoComida.ALMUERZO,
                        List.of(RestriccionAlimentaria.SIN_GLUTEN),
                        8, 2, "18000",
                        "Torre 2 Apto 401, Carrera 15 #120-30",
                        4.6800, -74.0550, ahora, 30),

                // 2. ALMUERZO sin restricciones
                construir(cocinera1,
                        "Ajiaco Santafereño",
                        "Con pollo desmechado, tres tipos de papa, mazorca, alcaparras y crema de leche",
                        TipoComida.ALMUERZO,
                        List.of(),
                        6, 0, "16000",
                        "Portería Principal, Calle 127 #19-45",
                        4.6850, -74.0500, ahora, 60),

                // 3. POSTRE vegetariano
                construir(cocinera1,
                        "Postre de Natas",
                        "Dulce tradicional con natas, canela y arequipe",
                        TipoComida.POSTRE,
                        List.of(RestriccionAlimentaria.VEGETARIANO),
                        4, 0, "8000",
                        "Torre 2 Apto 401",
                        4.6800, -74.0550, ahora, 45),

                // 4. SNACK sin lactosa
                construir(cocinera1,
                        "Empanadas de Pipián",
                        "Empanadas de pipián con ají de maní",
                        TipoComida.SNACK,
                        List.of(RestriccionAlimentaria.SIN_LACTOSA),
                        12, 4, "3500",
                        "Torre 2 Apto 401",
                        4.6800, -74.0550, ahora, 20),

                // ==================== COCINERA 2 ====================

                // 5. CENA sin lactosa
                construir(cocinera2,
                        "Sopa de Pollo con Verduras",
                        "Sopa casera con pollo campesino y verduras frescas",
                        TipoComida.CENA,
                        List.of(RestriccionAlimentaria.SIN_LACTOSA),
                        5, 1, "14000",
                        "Torre 4 Apto 202",
                        4.6750, -74.0600, ahora, 15),

                // 6. BEBIDA con las 3 restricciones (vegetariano + sin gluten + sin lactosa)
                construir(cocinera2,
                        "Jugo Natural de Mora",
                        "Jugo de mora recién preparado sin azúcar añadida",
                        TipoComida.BEBIDA,
                        List.of(
                                RestriccionAlimentaria.VEGETARIANO,
                                RestriccionAlimentaria.SIN_GLUTEN,
                                RestriccionAlimentaria.SIN_LACTOSA),
                        10, 3, "5000",
                        "Torre 4 Apto 202",
                        4.6750, -74.0600, ahora, 10)
        );

        platoRepository.saveAll(catalogo.stream().map(platoEntityMapper::toEntity).toList());
        log.info("OC-210: {} platos cargados con distintas etiquetas y restricciones", catalogo.size());
    }

    /**
     * Helper para construir un Plato (dominio) con todos los campos.
     *
     * @param minutosDesdePublicacion cuántos minutos atrás se publicó (mayor = más viejo)
     */
    private Plato construir(UUID cocineraId,
                            String nombre,
                            String descripcion,
                            TipoComida tipo,
                            List<RestriccionAlimentaria> restricciones,
                            int porcionesTotales,
                            int porcionesComprometidas,
                            String precio,
                            String puntoEntrega,
                            double latitud,
                            double longitud,
                            LocalDateTime ahora,
                            int minutosDesdePublicacion) {

        LocalDateTime publicacion = ahora.minusMinutes(minutosDesdePublicacion);

        return Plato.builder()
                .id(UUID.randomUUID())
                .cocineraId(cocineraId)
                .nombre(nombre)
                .descripcion(descripcion)
                .fotoUrl("https://images.unsplash.com/photo-" + UUID.randomUUID())
                .tipoComida(tipo)
                .restricciones(restricciones)
                .porcionesTotales(porcionesTotales)
                .porcionesComprometidas(porcionesComprometidas)
                .precioPorcion(new BigDecimal(precio))
                .estado(EstadoPlato.ACTIVO)
                .fechaPublicacion(publicacion)
                .fechaExpiracion(publicacion.plusHours(4))         // RN-02: expira 4h después
                .horaDisponibilidad(ahora.plusHours(1))
                .puntoEntrega(puntoEntrega)
                .latitud(latitud)
                .longitud(longitud)
                .build();
    }
}