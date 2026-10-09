// test/.../service/PerfilCocineraServiceImplTest.java
package com.ollacercana.service;

import com.ollacercana.controller.handlers.exception.CocineraNoEncontradaException;
import com.ollacercana.core.services.impl.PerfilCocineraQueryServiceImpl;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilCocineraQueryServiceImplTest {   // ← nombre CORRECTO

    @Mock private PerfilCocineraRepository repository;
    @InjectMocks private PerfilCocineraQueryServiceImpl service;

    private final UUID cocineraId = UUID.randomUUID();

    @Test
    @DisplayName("estaVerificada retorna true o false según el perfil")
    void estaVerificada_existente() {
        PerfilCocineraEntity entity = PerfilCocineraEntity.builder().verificada(true).build();
        when(repository.findById(cocineraId)).thenReturn(Optional.of(entity));

        assertTrue(service.estaVerificada(cocineraId));
    }

    @Test
    @DisplayName("estaVerificada con cocinera inexistente lanza CocineraNoEncontradaException")
    void estaVerificada_noExiste_lanzaExcepcion() {
        when(repository.findById(cocineraId)).thenReturn(Optional.empty());

        assertThrows(CocineraNoEncontradaException.class, () -> service.estaVerificada(cocineraId));
    }

    @Test
    @DisplayName("estaPausada retorna el estado del perfil")
    void estaPausada_existente() {
        PerfilCocineraEntity entity = PerfilCocineraEntity.builder().pausada(true).build();
        when(repository.findById(cocineraId)).thenReturn(Optional.of(entity));

        assertTrue(service.estaPausada(cocineraId));
    }

    @Test
    @DisplayName("estaPausada con cocinera inexistente lanza CocineraNoEncontradaException")
    void estaPausada_noExiste_lanzaExcepcion() {
        when(repository.findById(cocineraId)).thenReturn(Optional.empty());

        assertThrows(CocineraNoEncontradaException.class, () -> service.estaPausada(cocineraId));
    }

    @Test
    @DisplayName("OC-253 / OC-256: Guardar y retornar métodos de pago configurados en el perfil")
    void perfil_guardaYRetornaMediosDePago() {
        PerfilCocinera perfilConMedios = PerfilCocinera.builder()
                .id(perfilId)
                .conjuntoResidencial("Torres del Parque")
                .mediosPago(List.of(MedioPago.NEQUI, MedioPago.DAVIPLATA, MedioPago.EFECTIVO))
                .numeroNequi("3001234567")
                .numeroDaviplata("3107654321")
                .cuenta(cuenta)
                .build();

        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaEntity));
        when(perfilRepository.save(any(PerfilCocineraEntity.class))).thenAnswer(i -> i.getArgument(0));

        PerfilCocinera resultado = perfilService.crearPerfil(perfilConMedios, 1L);

        ArgumentCaptor<PerfilCocineraEntity> captor = ArgumentCaptor.forClass(PerfilCocineraEntity.class);
        verify(perfilRepository).save(captor.capture());

        PerfilCocineraEntity persistido = captor.getValue();
        assertNotNull(persistido);
        assertNotNull(persistido.getMediosPago());
        assertEquals(3, persistido.getMediosPago().size());
        assertTrue(persistido.getMediosPago().contains(MedioPago.NEQUI));
        assertTrue(persistido.getMediosPago().contains(MedioPago.DAVIPLATA));
        assertTrue(persistido.getMediosPago().contains(MedioPago.EFECTIVO));

        assertEquals(List.of(MedioPago.NEQUI, MedioPago.DAVIPLATA, MedioPago.EFECTIVO), resultado.getMediosPago());
    }
}
