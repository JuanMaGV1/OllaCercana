package com.ollacercana.controller;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.docs.ReservaApi;
import com.ollacercana.controller.dtos.request.CierreTransaccionRequestDTO;
import com.ollacercana.controller.dtos.request.DecisionReservaRequestDTO;
import com.ollacercana.controller.dtos.request.ReservaRequestDTO;
import com.ollacercana.controller.dtos.response.ReservaResponseDTO;
import com.ollacercana.controller.handlers.exception.AccesoDenegadoException;
import com.ollacercana.controller.mappers.ReservaMapper;
import com.ollacercana.core.models.Reserva;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.services.ReservaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST de Reservas.
 *
 * HU-11 · HU-12 · HU-14 · HU-23
 *
 * @see OC-140 Endpoint POST /reservas
 * @see OC-150 Endpoint PATCH /decision
 * @see OC-160 Endpoint POST /completar
 * @see OC-255 Validación de método de pago
 */
@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController implements ReservaApi {

    private final ReservaService reservaService;
    private final ReservaMapper reservaMapper;
    private final UsuarioActual usuarioActual;

    @Override
    @PostMapping
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<ReservaResponseDTO> crear(@Valid @RequestBody ReservaRequestDTO request) {
        Long compradorId = usuarioActual.getCuentaId();
        Reserva reserva = reservaMapper.toDomain(request);
        Reserva guardada = reservaService.crear(compradorId, reserva);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservaService.obtenerConDetalle(guardada.getId()));
    }

    @Override
    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasRole('COCINERA')")
    public ResponseEntity<ReservaResponseDTO> decidir(
            @PathVariable UUID id,
            @Valid @RequestBody DecisionReservaRequestDTO request) {
        UUID cocineraId = usuarioActual.getCocineraId();
        Reserva actualizada = reservaService.decidir(id, cocineraId, request);
        return ResponseEntity.ok(reservaMapper.toResponse(actualizada));
    }

    @Override
    @PostMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('COMPRADOR', 'COCINERA')")
    public ResponseEntity<ReservaResponseDTO> completar(
            @PathVariable UUID id,
            @Valid @RequestBody CierreTransaccionRequestDTO request) {
        Reserva reserva = reservaService.obtenerPorId(id);
        Long cuentaId = usuarioActual.getCuentaId();

        boolean esComprador = reserva.getCompradorId().equals(cuentaId);
        boolean esCocinera = usuarioActual.getCocineraIdOpt()
                .map(cocineraId -> cocineraId.equals(reserva.getCocineraId()))
                .orElse(false);

        if (!esComprador && !esCocinera && !usuarioActual.tieneRol(Rol.ADMIN)) {
            throw new AccesoDenegadoException("No tienes permiso para cerrar esta transacción");
        }

        Reserva completada = reservaService.completar(id, request.comentario());
        return ResponseEntity.ok(reservaMapper.toResponse(completada));
    }

    @Override
    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('COCINERA')")
    public ResponseEntity<List<ReservaResponseDTO>> listarPendientes() {
        UUID cocineraId = usuarioActual.getCocineraId();
        return ResponseEntity.ok(reservaMapper.toResponseList(reservaService.listarPendientesDeCocinera(cocineraId)));
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable UUID id) {
        Reserva reserva = reservaService.obtenerPorId(id);
        Long cuentaId = usuarioActual.getCuentaId();

        boolean esComprador = reserva.getCompradorId().equals(cuentaId);
        boolean esCocinera = usuarioActual.getCocineraIdOpt()
                .map(cocineraId -> cocineraId.equals(reserva.getCocineraId()))
                .orElse(false);

        if (!esComprador && !esCocinera && !usuarioActual.tieneRol(Rol.ADMIN)) {
            throw new AccesoDenegadoException("No tienes permiso para consultar esta reserva");
        }

        return ResponseEntity.ok(reservaMapper.toResponse(reserva));
    }
}