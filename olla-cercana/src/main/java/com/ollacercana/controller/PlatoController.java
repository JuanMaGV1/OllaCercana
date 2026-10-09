package com.ollacercana.controller;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.docs.PlatoApi;
import com.ollacercana.controller.dtos.request.AjusteDisponibilidadRequest;
import com.ollacercana.controller.dtos.request.ConsultaPlatosRequest;
import com.ollacercana.controller.dtos.request.PlatoRequestDTO;
import com.ollacercana.controller.dtos.response.PaginaResponseDTO;
import com.ollacercana.controller.dtos.response.PlatoCercanoResponseDTO;
import com.ollacercana.controller.dtos.response.PlatoResponseDTO;
import com.ollacercana.controller.handlers.exception.AccesoDenegadoException;
import com.ollacercana.controller.mappers.PlatoMapper;
import com.ollacercana.core.models.PerfilCocinera;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.MedioPago;
import com.ollacercana.core.services.PlatoService;
import com.ollacercana.core.util.GeoUtils;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platos")
@RequiredArgsConstructor


public class PlatoController implements PlatoApi {

    private final PlatoService platoService;
    private final PlatoMapper platoMapper;
    private final PerfilCocineraRepository perfilCocineraRepository;
    private final UsuarioActual usuarioActual;
    
    @Override
    @PostMapping
    @PreAuthorize("hasRole('COCINERA')")
    public ResponseEntity<PlatoResponseDTO> crear(@Valid @RequestBody PlatoRequestDTO request) {
        Plato plato = platoMapper.toDomain(request);
        plato.setCocineraId(usuarioActual.getCocineraId());
        Plato guardado = platoService.crear(plato);
        return ResponseEntity.status(HttpStatus.CREATED).body(platoMapper.toResponse(guardado));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PlatoResponseDTO> obtenerPorId(@PathVariable UUID id) {
        Plato plato = platoService.obtenerPorId(id);
        List<MedioPago> mediosPago = platoService.obtenerMediosPago(id);
        PlatoResponseDTO response = platoMapper.toResponse(plato).withMediosPago(mediosPago);
        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/{id}/disponibilidad")
    @PreAuthorize("hasRole('COCINERA')")
    public ResponseEntity<PlatoResponseDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AjusteDisponibilidadRequest request) {
        Plato existente = platoService.obtenerPorId(id);
        if (!existente.getCocineraId().equals(usuarioActual.getCocineraId())) {
            throw new AccesoDenegadoException("No tienes permiso para modificar la disponibilidad de este plato");
        }
        Plato actualizado = platoService.ajustarDisponibilidad(id, request.tipo(), request.cantidad(), request.version());
        return ResponseEntity.ok(platoMapper.toResponse(actualizado));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COCINERA') or hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        Plato existente = platoService.obtenerPorId(id);
        if (!usuarioActual.tieneRol(com.ollacercana.core.models.enums.Rol.ADMIN) &&
                !existente.getCocineraId().equals(usuarioActual.getCocineraId())) {
            throw new AccesoDenegadoException("No tienes permiso para eliminar este plato");
        }
        platoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cercanos")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PaginaResponseDTO<PlatoCercanoResponseDTO>> consultarCercanos(
            @ParameterObject @Valid @ModelAttribute ConsultaPlatosRequest request) {
        return ResponseEntity.ok(platoService.consultarCercanos(request));
    }
}