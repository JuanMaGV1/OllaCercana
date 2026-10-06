package com.ollacercana.controller;

import com.ollacercana.controller.docs.ReservaApi;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.domain.Reserva;
import com.ollacercana.model.domain.Rol;
import com.ollacercana.model.dto.request.CierreTransaccionRequestDTO;
import com.ollacercana.model.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.model.dto.request.ReservaRequestDTO;
import com.ollacercana.model.dto.response.ReservaResponseDTO;
import com.ollacercana.exception.AccesoDenegadoException;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.security.UsuarioActual;
import com.ollacercana.service.PlatoService;
import com.ollacercana.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController implements ReservaApi {

    private final ReservaService reservaService;
    private final PlatoService platoService;
    private final PerfilCocineraRepository perfilCocineraRepository;
    private final ReservaMapper reservaMapper;
    private final UsuarioActual usuarioActual;

    @Override
    @PostMapping
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<ReservaResponseDTO> crear(@Valid @RequestBody ReservaRequestDTO request) {
        Long compradorId = usuarioActual.getCuentaId();
        Reserva reserva = reservaMapper.toDomain(request);
        Reserva guardada = reservaService.crear(compradorId, reserva);

        Plato plato = platoService.obtenerPorId(guardada.getPlatoId());

        String conjunto = perfilCocineraRepository.findById(guardada.getCocineraId())
                .map(perfil -> perfil.getConjuntoResidencial())
                .orElse("Conjunto Residencial");

        ReservaResponseDTO response = reservaMapper.toResponseDTO(guardada, plato.getNombre(), conjunto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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