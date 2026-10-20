package com.ollacercana.domain;

import com.ollacercana.core.models.*;
import com.ollacercana.core.models.enums.*;

import com.ollacercana.controller.handlers.exception.DecisionReservaInvalidaException;
import com.ollacercana.controller.handlers.exception.ReservaNoConfirmadaException;
import com.ollacercana.controller.handlers.exception.ReservaNoPendienteException;
import com.ollacercana.controller.handlers.exception.ReservaVencidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

   
                                                       
   
class ReservaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 1, 12, 0);

    private Plato plato() {
        return Plato.builder()
                .id(UUID.randomUUID())
                .cocineraId(UUID.randomUUID())
                .nombre("Ajiaco")
                .porcionesTotales(6)
                .porcionesComprometidas(0)
                .precioPorcion(new BigDecimal("16000"))
                .estado(EstadoPlato.ACTIVO)
                .build();
    }

    private Reserva reservaCreadaA(LocalDateTime fechaCreacion) {
        return Reserva.crear(plato(), 7L, 2, MedioPago.NEQUI, "Sin cebolla", fechaCreacion);
    }

    @Test
    void crear_debeQuedarPendienteConLimiteDe10MinutosYMontoCalculado() {
        Plato plato = plato();
        Reserva reserva = Reserva.crear(plato, 7L, 2, MedioPago.NEQUI, null, AHORA);

        assertNotNull(reserva.getId());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(plato.getId(), reserva.getPlatoId());
        assertEquals(plato.getCocineraId(), reserva.getCocineraId());
        assertEquals(AHORA.plusMinutes(10), reserva.getFechaLimiteConfirmacion());
        assertEquals(0, new BigDecimal("32000").compareTo(reserva.getMontoTotal()));
        assertFalse(reserva.isChatHabilitado());
        assertFalse(reserva.isRecordatorioEnviado());
    }

    @Test
    void crear_sinPlatoOConCantidadInvalida_debeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> Reserva.crear(null, 7L, 1, MedioPago.NEQUI, null, AHORA));
        Plato plato = plato();
        assertThrows(IllegalArgumentException.class,
                () -> Reserva.crear(plato, 7L, 0, MedioPago.NEQUI, null, AHORA));
    }

    @Test
    void confirmar_debePasarAConfirmadaYHabilitarChat() {
        Reserva reserva = reservaCreadaA(AHORA);

        reserva.confirmar(AHORA.plusMinutes(45), AHORA.plusMinutes(3));

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(AHORA.plusMinutes(45), reserva.getHoraEstimadaEntrega());
        assertEquals(AHORA.plusMinutes(3), reserva.getFechaDecision());
        assertTrue(reserva.isChatHabilitado());
    }

    @Test
    void confirmar_sinHoraEstimadaOConHoraPasada_debeLanzarExcepcion() {
        Reserva reserva = reservaCreadaA(AHORA);

        assertThrows(DecisionReservaInvalidaException.class, () -> reserva.confirmar(null, AHORA));
        assertThrows(DecisionReservaInvalidaException.class, () -> reserva.confirmar(AHORA.minusMinutes(1), AHORA));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void confirmar_despuesDeLos10Minutos_debeLanzarReservaVencida() {
        Reserva reserva = reservaCreadaA(AHORA);

        assertThrows(ReservaVencidaException.class,
                () -> reserva.confirmar(AHORA.plusHours(1), AHORA.plusMinutes(10)));
    }

    @Test
    void confirmar_reservaYaGestionada_debeLanzarReservaNoPendiente() {
        Reserva reserva = reservaCreadaA(AHORA);
        reserva.rechazar(MotivoRechazo.IMPREVISTO_PERSONAL, null, AHORA.plusMinutes(1));

        assertThrows(ReservaNoPendienteException.class,
                () -> reserva.confirmar(AHORA.plusHours(1), AHORA.plusMinutes(2)));
    }

    @Test
    void rechazar_debePasarARechazadaYGuardarMotivo() {
        Reserva reserva = reservaCreadaA(AHORA);

        reserva.rechazar(MotivoRechazo.INGREDIENTES_INSUFICIENTES, "   ", AHORA.plusMinutes(2));

        assertEquals(EstadoReserva.RECHAZADA, reserva.getEstado());
        assertEquals(MotivoRechazo.INGREDIENTES_INSUFICIENTES, reserva.getMotivoRechazo());
        assertNull(reserva.getComentarioRechazo());
        assertFalse(reserva.isChatHabilitado());
    }

    @Test
    void rechazar_conMotivoOtro_exigeComentario() {
        Reserva reserva = reservaCreadaA(AHORA);

        assertThrows(DecisionReservaInvalidaException.class,
                () -> reserva.rechazar(MotivoRechazo.OTRO, null, AHORA.plusMinutes(1)));
        assertThrows(DecisionReservaInvalidaException.class,
                () -> reserva.rechazar(MotivoRechazo.OTRO, "  ", AHORA.plusMinutes(1)));

        reserva.rechazar(MotivoRechazo.OTRO, "  Se me dañó la estufa ", AHORA.plusMinutes(1));
        assertEquals("Se me dañó la estufa", reserva.getComentarioRechazo());
    }

    @Test
    void rechazar_sinMotivoOConComentarioLargo_debeLanzarExcepcion() {
        Reserva reserva = reservaCreadaA(AHORA);
        String comentarioLargo = "a".repeat(151);

        assertThrows(DecisionReservaInvalidaException.class,
                () -> reserva.rechazar(null, null, AHORA.plusMinutes(1)));
        assertThrows(DecisionReservaInvalidaException.class,
                () -> reserva.rechazar(MotivoRechazo.OTRO, comentarioLargo, AHORA.plusMinutes(1)));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void expirar_conHoraLimitePasada_debePasarAExpirada() {
        Reserva reserva = reservaCreadaA(AHORA);

        reserva.expirar(AHORA.plusMinutes(10));

        assertEquals(EstadoReserva.EXPIRADA, reserva.getEstado());
    }

    @Test
    void expirar_antesDelLimiteOSiNoEstaPendiente_debeLanzarExcepcion() {
        Reserva vigente = reservaCreadaA(AHORA);
        assertThrows(IllegalStateException.class, () -> vigente.expirar(AHORA.plusMinutes(9)));

        Reserva confirmada = reservaCreadaA(AHORA);
        confirmada.confirmar(AHORA.plusHours(1), AHORA.plusMinutes(1));
        assertThrows(ReservaNoPendienteException.class, () -> confirmada.expirar(AHORA.plusMinutes(11)));
    }

    @Test
    void requiereRecordatorio_soloEntreLos7YLos10MinutosYUnaSolaVez() {
        Reserva reserva = reservaCreadaA(AHORA);

        assertFalse(reserva.requiereRecordatorio(AHORA.plusMinutes(6)));
        assertTrue(reserva.requiereRecordatorio(AHORA.plusMinutes(7)));
        assertFalse(reserva.requiereRecordatorio(AHORA.plusMinutes(10)));

        reserva.marcarRecordatorioEnviado();
        assertFalse(reserva.requiereRecordatorio(AHORA.plusMinutes(8)));
    }

    @Test
    void requiereRecordatorio_siYaRespondio_debeSerFalso() {
        Reserva reserva = reservaCreadaA(AHORA);
        reserva.confirmar(AHORA.plusHours(1), AHORA.plusMinutes(2));

        assertFalse(reserva.requiereRecordatorio(AHORA.plusMinutes(8)));
    }

    @Test
    void perteneceACocinera_debeCompararElIdDelPerfil() {
        Reserva reserva = reservaCreadaA(AHORA);

        assertTrue(reserva.perteneceACocinera(reserva.getCocineraId()));
        assertFalse(reserva.perteneceACocinera(UUID.randomUUID()));
    }

                                                                

    private Reserva reservaConfirmadaA(LocalDateTime confirmadaEn) {
        Reserva reserva = reservaCreadaA(confirmadaEn.minusMinutes(2));
        reserva.confirmar(confirmadaEn.plusHours(1), confirmadaEn);
        return reserva;
    }

    @Test
    void confirmar_debeActivarElChat() {
        Reserva reserva = reservaCreadaA(AHORA.minusMinutes(1));
        assertEquals(EstadoChat.INACTIVO, reserva.getEstadoChat());

        reserva.confirmar(AHORA.plusHours(1), AHORA);

        assertEquals(EstadoChat.ACTIVO, reserva.getEstadoChat());
    }

    @Test
    void completar_debePasarACompletadaConChatEnSoloLecturaYCalificacionHabilitada() {
        Reserva reserva = reservaConfirmadaA(AHORA);
        LocalDateTime cierre = AHORA.plusHours(2);

        reserva.completar("  Todo bien  ", cierre);

        assertEquals(EstadoReserva.COMPLETADA, reserva.getEstado());
        assertEquals(EstadoChat.SOLO_LECTURA, reserva.getEstadoChat());
        assertTrue(reserva.isCalificacionHabilitada());
        assertEquals("Todo bien", reserva.getComentarioCierre());
        assertEquals(cierre, reserva.getFechaCompletada());
    }

    @Test
    void completar_reservaNoConfirmada_debeLanzarReservaNoConfirmada() {
        Reserva pendiente = reservaCreadaA(AHORA);

        assertThrows(ReservaNoConfirmadaException.class, () -> pendiente.completar(null, AHORA));
        assertEquals(EstadoReserva.PENDIENTE, pendiente.getEstado());
        assertFalse(pendiente.isCalificacionHabilitada());
    }

    @Test
    void completar_comentarioMayorA150_debeLanzarIllegalArgument() {
        Reserva reserva = reservaConfirmadaA(AHORA);
        String largo = "x".repeat(Reserva.MAX_CARACTERES_COMENTARIO + 1);

        assertThrows(IllegalArgumentException.class, () -> reserva.completar(largo, AHORA));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void cierreAutomaticoVencido_soloDesdeLas24HorasDeLaConfirmacion() {
        Reserva reserva = reservaConfirmadaA(AHORA);

        assertFalse(reserva.cierreAutomaticoVencido(AHORA.plusHours(23).plusMinutes(59)));
        assertTrue(reserva.cierreAutomaticoVencido(AHORA.plusHours(24)));
        assertTrue(reserva.cierreAutomaticoVencido(AHORA.plusHours(30)));
    }

    @Test
    void cierreAutomaticoVencido_reservaNoConfirmada_esFalso() {
        assertFalse(reservaCreadaA(AHORA.minusDays(3)).cierreAutomaticoVencido(AHORA));
    }
}