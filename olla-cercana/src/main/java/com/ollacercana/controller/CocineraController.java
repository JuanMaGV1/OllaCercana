package com.ollacercana.controller;

import com.ollacercana.controller.docs.CocineraApi;
import com.ollacercana.dto.response.HistorialPaginadoResponseDTO;
import com.ollacercana.dto.response.MetricasCocineraResponseDTO;
import com.ollacercana.security.UsuarioActual;
import com.ollacercana.service.MetricasCocineraService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/cocineras")
@RequiredArgsConstructor
public class CocineraController implements CocineraApi {

    private final MetricasCocineraService metricasService;
    private final UsuarioActual usuarioActual;

    @Override
    @GetMapping("/metricas")
    @PreAuthorize("hasRole('COCINERA')")
    public ResponseEntity<MetricasCocineraResponseDTO> obtenerMetricas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(metricasService.obtenerMetricas(usuarioActual.getCocineraId(), desde, hasta));
    }

    @Override
    @GetMapping("/historial")
    @PreAuthorize("hasRole('COCINERA')")
    public ResponseEntity<HistorialPaginadoResponseDTO> obtenerHistorial(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return ResponseEntity.ok(metricasService.obtenerHistorial(usuarioActual.getCocineraId(), pagina, tamanio));
    }
}
