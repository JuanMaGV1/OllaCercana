package com.ollacercana.core.services;

import com.ollacercana.controller.dtos.request.ReporteCrearDTO;
import com.ollacercana.controller.dtos.response.ReporteDTO;

/**
 * HU-18 / OC-034: contrato del servicio de reportes.
 */
public interface IReporteService {
    
    ReporteDTO crear(ReporteCrearDTO dto, Long reportanteId);
}