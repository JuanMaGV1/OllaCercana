package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.*;
import com.ollacercana.core.models.enums.*;
import com.ollacercana.persistence.document.NotificacionDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DocumentMappersTest {

    private final MedallaEntityMapper medallaMapper = new MedallaEntityMapper();
    private final MedallaUsuarioEntityMapper medallaUsuarioMapper =
            new MedallaUsuarioEntityMapper(medallaMapper);

    @Test
    @DisplayName("MedallaEntityMapper roundtrip")
    void medallaMapper() {
        Medalla m = Medalla.builder().codigo(CodigoMedalla.VECINO_FIEL)
                .nombre("Vecino Fiel").requisito("3 entregas").build();
        assertEquals(CodigoMedalla.VECINO_FIEL, medallaMapper.toDomain(medallaMapper.toEntity(m)).getCodigo());
        assertNull(medallaMapper.toEntity(null));
    }

    @Test
    @DisplayName("MedallaUsuarioEntityMapper roundtrip")
    void medallaUsuarioMapper() {
        MedallaUsuario mu = MedallaUsuario.builder()
                .id(UUID.randomUUID()).usuarioId(1L)
                .medalla(Medalla.builder().codigo(CodigoMedalla.VECINO_FIEL).nombre("x").requisito("y").build())
                .fechaOtorgada(LocalDateTime.now())
                .vigenteHasta(LocalDateTime.now().plusDays(7)).build();
        MedallaUsuario back = medallaUsuarioMapper.toDomain(medallaUsuarioMapper.toEntity(mu));
        assertEquals(mu.getUsuarioId(), back.getUsuarioId());
    }
}