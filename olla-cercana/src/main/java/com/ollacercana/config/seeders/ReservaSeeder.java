package com.ollacercana.config.seeders;

import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReservaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(3)
@Profile("!test")
public class ReservaSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ReservaSeeder.class);

    static final UUID COCINERA_DEMO = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID RESERVA_PARA_CONFIRMAR = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    static final UUID RESERVA_PARA_RECHAZAR = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    static final UUID RESERVA_POR_VENCER = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final int PORCIONES_NECESARIAS = 4;

    private final ReservaRepository reservaRepository;
    private final PlatoRepository platoRepository;

    public ReservaSeeder(ReservaRepository reservaRepository, PlatoRepository platoRepository) {
        this.reservaRepository = reservaRepository;
        this.platoRepository = platoRepository;
    }

    @Override
    public void run(String... args) {
        if (reservaRepository.count() > 0) return;

        try {
            Optional<PlatoEntity> platoDemoOpt = platoRepository
                    .findByCocineraIdAndEstado(COCINERA_DEMO, EstadoPlato.ACTIVO)
                    .stream()
                    .filter(p -> (p.getPorcionesTotales() -
                            (p.getPorcionesComprometidas() == null ? 0 : p.getPorcionesComprometidas()))
                            >= PORCIONES_NECESARIAS)
                    .max(Comparator.comparingInt(p ->
                            p.getPorcionesTotales() -
                            (p.getPorcionesComprometidas() == null ? 0 : p.getPorcionesComprometidas())));

            if (platoDemoOpt.isEmpty()) {
                log.info("No hay un plato de la cocinera demo con porciones suficientes; no se crean reservas de prueba.");
                return;
            }

            PlatoEntity plato = platoDemoOpt.get();
            LocalDateTime ahora = LocalDateTime.now();

            ReservaEntity paraConfirmar = ReservaEntity.builder()
                    .id(RESERVA_PARA_CONFIRMAR)
                    .platoId(plato.getId())
                    .cocineraId(plato.getCocineraId())
                    .compradorId(1L)
                    .cantidadPorciones(2)
                    .montoTotal(plato.getPrecioPorcion().multiply(BigDecimal.valueOf(2)))
                    .medioPago(MedioPago.NEQUI)
                    .estado(EstadoReserva.PENDIENTE)
                    .notaComprador("Sin cebolla, por favor")
                    .fechaCreacion(ahora)
                    .fechaLimiteConfirmacion(ahora.plusMinutes(10))
                    .build();

            ReservaEntity paraRechazar = ReservaEntity.builder()
                    .id(RESERVA_PARA_RECHAZAR)
                    .platoId(plato.getId())
                    .cocineraId(plato.getCocineraId())
                    .compradorId(2L)
                    .cantidadPorciones(1)
                    .montoTotal(plato.getPrecioPorcion())
                    .medioPago(MedioPago.EFECTIVO)
                    .estado(EstadoReserva.PENDIENTE)
                    .fechaCreacion(ahora)
                    .fechaLimiteConfirmacion(ahora.plusMinutes(10))
                    .build();

            ReservaEntity porVencer = ReservaEntity.builder()
                    .id(RESERVA_POR_VENCER)
                    .platoId(plato.getId())
                    .cocineraId(plato.getCocineraId())
                    .compradorId(3L)
                    .cantidadPorciones(1)
                    .montoTotal(plato.getPrecioPorcion())
                    .medioPago(MedioPago.DAVIPLATA)
                    .estado(EstadoReserva.PENDIENTE)
                    .notaComprador("Timbrar en la portería")
                    .fechaCreacion(ahora.minusMinutes(8))
                    .fechaLimiteConfirmacion(ahora.minusMinutes(8).plusMinutes(10)) // ya casi vence
                    .build();

            // Compromete porciones en el plato
            plato.setPorcionesComprometidas(
                    (plato.getPorcionesComprometidas() == null ? 0 : plato.getPorcionesComprometidas())
                            + PORCIONES_NECESARIAS);
            platoRepository.save(plato);

            reservaRepository.saveAll(List.of(paraConfirmar, paraRechazar, porVencer));

            log.info("Reservas de prueba (HU-12) creadas sobre el plato '{}'. Usa X-Cocinera-Id: {}",
                    plato.getNombre(), COCINERA_DEMO);
        } catch (RuntimeException e) {
            log.warn("No se pudieron crear las reservas de prueba: {}", e.getMessage());
        }
    }
}