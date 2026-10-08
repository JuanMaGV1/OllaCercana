package com.ollacercana.service;

import com.ollacercana.domain.CodigoMedalla;
import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.EstadoReserva;
import com.ollacercana.domain.Medalla;
import com.ollacercana.domain.MedallaUsuario;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.dto.response.MedallaUsuarioResponseDTO;
import com.ollacercana.exception.CuentaNoEncontradaException;
import com.ollacercana.repository.BalanceConjuntoProjection;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.MedallaRepository;
import com.ollacercana.repository.MedallaUsuarioRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReservaRepository;
import com.ollacercana.service.impl.MedallaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedallaServiceImplTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;
    @Mock private CuentaRepository cuentaRepository;
    @Mock private MedallaRepository medallaRepository;
    @Mock private MedallaUsuarioRepository medallaUsuarioRepository;

    private MedallaServiceImpl service;

    private final Long compradorId = 7L;
    private final UUID cocineraId = UUID.randomUUID();
    private final UUID otraCocineraId = UUID.randomUUID();
    private final LocalDateTime fecha = LocalDateTime.of(2026, 10, 15, 12, 0);

    private final Medalla vecinoFiel = Medalla.builder()
            .codigo(CodigoMedalla.VECINO_FIEL).nombre("Vecino Fiel").requisito("3 reservas en el mes").build();
    private final Medalla ollaVerde = Medalla.builder()
            .codigo(CodigoMedalla.CONJUNTO_OLLA_VERDE).nombre("Conjunto Olla Verde").requisito("100% vendido").build();

    @BeforeEach
    void setUp() {
        service = new MedallaServiceImpl(reservaRepository, platoRepository, perfilCocineraRepository,
                cuentaRepository, medallaRepository, medallaUsuarioRepository);
    }

    private BalanceConjuntoProjection balance(String conjunto, Long publicadas, Long vendidas) {
        return new BalanceConjuntoProjection() {
            @Override public String getConjunto() { return conjunto; }
            @Override public Long getPublicadas() { return publicadas; }
            @Override public Long getVendidas() { return vendidas; }
        };
    }

    private PerfilCocinera perfil(String conjunto, Long cuentaId) {
        Cuenta cuenta = cuentaId == null ? null : Cuenta.builder().id(cuentaId).build();
        return PerfilCocinera.builder().id(UUID.randomUUID()).conjuntoResidencial(conjunto).cuenta(cuenta).build();
    }

    // ---------- OC-279: Vecino Fiel ----------

    @Test
    @DisplayName("3 entregas del mes con la misma cocinera: se otorga VECINO_FIEL sin vencimiento")
    void tresEntregasSeOtorga() {
        when(medallaUsuarioRepository.existsByUsuarioIdAndMedallaCodigo(compradorId, CodigoMedalla.VECINO_FIEL)).thenReturn(false);
        when(reservaRepository.contarCompletadasEnPeriodo(eq(compradorId), eq(cocineraId), eq(EstadoReserva.COMPLETADA), any(), any()))
                .thenReturn(3L);
        when(medallaRepository.findById(CodigoMedalla.VECINO_FIEL)).thenReturn(Optional.of(vecinoFiel));
        when(medallaUsuarioRepository.save(any(MedallaUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<MedallaUsuario> otorgada = service.evaluarVecinoFiel(compradorId, cocineraId, fecha);

        assertTrue(otorgada.isPresent());
        assertEquals(CodigoMedalla.VECINO_FIEL, otorgada.get().getMedalla().getCodigo());
        assertEquals(compradorId, otorgada.get().getUsuarioId());
        assertNull(otorgada.get().getVigenteHasta());
    }

    @Test
    @DisplayName("2 entregas del mes: no se otorga")
    void dosEntregasNoSeOtorga() {
        when(medallaUsuarioRepository.existsByUsuarioIdAndMedallaCodigo(compradorId, CodigoMedalla.VECINO_FIEL)).thenReturn(false);
        when(reservaRepository.contarCompletadasEnPeriodo(eq(compradorId), eq(cocineraId), eq(EstadoReserva.COMPLETADA), any(), any()))
                .thenReturn(2L);

        assertTrue(service.evaluarVecinoFiel(compradorId, cocineraId, fecha).isEmpty());
        verify(medallaUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("3 entregas con cocineras distintas: no se otorga (el conteo es por cocinera)")
    void entregasConCocinerasDistintasNoSeOtorga() {
        when(medallaUsuarioRepository.existsByUsuarioIdAndMedallaCodigo(compradorId, CodigoMedalla.VECINO_FIEL)).thenReturn(false);
        // Cada cocinera solo tiene 1 entrega completada con este comprador.
        when(reservaRepository.contarCompletadasEnPeriodo(eq(compradorId), eq(cocineraId), eq(EstadoReserva.COMPLETADA), any(), any()))
                .thenReturn(1L);
        when(reservaRepository.contarCompletadasEnPeriodo(eq(compradorId), eq(otraCocineraId), eq(EstadoReserva.COMPLETADA), any(), any()))
                .thenReturn(1L);

        assertTrue(service.evaluarVecinoFiel(compradorId, cocineraId, fecha).isEmpty());
        assertTrue(service.evaluarVecinoFiel(compradorId, otraCocineraId, fecha).isEmpty());
        verify(medallaUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("La insignia no se otorga dos veces")
    void noSeOtorgaDosVeces() {
        when(medallaUsuarioRepository.existsByUsuarioIdAndMedallaCodigo(compradorId, CodigoMedalla.VECINO_FIEL)).thenReturn(true);

        assertTrue(service.evaluarVecinoFiel(compradorId, cocineraId, fecha).isEmpty());
        verify(medallaUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Al cambiar de mes el conteo arranca en cero")
    void conteoArrancaEnCeroAlCambiarDeMes() {
        LocalDateTime primerDiaNoviembre = LocalDateTime.of(2026, 11, 1, 9, 0);
        when(medallaUsuarioRepository.existsByUsuarioIdAndMedallaCodigo(compradorId, CodigoMedalla.VECINO_FIEL)).thenReturn(false);
        // En noviembre solo hay 1 entrega, aunque en octubre hubo más.
        when(reservaRepository.contarCompletadasEnPeriodo(compradorId, cocineraId, EstadoReserva.COMPLETADA,
                LocalDateTime.of(2026, 11, 1, 0, 0), LocalDateTime.of(2026, 12, 1, 0, 0)))
                .thenReturn(1L);

        assertTrue(service.evaluarVecinoFiel(compradorId, cocineraId, primerDiaNoviembre).isEmpty());
        verify(reservaRepository).contarCompletadasEnPeriodo(compradorId, cocineraId, EstadoReserva.COMPLETADA,
                LocalDateTime.of(2026, 11, 1, 0, 0), LocalDateTime.of(2026, 12, 1, 0, 0));
    }

    // ---------- OC-299: Conjunto Olla Verde ----------

    @Test
    @DisplayName("Conjunto con 100% vendido: sus cocineras reciben la medalla con vencimiento a 7 días")
    void conjunto100PorCientoRecibeMedalla() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 12, 0, 0);
        when(platoRepository.balancePorConjunto(ahora.minusDays(7), ahora))
                .thenReturn(List.of(balance("Torres del Parque", 10L, 10L)));
        when(perfilCocineraRepository.findByConjuntoResidencial("Torres del Parque"))
                .thenReturn(List.of(perfil("Torres del Parque", 11L), perfil("Torres del Parque", 12L)));
        when(medallaRepository.findById(CodigoMedalla.CONJUNTO_OLLA_VERDE)).thenReturn(Optional.of(ollaVerde));
        when(medallaUsuarioRepository.findByUsuarioIdAndMedallaCodigo(any(), eq(CodigoMedalla.CONJUNTO_OLLA_VERDE)))
                .thenReturn(Optional.empty());

        List<String> premiados = service.calcularBalanceSemanal(ahora);

        assertEquals(List.of("Torres del Parque"), premiados);
        ArgumentCaptor<MedallaUsuario> captor = ArgumentCaptor.forClass(MedallaUsuario.class);
        verify(medallaUsuarioRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        for (MedallaUsuario medalla : captor.getAllValues()) {
            assertEquals(CodigoMedalla.CONJUNTO_OLLA_VERDE, medalla.getMedalla().getCodigo());
            assertEquals(ahora.plusDays(7), medalla.getVigenteHasta());
        }
    }

    @Test
    @DisplayName("Conjunto con menos del 100% vendido: no recibe la medalla")
    void conjuntoConMenosDelCienNoRecibe() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 12, 0, 0);
        when(platoRepository.balancePorConjunto(ahora.minusDays(7), ahora))
                .thenReturn(List.of(balance("Torres del Parque", 10L, 8L)));

        assertTrue(service.calcularBalanceSemanal(ahora).isEmpty());
        verify(medallaUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Conjunto sin publicaciones en la semana: no recibe la medalla")
    void conjuntoSinPublicacionesNoRecibe() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 12, 0, 0);
        when(platoRepository.balancePorConjunto(ahora.minusDays(7), ahora))
                .thenReturn(List.of(balance("Torres del Parque", 0L, 0L)));

        assertTrue(service.calcularBalanceSemanal(ahora).isEmpty());
        verify(medallaUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Si la cocinera ya tenía la medalla, se renueva el vencimiento en vez de duplicarla")
    void medallaExistenteSeRenueva() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 12, 0, 0);
        MedallaUsuario anterior = MedallaUsuario.builder()
                .usuarioId(11L).medalla(ollaVerde)
                .fechaOtorgada(ahora.minusDays(7)).vigenteHasta(ahora).build();
        when(platoRepository.balancePorConjunto(ahora.minusDays(7), ahora))
                .thenReturn(List.of(balance("Torres del Parque", 6L, 6L)));
        when(perfilCocineraRepository.findByConjuntoResidencial("Torres del Parque"))
                .thenReturn(List.of(perfil("Torres del Parque", 11L)));
        when(medallaRepository.findById(CodigoMedalla.CONJUNTO_OLLA_VERDE)).thenReturn(Optional.of(ollaVerde));
        when(medallaUsuarioRepository.findByUsuarioIdAndMedallaCodigo(11L, CodigoMedalla.CONJUNTO_OLLA_VERDE))
                .thenReturn(Optional.of(anterior));

        service.calcularBalanceSemanal(ahora);

        assertEquals(ahora.plusDays(7), anterior.getVigenteHasta());
        verify(medallaUsuarioRepository).save(anterior);
    }

    // ---------- OC-279: endpoint de medallas ----------

    @Test
    @DisplayName("Lista las medallas vigentes del usuario")
    void listaMedallasVigentes() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 15, 12, 0);
        when(cuentaRepository.existsById(compradorId)).thenReturn(true);
        when(medallaUsuarioRepository.findVigentes(compradorId, ahora)).thenReturn(List.of(
                MedallaUsuario.builder().usuarioId(compradorId).medalla(vecinoFiel).fechaOtorgada(ahora).build()));

        List<MedallaUsuarioResponseDTO> medallas = service.listarVigentes(compradorId, ahora);

        assertEquals(1, medallas.size());
        assertEquals("VECINO_FIEL", medallas.get(0).getCodigo());
        assertEquals("Vecino Fiel", medallas.get(0).getNombre());
        assertNull(medallas.get(0).getVigenteHasta());
    }

    @Test
    @DisplayName("Usuario inexistente: lanza CuentaNoEncontradaException")
    void usuarioInexistente() {
        when(cuentaRepository.existsById(99L)).thenReturn(false);

        assertThrows(CuentaNoEncontradaException.class, () -> service.listarVigentes(99L, fecha));
    }
}
