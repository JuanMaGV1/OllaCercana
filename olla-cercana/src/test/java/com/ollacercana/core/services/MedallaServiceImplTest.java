package com.ollacercana.core.services;

import com.ollacercana.core.models.Medalla;
import com.ollacercana.core.models.MedallaUsuario;
import com.ollacercana.core.models.enums.CodigoMedalla;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.services.impl.MedallaServiceImpl;
import com.ollacercana.persistence.entities.MedallaEntity;
import com.ollacercana.persistence.entities.MedallaUsuarioEntity;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.mappers.MedallaEntityMapper;
import com.ollacercana.persistence.mappers.MedallaUsuarioEntityMapper;
import com.ollacercana.persistence.repository.BalanceConjuntoProjection;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.MedallaRepository;
import com.ollacercana.persistence.repository.MedallaUsuarioRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedallaServiceImplTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private PerfilCocineraRepository perfilCocineraRepository;
    @Mock private CuentaRepository cuentaRepository;
    @Mock private MedallaRepository medallaRepository;
    @Mock private MedallaUsuarioRepository medallaUsuarioRepository;
    @Mock private MedallaEntityMapper medallaMapper;
    @Mock private MedallaUsuarioEntityMapper medallaUsuarioMapper;

    private MedallaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MedallaServiceImpl(
                reservaRepository, platoRepository, perfilCocineraRepository,
                cuentaRepository, medallaRepository, medallaUsuarioRepository,
                medallaMapper, medallaUsuarioMapper);
    }

    private MedallaEntity medallaEntity(CodigoMedalla codigo) {
        return MedallaEntity.builder().codigo(codigo).nombre("X").requisito("Y").build();
    }

    private Medalla medalla(CodigoMedalla codigo) {
        return Medalla.builder().codigo(codigo).nombre("X").requisito("Y").build();
    }

    // ============ evaluarVecinoFiel ============

    @Test
    @DisplayName("Otorga Vecino Fiel tras 3 entregas en el mismo mes")
    void evaluarVecinoFiel_otorga() {
        Long compradorId = 1L;
        UUID cocineraId = UUID.randomUUID();

        when(medallaUsuarioRepository.existsByUsuarioIdAndMedallaCodigo(compradorId, CodigoMedalla.VECINO_FIEL))
                .thenReturn(false);
        when(reservaRepository.contarCompletadasEnPeriodo(
                eq(compradorId), eq(cocineraId), eq(EstadoReserva.COMPLETADA), any(), any()))
                .thenReturn(3L);

        MedallaEntity me = medallaEntity(CodigoMedalla.VECINO_FIEL);
        when(medallaRepository.findById(CodigoMedalla.VECINO_FIEL)).thenReturn(Optional.of(me));
        when(medallaMapper.toDomain(me)).thenReturn(medalla(CodigoMedalla.VECINO_FIEL));

        MedallaUsuario domain = MedallaUsuario.builder()
                .usuarioId(compradorId).medalla(medalla(CodigoMedalla.VECINO_FIEL))
                .fechaOtorgada(LocalDateTime.now()).build();
        MedallaUsuarioEntity entity = MedallaUsuarioEntity.builder()
                .usuarioId(compradorId).medalla(me).fechaOtorgada(LocalDateTime.now()).build();

        when(medallaUsuarioMapper.toEntity(any(MedallaUsuario.class))).thenReturn(entity);
        when(medallaUsuarioRepository.save(entity)).thenReturn(entity);
        when(medallaUsuarioMapper.toDomain(entity)).thenReturn(domain);

        Optional<MedallaUsuario> resultado = service.evaluarVecinoFiel(compradorId, cocineraId, LocalDateTime.now());

        assertTrue(resultado.isPresent());
        assertEquals(compradorId, resultado.get().getUsuarioId());
        verify(medallaUsuarioRepository).save(any(MedallaUsuarioEntity.class));
    }

    @Test
    @DisplayName("No otorga si ya tenía la medalla")
    void evaluarVecinoFiel_yaTiene() {
        when(medallaUsuarioRepository.existsByUsuarioIdAndMedallaCodigo(1L, CodigoMedalla.VECINO_FIEL))
                .thenReturn(true);

        assertTrue(service.evaluarVecinoFiel(1L, UUID.randomUUID(), LocalDateTime.now()).isEmpty());
        verify(reservaRepository, never()).contarCompletadasEnPeriodo(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("No otorga con menos de 3 entregas")
    void evaluarVecinoFiel_pocasEntregas() {
        when(medallaUsuarioRepository.existsByUsuarioIdAndMedallaCodigo(1L, CodigoMedalla.VECINO_FIEL))
                .thenReturn(false);
        when(reservaRepository.contarCompletadasEnPeriodo(any(), any(), any(), any(), any()))
                .thenReturn(2L);

        assertTrue(service.evaluarVecinoFiel(1L, UUID.randomUUID(), LocalDateTime.now()).isEmpty());
    }

    // ============ calcularBalanceSemanal ============

    @Test
    @DisplayName("Otorga Olla Verde cuando se vende el 100%")
    void calcularBalanceSemanal_premia() {
        BalanceConjuntoProjection proj = mock(BalanceConjuntoProjection.class);
        when(proj.getConjunto()).thenReturn("Torres del Sol");
        when(proj.getPublicadas()).thenReturn(10L);
        when(proj.getVendidas()).thenReturn(10L);
        when(platoRepository.balancePorConjunto(any(), any())).thenReturn(List.of(proj));

        MedallaEntity me = medallaEntity(CodigoMedalla.CONJUNTO_OLLA_VERDE);
        when(medallaRepository.findById(CodigoMedalla.CONJUNTO_OLLA_VERDE)).thenReturn(Optional.of(me));
        when(medallaMapper.toDomain(me)).thenReturn(medalla(CodigoMedalla.CONJUNTO_OLLA_VERDE));

        PerfilCocineraEntity perfil = PerfilCocineraEntity.builder()
                .id(UUID.randomUUID())
                .cuenta(com.ollacercana.persistence.entities.CuentaEntity.builder().id(99L).build())
                .build();
        when(perfilCocineraRepository.findByConjuntoResidencial("Torres del Sol"))
                .thenReturn(List.of(perfil));

        when(medallaUsuarioRepository.findByUsuarioIdAndMedallaCodigo(99L, CodigoMedalla.CONJUNTO_OLLA_VERDE))
                .thenReturn(Optional.empty());
        when(medallaUsuarioMapper.toEntity(any(MedallaUsuario.class)))
                .thenReturn(MedallaUsuarioEntity.builder().build());
        when(medallaUsuarioRepository.save(any(MedallaUsuarioEntity.class)))
                .thenReturn(MedallaUsuarioEntity.builder().build());

        List<String> premiados = service.calcularBalanceSemanal(LocalDateTime.now());

        assertEquals(List.of("Torres del Sol"), premiados);
    }

        @Test
        @DisplayName("No premia si no se vendió el 100%")
        void calcularBalanceSemanal_noPremia() {
        BalanceConjuntoProjection proj = mock(BalanceConjuntoProjection.class);
        when(proj.getPublicadas()).thenReturn(10L);
        when(proj.getVendidas()).thenReturn(5L);
        when(platoRepository.balancePorConjunto(any(), any())).thenReturn(List.of(proj));

        assertTrue(service.calcularBalanceSemanal(LocalDateTime.now()).isEmpty());
        }
    // ============ listarVigentes ============

    @Test
    @DisplayName("Lista solo medallas vigentes")
    void listarVigentes() {
        when(cuentaRepository.existsById(1L)).thenReturn(true);

        MedallaEntity me = medallaEntity(CodigoMedalla.VECINO_FIEL);
        MedallaUsuarioEntity entity = MedallaUsuarioEntity.builder()
                .usuarioId(1L).medalla(me)
                .fechaOtorgada(LocalDateTime.now()).build();
        when(medallaUsuarioRepository.findVigentes(eq(1L), any())).thenReturn(List.of(entity));

        var result = service.listarVigentes(1L, LocalDateTime.now());

        assertEquals(1, result.size());
        assertEquals("VECINO_FIEL", result.get(0).getCodigo());
    }
}
