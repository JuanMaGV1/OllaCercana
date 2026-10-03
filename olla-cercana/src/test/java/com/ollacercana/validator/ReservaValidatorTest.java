package com.ollacercana.validator;

import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.domain.Plato;
import com.ollacercana.exception.AutoReservaException;
import com.ollacercana.exception.LimiteReservasPendientesException;
import com.ollacercana.exception.PorcionesInsuficientesException;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaValidatorTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private PerfilCocineraRepository perfilCocineraRepository;

    @InjectMocks
    private ReservaValidator validator;

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
                .build();
    }

    @Test
    @DisplayName("validarParaCrear: Cocinera intenta reservar su propio plato lanza AutoReservaException (RN-14)")
    void validarParaCrear_autoReserva_lanzaExcepcion() {
        PerfilCocinera perfilMismaCocinera = PerfilCocinera.builder().id(COCINERA_ID).build();
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.of(perfilMismaCocinera));

        assertThrows(AutoReservaException.class, () -> validator.validarParaCrear(COMPRADOR_ID, plato, 1));
    }

    @Test
    @DisplayName("validarParaCrear: Comprador con 2 o más reservas pendientes lanza LimiteReservasPendientesException (RN-15)")
    void validarParaCrear_limitePendientesExcedido_lanzaExcepcion() {
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(2L);

        assertThrows(LimiteReservasPendientesException.class, () -> validator.validarParaCrear(COMPRADOR_ID, plato, 1));
    }

    @Test
    @DisplayName("validarParaCrear: Porciones solicitadas mayores a disponibles lanza PorcionesInsuficientesException (RN-03)")
    void validarParaCrear_porcionesInsuficientes_lanzaExcepcion() {
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.empty());
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(0L);

        // Disponibles = 5 - 1 = 4. Solicitadas = 5
        assertThrows(PorcionesInsuficientesException.class, () -> validator.validarParaCrear(COMPRADOR_ID, plato, 5));
    }

    @Test
    @DisplayName("validarParaCrear: Comprador con perfil de cocinera diferente y porciones disponibles es exitoso")
    void validarParaCrear_compradorOtraCocineraValido_pasaExitoso() {
        PerfilCocinera perfilOtraCocinera = PerfilCocinera.builder().id(UUID.randomUUID()).build();
        when(perfilCocineraRepository.findByCuentaId(COMPRADOR_ID)).thenReturn(Optional.of(perfilOtraCocinera));
        when(reservaRepository.countByCompradorIdAndEstado(COMPRADOR_ID, EstadoReserva.PENDIENTE)).thenReturn(1L);

        assertDoesNotThrow(() -> validator.validarParaCrear(COMPRADOR_ID, plato, 2));
    }
}