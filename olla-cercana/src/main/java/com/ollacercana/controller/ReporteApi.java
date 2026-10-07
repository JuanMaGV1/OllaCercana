package com.ollacercana.controller;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.dtos.request.ReporteCrearDTO;
import com.ollacercana.controller.dtos.response.ReporteDTO;
import com.ollacercana.core.models.enums.MotivoReporte;
import com.ollacercana.core.services.ReporteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteApi {

    private final ReporteService reporteService;
    private final UsuarioActual usuarioActual;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPRADOR', 'COCINERA')")
    public ResponseEntity<ReporteDTO> crearReporte(
            @Valid @RequestBody ReporteCrearDTO dto) {
        
        ReporteDTO creado = reporteService.crear(dto, usuarioActual.getCuentaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/motivos")
    public ResponseEntity<List<String>> obtenerMotivos() {
        List<String> motivos = Arrays.stream(MotivoReporte.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(motivos);
    }
}