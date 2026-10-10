package com.ollacercana.persistence.repository;

import com.ollacercana.core.models.enums.EstadoCalificacion;
import com.ollacercana.persistence.entities.CalificacionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CalificacionRepositoryTest {

    @Autowired private CalificacionRepository repo;

    private UUID cocineraId;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        cocineraId = UUID.randomUUID();
    }

    private CalificacionEntity crear(UUID reservaId, int estrellas, Long compradorId) {
        LocalDateTime ahora = LocalDateTime.now();
        return repo.save(CalificacionEntity.builder()
                .reservaId(reservaId)
                .compradorId(compradorId)
                .cocineraId(cocineraId)
                .estrellas(estrellas)
                .estado(EstadoCalificacion.PUBLICADA)          
                .fechaCreacion(ahora)
                .fechaPublicacion(ahora)                       
                .fechaLimitePublicacion(ahora.plusHours(72)) 
                .build());
    }

    @Test
    @DisplayName("findByReservaId retorna la calificación existente")
    void findByReservaId() {
        UUID reservaId = UUID.randomUUID();
        crear(reservaId, 5, 1L);

        Optional<CalificacionEntity> found = repo.findByReservaId(reservaId);
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getEstrellas());
    }

    @Test
    @DisplayName("findByCocineraId pagina correctamente")
    void findByCocineraId() {
        for (int i = 0; i < 15; i++) crear(UUID.randomUUID(), 5, (long) i);

        Page<CalificacionEntity> page = repo.findByCocineraIdAndEstado(
        cocineraId, EstadoCalificacion.PUBLICADA, PageRequest.of(0, 10));
        assertEquals(10, page.getContent().size());
        assertEquals(15, page.getTotalElements());
    }

    @Test
    @DisplayName("findByCocineraIdAndEstrellas filtra por estrellas")
    void findByCocineraIdAndEstrellas() {
        crear(UUID.randomUUID(), 5, 1L);
        crear(UUID.randomUUID(), 3, 2L);
        crear(UUID.randomUUID(), 5, 3L);

        Page<CalificacionEntity> page = repo.findByCocineraIdAndEstadoAndEstrellas(
        cocineraId, EstadoCalificacion.PUBLICADA, 5, PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("promedioPorCocinera retorna el promedio correcto")
    void promedioPorCocinera() {
        crear(UUID.randomUUID(), 5, 1L);
        crear(UUID.randomUUID(), 4, 2L);
        crear(UUID.randomUUID(), 3, 3L);

        Double prom = repo.promedioPorCocinera(cocineraId);
        assertNotNull(prom);
        assertEquals(4.0, prom, 0.001);
    }

    @Test
    @DisplayName("contarPositivas cuenta solo estrellas >= 4")
    void contarPositivas() {
        crear(UUID.randomUUID(), 5, 1L);
        crear(UUID.randomUUID(), 4, 2L);
        crear(UUID.randomUUID(), 3, 3L);
        crear(UUID.randomUUID(), 2, 4L);

        assertEquals(2L, repo.contarPositivas(cocineraId));
    }

    @Test
    @DisplayName("contarTotal cuenta todas las filas de la cocinera")
    void contarTotal() {
        crear(UUID.randomUUID(), 5, 1L);
        crear(UUID.randomUUID(), 4, 2L);

        assertEquals(2L, repo.contarTotal(cocineraId));
    }
}