package com.ollacercana.core.validator;

import com.ollacercana.controller.handlers.exception.AutoReservaException;
import com.ollacercana.controller.handlers.exception.LimiteReservasPendientesException;
import com.ollacercana.controller.handlers.exception.MedioPagoNoAceptadoException;
import com.ollacercana.controller.handlers.exception.PorcionesInsuficientesException;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.core.validators.ReservaValidator;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaValidatorTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;
    @InjectMocks private ReservaValidator validator;

    private static final Long COMPRADOR_ID = 1L;
    private static final UUID COCINERA_ID = UUID.randomUUID();
    private Plato plato;

    @BeforeEach
    void setUp() {
        plato = Plato.builder()
                .id(UUID.randomUUID())
                .cocineraId(COCINERA_ID)
                .porcionesTotales(5)
                .porcionesComprometidas(1)
                .estado(EstadoPlato.ACTIVO)
                .fechaExpiracion(LocalDateTime.now().plusHours(1))
                .build();
    }

    @Test
    @DisplayName("validarParaCrear: Cocinera intenta reservar su propio plato lanza AutoReservaException (RN-14)")
    void validarParaCrear_autoReserva_lanzaExcepcion() {
        PerfilCocineraEntity perfilMismaCocinera = PerfilCocineraEntity.builder()
                .id(COCINERA_ID)
                .build();
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID))
                .thenReturn(Optional.of(perfilMismaCocinera));

        assertThrows(AutoReservaException.class,
                () -> validator.validarParaCrear(COMPRADOR_ID, plato, 1));
    }

    @Test
    @DisplayName("validarParaCrear: Comprador con 2 o más reservas pendientes lanza LimiteReservasPendientesException (RN-15)")
    void validarParaCrear_limitePendientesExcedido_lanzaExcepcion() {
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(2L);

        assertThrows(LimiteReservasPendientesException.class,
                () -> validator.validarParaCrear(COMPRADOR_ID, plato, 1));
    }

    @Test
    @DisplayName("validarParaCrear: Porciones solicitadas mayores a disponibles lanza PorcionesInsuficientesException (RN-03)")
    void validarParaCrear_porcionesInsuficientes_lanzaExcepcion() {
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(0L);

        assertThrows(PorcionesInsuficientesException.class,
                () -> validator.validarParaCrear(COMPRADOR_ID, plato, 5));
    }

    @Test
    @DisplayName("validarParaCrear: Comprador con perfil de cocinera diferente y porciones disponibles es exitoso")
    void validarParaCrear_compradorOtraCocineraValido_pasaExitoso() {
        PerfilCocineraEntity perfilOtraCocinera = PerfilCocineraEntity.builder()
                .id(UUID.randomUUID())
                .build();
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.of(perfilOtraCocinera));
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(1L);

        assertDoesNotThrow(() -> validator.validarParaCrear(COMPRADOR_ID, plato, 2));
    }

    @Test
    @DisplayName("OC-255 / OC-256: Reserva sin método de pago (null) es válida y se crea normalmente")
    void validarParaCrear_sinMedioPago_esValida() {
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(0L);

        assertDoesNotThrow(() -> validator.validarParaCrear(COMPRADOR_ID, plato, 1, null));
    }

    @Test
    @DisplayName("OC-255 / OC-256: Reserva con método de pago aceptado por la cocinera es válida")
    void validarParaCrear_conMedioPagoAceptado_esValida() {
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(0L);

        PerfilCocineraEntity perfilCocinera = PerfilCocineraEntity.builder()
                .id(COCINERA_ID)
                .mediosPago(List.of(MedioPago.NEQUI, MedioPago.EFECTIVO))
                .build();
        when(perfilCocineraRepository.findById(COCINERA_ID)).thenReturn(Optional.of(perfilCocinera));

        assertDoesNotThrow(() -> validator.validarParaCrear(COMPRADOR_ID, plato, 1, MedioPago.NEQUI));
    }

    @Test
    @DisplayName("OC-255 / OC-256: Reserva con método de pago NO aceptado por la cocinera lanza MedioPagoNoAceptadoException")
    void validarParaCrear_conMedioPagoNoAceptado_lanzaExcepcion() {
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(0L);

        PerfilCocineraEntity perfilCocinera = PerfilCocineraEntity.builder()
                .id(COCINERA_ID)
                .mediosPago(List.of(MedioPago.EFECTIVO))
                .build();
        when(perfilCocineraRepository.findById(COCINERA_ID)).thenReturn(Optional.of(perfilCocinera));

        assertThrows(MedioPagoNoAceptadoException.class,
                () -> validator.validarParaCrear(COMPRADOR_ID, plato, 1, MedioPago.DAVIPLATA));
    }

    @Test
    @DisplayName("validarParaCrear: un plato oculto no se puede reservar")
    void validarParaCrear_platoOculto_rechazaReserva() {
        plato.setEstado(EstadoPlato.OCULTO);
        assertThrows(ReglaDeNegocioException.class,
                () -> validator.validarParaCrear(COMPRADOR_ID, plato, 1));
    }
}