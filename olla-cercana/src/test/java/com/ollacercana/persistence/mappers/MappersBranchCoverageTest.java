package com.ollacercana.persistence.mappers;

import com.ollacercana.controller.dtos.request.RegistroRequestDTO;
import com.ollacercana.controller.mappers.*;
import com.ollacercana.core.models.*;
import com.ollacercana.core.models.enums.*;
import com.ollacercana.persistence.document.EventoReservaDocument;
import com.ollacercana.persistence.document.NotificacionDocument;
import com.ollacercana.persistence.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MappersBranchCoverageTest {

    private final CuentaEntityMapper cuentaEntityMapper = new CuentaEntityMapperImpl();
    private final PerfilCocineraPersistenceMapper perfilPersistenceMapper = new PerfilCocineraPersistenceMapperImpl();
    private final CodigoOTPEntityMapper otpMapper = new CodigoOTPEntityMapperImpl();
    private final NotificacionDocumentMapper notifMapper = new NotificacionDocumentMapperImpl();
    private final EventoMapper eventoMapper = new EventoMapperImpl();
    private final CuentaMapper cuentaMapper = new CuentaMapperImpl();
    private final PlatoMapper platoMapper = new PlatoMapperImpl();
    private final ReservaMapper reservaMapper = new ReservaMapperImpl();
    private final CalificacionEntityMapper calificacionEntityMapper = new CalificacionEntityMapper();

    @Test
    @DisplayName("Cubre branches de mappers de entidades Cuenta, Perfil y OTP")
    void testCuentaPerfilYOtpMappers() {
        assertNull(cuentaEntityMapper.toEntity(null));
        assertNull(cuentaEntityMapper.toDomain(null));
        assertNull(cuentaEntityMapper.toDomainList(null));
        assertTrue(cuentaEntityMapper.toDomainList(List.of()).isEmpty());

        assertNull(perfilPersistenceMapper.toEntity(null));
        PerfilCocinera perfil = PerfilCocinera.builder().id(UUID.randomUUID()).build();
        assertNotNull(perfilPersistenceMapper.toEntity(perfil));

        assertNull(otpMapper.toEntity(null));
        assertNull(otpMapper.toDomain(null));
        CodigoOTP otp = CodigoOTP.builder().codigo("1234").build();
        assertNotNull(otpMapper.toEntity(otp));
        assertNotNull(otpMapper.toDomain(new CodigoOTPEntity()));
    }

    @Test
    @DisplayName("Cubre branches de mappers Document MongoDB (Notificacion y Evento)")
    void testMongoDocumentMappers() {
        assertNull(notifMapper.toDocument(null));
        assertNull(notifMapper.toDomain(null));
        assertNotNull(notifMapper.toDocument(Notificacion.builder().titulo("T").build()));
        assertNotNull(notifMapper.toDomain(new NotificacionDocument()));

        assertNull(eventoMapper.toDocument(null));
        assertNull(eventoMapper.toDomain(null));
        EventoReserva eventoSinPayload = EventoReserva.builder().payload(null).build();
        assertNotNull(eventoMapper.toDocument(eventoSinPayload));
        EventoReserva eventoConPayload = EventoReserva.builder().payload(Map.of("k", "v")).build();
        assertNotNull(eventoMapper.toDocument(eventoConPayload));
        assertNotNull(eventoMapper.toDomain(new EventoReservaDocument()));
    }

    @Test
    @DisplayName("Cubre branches de CalificacionEntityMapper y CuentaMapper")
    void testCalificacionYCuentaMapper() {
        assertNotNull(calificacionEntityMapper.toDomainList(null));
        assertTrue(calificacionEntityMapper.toDomainList(null).isEmpty());
        assertTrue(calificacionEntityMapper.toDomainList(List.of()).isEmpty());

        assertNull(cuentaMapper.toDomain(null));
        assertNull(cuentaMapper.toResponseDTO(null));
        assertTrue(cuentaMapper.rolToRolesSet(null).isEmpty());
        assertEquals(Set.of(Rol.ADMIN), cuentaMapper.rolToRolesSet(Rol.ADMIN));
        assertNull(cuentaMapper.rolesSetToRol(null));
        assertNull(cuentaMapper.rolesSetToRol(Set.of()));
        assertEquals(Rol.COCINERA, cuentaMapper.rolesSetToRol(Set.of(Rol.COCINERA)));

        RegistroRequestDTO regReq = RegistroRequestDTO.builder().nombre("N").rol(Rol.COMPRADOR).build();
        assertNotNull(cuentaMapper.toDomain(regReq));
        Cuenta c = Cuenta.builder().identidad(Identidad.builder().correo("c@c.com").build()).roles(Set.of(Rol.COMPRADOR)).build();
        assertNotNull(cuentaMapper.toResponseDTO(c));
    }

    @Test
    @DisplayName("Cubre branches de PlatoMapper y ReservaMapper evitando NPE")
    void testPlatoYReservaMappers() {
        assertNull(platoMapper.toDomain(null));
        assertNull(platoMapper.toResponse(null));
        assertNull(platoMapper.toResponseList(null));
        Plato p = Plato.builder()
                .id(UUID.randomUUID())
                .nombre("P")
                .porcionesTotales(5)
                .porcionesComprometidas(1)
                .precioPorcion(BigDecimal.TEN)
                .build();
        assertNotNull(platoMapper.toResponse(p));
        assertNotNull(platoMapper.toResponseList(List.of(p)));

        assertNull(reservaMapper.toDomain(null));
        assertNull(reservaMapper.toResponse(null));
        assertNull(reservaMapper.toResponseDTO(null, null, null));
        assertNull(reservaMapper.toResponseList(null));
        Reserva r = Reserva.builder()
                .id(UUID.randomUUID())
                .cantidadPorciones(2)
                .montoTotal(BigDecimal.ONE)
                .fechaLimiteConfirmacion(LocalDateTime.now())
                .build();
        assertNotNull(reservaMapper.toResponse(r));
        assertNotNull(reservaMapper.toResponseDTO(r, "Plato", "Conjunto"));
        assertNotNull(reservaMapper.toResponseList(List.of(r)));
    }
}