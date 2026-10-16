package com.ollacercana.persistence.mappers;

import com.ollacercana.controller.dtos.request.CalificacionRequestDTO;
import com.ollacercana.controller.dtos.request.ReporteCrearDTO;
import com.ollacercana.controller.dtos.response.CalificacionResponseDTO;
import com.ollacercana.controller.dtos.response.ReporteDTO;
import com.ollacercana.controller.mappers.CalificacionMapper;
import com.ollacercana.controller.mappers.ReporteMapper;
import com.ollacercana.core.models.*;
import com.ollacercana.core.models.enums.*;
import com.ollacercana.persistence.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MappersFaltantesTest {

    private final PerfilCocineraDomainMapper perfilDomainMapper = new PerfilCocineraDomainMapper();
    private final DecisionModeracionEntityMapper decisionMapper = new DecisionModeracionEntityMapper(new ReporteEntityMapper());
    private final CalificacionMapper calificacionMapper = new CalificacionMapper();
    private final ReporteMapper reporteMapper = new ReporteMapper();

    // ──────────────── PerfilCocineraDomainMapper ────────────────

    @Test
    @DisplayName("PerfilCocineraDomainMapper: roundtrip completo y nulls")
    void perfilDomainMapper_roundtrip() {
        UUID id = UUID.randomUUID();
        Cuenta cuenta = Cuenta.builder()
                .id(1L)
                .identidad(new Identidad("Rosa", "r@r.com", "3001112233", "http://foto.jpg"))
                .credenciales(new Credenciales("hash", "tokenFCM", true))
                .roles(Set.of(Rol.COCINERA))
                .estado(EstadoCuenta.ACTIVO)
                .fechaRegistro(LocalDateTime.now())
                .build();

        PerfilCocinera perfil = PerfilCocinera.builder()
                .id(id)
                .presentacion("Cocina típica")
                .especialidades(List.of("Sancocho"))
                .mediosPago(List.of(MedioPago.NEQUI))
                .numeroNequi("3001112233")
                .numeroDaviplata("3102223344")
                .conjuntoResidencial("Torres")
                .promedioCalificacion(4.8)
                .resenasPositivas(10)
                .esDestacada(true)
                .verificada(true)
                .pausada(false)
                .fechaReactivacion(LocalDateTime.now())
                .cuenta(cuenta)
                .build();

        PerfilCocineraEntity entity = perfilDomainMapper.toEntity(perfil);
        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals("Torres", entity.getConjuntoResidencial());

        PerfilCocinera back = perfilDomainMapper.toDomain(entity);
        assertNotNull(back);
        assertEquals(perfil.getPresentacion(), back.getPresentacion());
        assertTrue(back.isVerificada());
        assertTrue(back.verificada());
        assertFalse(back.pausada());

        assertNull(perfilDomainMapper.toDomain(null));
        assertNull(perfilDomainMapper.toEntity(null));
    }

    // ──────────────── DecisionModeracionEntityMapper ────────────────

    @Test
    @DisplayName("DecisionModeracionEntityMapper: roundtrip y nulls")
    void decisionModeracionMapper_roundtrip() {
        DecisionModeracion decision = DecisionModeracion.builder()
                .id(UUID.randomUUID())
                .decision(TipoDecision.INHABILITAR_PUBLICACION)
                .justificacion("Foto inapropiada")
                .administradorId(99L)
                .fecha(LocalDateTime.now())
                .reporte(Reporte.builder().id(UUID.randomUUID()).build())
                .build();

        DecisionModeracionEntity entity = decisionMapper.toEntity(decision);
        assertNotNull(entity);
        assertEquals(TipoDecision.INHABILITAR_PUBLICACION, entity.getDecision());

        DecisionModeracion back = decisionMapper.toDomain(entity);
        assertNotNull(back);
        assertEquals(decision.getJustificacion(), back.getJustificacion());

        assertNull(decisionMapper.toEntity(null));
        assertNull(decisionMapper.toDomain(null));
    }

    // ──────────────── CalificacionMapper & ReporteMapper ────────────────

    @Test
    @DisplayName("CalificacionMapper: toDomain y toResponse")
    void calificacionMapper_mappings() {
        CalificacionRequestDTO req = new CalificacionRequestDTO(5, "Rico");
        Calificacion c = calificacionMapper.toDomain(req);
        assertEquals(5, c.getEstrellas());
        assertEquals("Rico", c.getComentario());

        c.setId(UUID.randomUUID());
        c.setReservaId(UUID.randomUUID());
        c.setEstado(EstadoCalificacion.PUBLICADA);
        CalificacionResponseDTO res = calificacionMapper.toResponse(c);
        assertEquals(5, res.getEstrellas());
        assertEquals(EstadoCalificacion.PUBLICADA, res.getEstado());

        assertNull(calificacionMapper.toDomain(null));
        assertNull(calificacionMapper.toResponse(null));
    }

    @Test
    @DisplayName("ReporteMapper: toDto")
    void reporteMapper_toDto() {
        Reporte r = Reporte.builder()
                .id(UUID.randomUUID())
                .objetivo(ObjetivoReporte.PLATO)
                .motivo(MotivoReporte.FRAUDE_O_ESTAFA)
                .build();

        ReporteDTO dto = reporteMapper.toDto(r);
        assertNotNull(dto);
        assertEquals(ObjetivoReporte.PLATO, dto.getObjetivo());
        assertEquals(MotivoReporte.FRAUDE_O_ESTAFA, dto.getMotivo());

        assertNull(reporteMapper.toDto(null));
    }
}