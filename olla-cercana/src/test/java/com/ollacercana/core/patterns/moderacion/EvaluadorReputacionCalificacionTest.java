package com.ollacercana.core.patterns.moderacion;

import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.repository.CalificacionRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluadorReputacionCalificacionTest {

    @Mock private CalificacionRepository calificacionRepository;
    @Mock private PerfilCocineraRepository perfilRepository;

    @InjectMocks private EvaluadorReputacionCalificacion evaluador;

    private final UUID cocineraId = UUID.randomUUID();

    @Test
    @DisplayName("Menos de 5 reseñas no evalúa ni pausa a la cocinera")
    void evaluar_menosDeCincoResenas_noHaceNada() {
        when(calificacionRepository.contarTotal(cocineraId)).thenReturn(4L);

        evaluador.evaluar(cocineraId);

        verify(calificacionRepository, never()).promedioPorCocinera(any());
        verify(perfilRepository, never()).findById(any());
    }

    @Test
    @DisplayName("5 o más reseñas con promedio bajo (< 3.5) pausa el perfil")
    void evaluar_promedioBajo_pausaPerfil() {
        when(calificacionRepository.contarTotal(cocineraId)).thenReturn(5L);
        when(calificacionRepository.promedioPorCocinera(cocineraId)).thenReturn(3.2);

        PerfilCocineraEntity perfil = PerfilCocineraEntity.builder()
                .id(cocineraId)
                .pausada(false)
                .build();
        when(perfilRepository.findById(cocineraId)).thenReturn(Optional.of(perfil));

        evaluador.evaluar(cocineraId);

        verify(perfilRepository).save(argThat(PerfilCocineraEntity::isPausada));
    }

    @Test
    @DisplayName("5 o más reseñas con promedio alto (>= 3.5) no pausa el perfil")
    void evaluar_promedioAlto_noPausa() {
        when(calificacionRepository.contarTotal(cocineraId)).thenReturn(10L);
        when(calificacionRepository.promedioPorCocinera(cocineraId)).thenReturn(4.5);

        PerfilCocineraEntity perfil = PerfilCocineraEntity.builder()
                .id(cocineraId)
                .pausada(false)
                .build();
        when(perfilRepository.findById(cocineraId)).thenReturn(Optional.of(perfil));

        evaluador.evaluar(cocineraId);

        verify(perfilRepository, never()).save(any());
    }

    @Test
    @DisplayName("Perfil ya pausado no se vuelve a guardar")
    void evaluar_yaPausado_noGuarda() {
        when(calificacionRepository.contarTotal(cocineraId)).thenReturn(6L);
        when(calificacionRepository.promedioPorCocinera(cocineraId)).thenReturn(3.0);

        PerfilCocineraEntity perfil = PerfilCocineraEntity.builder()
                .id(cocineraId)
                .pausada(true)
                .build();
        when(perfilRepository.findById(cocineraId)).thenReturn(Optional.of(perfil));

        evaluador.evaluar(cocineraId);

        verify(perfilRepository, never()).save(any());
    }
}