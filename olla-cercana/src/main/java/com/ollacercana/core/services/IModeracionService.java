package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.request.EjecutarDecisionDTO;
import com.ollacercana.core.models.PerfilCocinera;
import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.EstadoReporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * HU-19 / OC-035: contrato del servicio de moderación (RN-09, RN-23).
 */
public interface IModeracionService {
    Page<Reporte> obtenerReportes(EstadoReporte estado, Pageable pageable);
    Reporte obtenerDetalle(UUID reporteId);
    void resolver(UUID reporteId, EjecutarDecisionDTO dto, Long administradorId);
    List<PerfilCocinera> listarPerfilesPausados();
    void reactivarPerfil(UUID cocineraId, String justificacion, Long administradorId);
}