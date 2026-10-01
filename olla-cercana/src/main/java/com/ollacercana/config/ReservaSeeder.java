package com.ollacercana.config;

import com.ollacercana.mapper.PlatoEntityMapper;
import com.ollacercana.mapper.ReservaEntityMapper;
import com.ollacercana.model.domain.*;
import com.ollacercana.persistence.entity.PlatoEntity;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(3)
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class ReservaSeeder implements CommandLineRunner {

    static final UUID COCINERA_DEMO = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID RESERVA_PARA_CONFIRMAR = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    static final UUID RESERVA_PARA_RECHAZAR = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    static final UUID RESERVA_POR_VENCER = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private static final UUID COMPRADOR_1 = UUID.fromString("11111111-2222-3333-4444-555555555551");
    private static final UUID COMPRADOR_2 = UUID.fromString("11111111-2222-3333-4444-555555555552");
    private static final UUID COMPRADOR_3 = UUID.fromString("11111111-2222-3333-4444-555555555553");

    private static final int PORCIONES_NECESARIAS = 4;

    private final ReservaRepository reservaRepository;
    private final PlatoRepository platoRepository;
    private final PlatoEntityMapper platoMapper;
    private final ReservaEntityMapper reservaMapper;

    @Override
    public void run(String... args) {
        if (reservaRepository.count() > 0) return;

        try {
            Optional<PlatoEntity> platoEntityOpt = platoRepository
                    .findByCocineraIdAndEstado(COCINERA_DEMO, EstadoPlato.ACTIVO)
                    .stream()
                    .filter(p -> (p.getPorcionesTotales() - p.getPorcionesComprometidas()) >= PORCIONES_NECESARIAS)
                    .findFirst();

            if (platoEntityOpt.isEmpty()) {
                log.info("No hay plato de la cocinera demo con porciones suficientes.");
                return;
            }

            Plato plato = platoMapper.toDomain(platoEntityOpt.get());
            LocalDateTime ahora = LocalDateTime.now();

            Reserva paraConfirmar = Reserva.crear(plato, COMPRADOR_1, 2, MedioPago.NEQUI, "Sin cebolla", ahora);
            paraConfirmar.setId(RESERVA_PARA_CONFIRMAR);

            Reserva paraRechazar = Reserva.crear(plato, COMPRADOR_2, 1, MedioPago.EFECTIVO, null, ahora);
            paraRechazar.setId(RESERVA_PARA_RECHAZAR);

            Reserva porVencer = Reserva.crear(plato, COMPRADOR_3, 1, MedioPago.DAVIPLATA, "Timbrar en portería", ahora.minusMinutes(8));
            porVencer.setId(RESERVA_POR_VENCER);

            plato.comprometerPorciones(PORCIONES_NECESARIAS);
            platoRepository.save(platoMapper.toEntity(plato));

            reservaRepository.saveAll(List.of(
                    reservaMapper.toEntity(paraConfirmar),
                    reservaMapper.toEntity(paraRechazar),
                    reservaMapper.toEntity(porVencer)
            ));

            log.info("Reservas de prueba creadas para el plato '{}'.", plato.getNombre());
        } catch (RuntimeException e) {
            log.warn("No se pudieron crear reservas de prueba: {}", e.getMessage());
        }
    }
}