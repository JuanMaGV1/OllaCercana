package com.ollacercana.controller;

import com.ollacercana.domain.MotivoReporte;
import com.ollacercana.dto.ReporteCrearDto;
import com.ollacercana.dto.ReporteDto;
import com.ollacercana.security.UsuarioActual;
import com.ollacercana.service.ReporteService;
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
    public ResponseEntity<ReporteDto> crearReporte(
            @Valid @RequestBody ReporteCrearDto dto) {
        
        ReporteDto creado = reporteService.crear(dto, usuarioActual.getCuentaId());
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
