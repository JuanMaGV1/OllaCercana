package com.ollacercana.controller;

import com.ollacercana.controller.docs.ReservaApi;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.Reserva;
import com.ollacercana.dto.request.CierreTransaccionRequestDTO;
import com.ollacercana.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.dto.request.ReservaRequestDTO;
import com.ollacercana.dto.response.ReservaResponseDTO;
import com.ollacercana.mapper.ReservaMapper;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.service.PlatoService;
import com.ollacercana.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Override
    @PostMapping
    public ResponseEntity<ReservaResponseDTO> crear(
            @RequestHeader("X-Comprador-Id") Long compradorId,
            @Valid @RequestBody ReservaRequestDTO request) {

        // 1. DTO -> Dominio (Mapper In)
        Reserva reserva = reservaMapper.toDomain(request);

        // 2. Ejecución de lógica en Service (recibe y devuelve Dominio)
        Reserva guardada = reservaService.crear(compradorId, reserva);

        // 3. Enriquecimiento de datos para la presentación
        Plato plato = platoService.obtenerPorId(guardada.getPlatoId());
        String conjunto = perfilCocineraRepository.findById(guardada.getCocineraId())
                .map(PerfilCocinera::getConjuntoResidencial)
                .orElse("Conjunto Residencial");

        // 4. Dominio -> DTO (Mapper Out)
        ReservaResponseDTO response = reservaMapper.toResponseDTO(guardada, plato.getNombre(), conjunto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PatchMapping("/{id}/decision")
    public ResponseEntity<ReservaResponseDTO> decidir(
            @PathVariable UUID id,
            @RequestHeader("X-Cocinera-Id") UUID cocineraId,
            @Valid @RequestBody DecisionReservaRequestDTO request) {
        Reserva actualizada = reservaService.decidir(id, cocineraId, request);
        return ResponseEntity.ok(reservaMapper.toResponse(actualizada));
    }

    @Override
    @PostMapping("/{id}/completar")
    public ResponseEntity<ReservaResponseDTO> completar(
            @PathVariable UUID id,
            @Valid @RequestBody CierreTransaccionRequestDTO request) {
        Reserva completada = reservaService.completar(id, request.comentario());
        return ResponseEntity.ok(reservaMapper.toResponse(completada));
    }

    @Override
    @GetMapping("/pendientes")
    public ResponseEntity<List<ReservaResponseDTO>> listarPendientes(
            @RequestHeader("X-Cocinera-Id") UUID cocineraId) {
        return ResponseEntity.ok(reservaMapper.toResponseList(reservaService.listarPendientesDeCocinera(cocineraId)));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(reservaMapper.toResponse(reservaService.obtenerPorId(id)));
    }
}