package com.ollacercana.config;

import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.MedioPago;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.domain.Reserva;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Reservas de prueba para probar HU-12 desde Swagger mientras no exista el endpoint de crear reserva.
 * Usan la cocinera sembrada 11111111-1111-1111-1111-111111111111
 * - aaaaaaaa-...: para CONFIRMAR
 * - bbbbbbbb-...: para RECHAZAR
 * - cccccccc-...: creada hace 8 min; recibe el recordatorio (RN-25) y expira sola en ~2 min (RN-04)
 */
@Component
@Order(3)
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
        if (reservaRepository.count() > 0) {
            return;
        }
        try {
            Optional<Plato> platoDemo = platoRepository.findByCocineraIdAndEstado(COCINERA_DEMO, EstadoPlato.ACTIVO)
                    .stream()
                    .filter(plato -> plato.getPorcionesDisponibles() >= PORCIONES_NECESARIAS)
                    .max(Comparator.comparingInt(Plato::getPorcionesDisponibles));

            if (platoDemo.isEmpty()) {
                log.info("No hay un plato de la cocinera demo con porciones suficientes; no se crean reservas de prueba.");
                return;
            }

            Plato plato = platoDemo.get();
            LocalDateTime ahora = LocalDateTime.now();

            Reserva paraConfirmar = Reserva.crear(plato, 1L, 2, MedioPago.NEQUI, "Sin cebolla, por favor", ahora);
            paraConfirmar.setId(RESERVA_PARA_CONFIRMAR);

            Reserva paraRechazar = Reserva.crear(plato, 2L, 1, MedioPago.EFECTIVO, null, ahora);
            paraRechazar.setId(RESERVA_PARA_RECHAZAR);

            Reserva porVencer = Reserva.crear(plato, 3L, 1, MedioPago.DAVIPLATA, "Timbrar en la portería", ahora.minusMinutes(8));
            porVencer.setId(RESERVA_POR_VENCER);

            plato.comprometerPorciones(PORCIONES_NECESARIAS);
            platoRepository.save(plato);
            reservaRepository.saveAll(List.of(paraConfirmar, paraRechazar, porVencer));

            log.info("Reservas de prueba (HU-12) creadas sobre el plato '{}'. Usa X-Cocinera-Id: {}",
                    plato.getNombre(), COCINERA_DEMO);
        } catch (RuntimeException e) {
            log.warn("No se pudieron crear las reservas de prueba: {}", e.getMessage());
        }
    }
}
