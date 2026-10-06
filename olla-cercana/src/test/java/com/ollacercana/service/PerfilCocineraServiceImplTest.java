// test/.../service/PerfilCocineraQueryServiceImplTest.java
package com.ollacercana.service;

import com.ollacercana.exception.CocineraNoEncontradaException;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.service.impl.PerfilCocineraQueryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}