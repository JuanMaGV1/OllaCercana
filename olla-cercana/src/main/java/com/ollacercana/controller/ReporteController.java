package com.ollacercana.controller;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.docs.ReporteApi;
import com.ollacercana.controller.dtos.request.ReporteCrearDTO;
import com.ollacercana.controller.dtos.response.ReporteDTO;
import com.ollacercana.core.models.enums.MotivoReporte;
import com.ollacercana.core.services.IReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HU-18 / OC-034: implementación del módulo de reportes.
 * La identidad del reportante se obtiene del JWT — nunca del body (RN-07).
 */
@RestController
@RequiredArgsConstructor
public class ReporteController implements ReporteApi {

    private final IReporteService reporteService;
    private final UsuarioActual usuarioActual;

    @Override
    @PreAuthorize("hasAnyRole('COMPRADOR', 'COCINERA')")
    public ResponseEntity<ReporteDTO> crearReporte(@Valid ReporteCrearDTO dto) {
        ReporteDTO creado = reporteService.crear(dto, usuarioActual.getCuentaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @Override
    public ResponseEntity<List<String>> obtenerMotivos() {
        List<String> motivos = Arrays.stream(MotivoReporte.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(motivos);
    }
}