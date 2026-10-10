package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.*;
import com.ollacercana.core.models.enums.*;
import com.ollacercana.persistence.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EntityMappersTest {

    private final PlatoEntityMapper platoMapper = new PlatoEntityMapper();
    private final CuentaEntityMapper cuentaMapper = new CuentaEntityMapperImpl(); // MapStruct generado
    private final ReservaEntityMapper reservaMapper = new ReservaEntityMapperImpl();
    private final ReporteEntityMapper reporteMapper = new ReporteEntityMapper();
    private final CalificacionEntityMapper calificacionMapper = new CalificacionEntityMapper();

    // ─────────── Plato ───────────
    @Test
    @DisplayName("PlatoEntityMapper: dominio→entidad→dominio es estable")
    void platoMapper_roundtrip() {
        UUID id = UUID.randomUUID();
        Plato p = Plato.builder()
                .id(id).cocineraId(UUID.randomUUID()).nombre("Ajiaco").descripcion("d")
                .fotoUrl("f").tipoComida(TipoComida.ALMUERZO)
                .restricciones(List.of(RestriccionAlimentaria.SIN_GLUTEN))
                .porcionesTotales(5).porcionesComprometidas(1)
                .precioPorcion(new BigDecimal("16000")).estado(EstadoPlato.ACTIVO)
                .fechaPublicacion(LocalDateTime.now()).fechaExpiracion(LocalDateTime.now().plusHours(4))
                .latitud(4.6).longitud(-74.0).puntoEntrega("portería").version(0).build();

        PlatoEntity entity = platoMapper.toEntity(p);
        assertEquals("Ajiaco", entity.getNombre());
        assertEquals(4, entity.getPorcionesTotales() - entity.getPorcionesComprometidas());

        Plato back = platoMapper.toDomain(entity);
        assertEquals(p.getNombre(), back.getNombre());
        assertEquals(p.getPrecioPorcion(), back.getPrecioPorcion());
    }

    @Test
    @DisplayName("PlatoEntityMapper: null devuelve null")
    void platoMapper_null() {
        assertNull(platoMapper.toEntity(null));
        assertNull(platoMapper.toDomain(null));
        assertTrue(platoMapper.toDomainList(null).isEmpty());
    }

    // ─────────── Cuenta ───────────
    @Test
    @DisplayName("CuentaEntityMapper: mapea identidad, credenciales y roles")
    void cuentaMapper_roundtrip() {
        Cuenta c = Cuenta.builder()
                .id(1L)
                .identidad(new Identidad("Ana", "a@a.com", "3000000000", null))
                .credenciales(new Credenciales("hash", null, true))
                .roles(Set.of(Rol.COCINERA)).estado(EstadoCuenta.ACTIVO).build();

        CuentaEntity entity = cuentaMapper.toEntity(c);
        assertEquals("a@a.com", entity.getIdentidad().getCorreo());
        assertTrue(entity.getRoles().contains(Rol.COCINERA));
    }

    // ─────────── Reserva ───────────
    @Test
    @DisplayName("ReservaEntityMapper: mapea todos los campos")
    void reservaMapper_roundtrip() {
        Reserva r = Reserva.builder()
                .id(UUID.randomUUID()).platoId(UUID.randomUUID()).cocineraId(UUID.randomUUID())
                .compradorId(2L).cantidadPorciones(2).montoTotal(new BigDecimal("32000"))
                .medioPago(MedioPago.NEQUI).estado(EstadoReserva.PENDIENTE)
                .notaComprador("sin cebolla").fechaCreacion(LocalDateTime.now())
                .fechaLimiteConfirmacion(LocalDateTime.now().plusMinutes(10))
                .estadoChat(EstadoChat.INACTIVO).build();

        ReservaEntity e = reservaMapper.toEntity(r);
        assertEquals(2, e.getCantidadPorciones());
        assertEquals(EstadoReserva.PENDIENTE, e.getEstado());

        Reserva back = reservaMapper.toDomain(e);
        assertEquals(r.getMontoTotal(), back.getMontoTotal());
    }

    // ─────────── Reporte ───────────
    @Test
    @DisplayName("ReporteEntityMapper: roundtrip y null")
    void reporteMapper_roundtrip() {
        Reporte r = Reporte.builder()
                .id(UUID.randomUUID()).objetivo(ObjetivoReporte.PLATO)
                .objetivoId(UUID.randomUUID()).motivo(MotivoReporte.CONTENIDO_INAPROPIADO)
                .reportanteId(1L).estado(EstadoReporte.ABIERTO)
                .fechaCreacion(LocalDateTime.now()).build();

        assertEquals(MotivoReporte.CONTENIDO_INAPROPIADO, reporteMapper.toDomain(reporteMapper.toEntity(r)).getMotivo());
        assertNull(reporteMapper.toEntity(null));
    }

    // ─────────── Calificación ───────────
    @Test
    @DisplayName("CalificacionEntityMapper: mapea estado y fechas")
    void calificacionMapper_roundtrip() {
        Calificacion c = Calificacion.builder()
                .id(UUID.randomUUID()).reservaId(UUID.randomUUID()).compradorId(1L)
                .cocineraId(UUID.randomUUID()).estrellas(5).comentario("ok")
                .estado(EstadoCalificacion.PUBLICADA)
                .fechaCreacion(LocalDateTime.now())
                .fechaPublicacion(LocalDateTime.now())
                .fechaLimitePublicacion(LocalDateTime.now().plusHours(72)).build();

        CalificacionEntity e = calificacionMapper.toEntity(c);
        assertEquals(EstadoCalificacion.PUBLICADA, e.getEstado());
        assertEquals("ok", e.getComentario());
    }
}