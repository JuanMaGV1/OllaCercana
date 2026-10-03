package com.ollacercana.controller;

import com.ollacercana.controller.docs.PlatoApi;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.domain.Plato;
import com.ollacercana.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoCercanoResponseDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
import com.ollacercana.mapper.PlatoMapper;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.service.PlatoService;
import com.ollacercana.util.GeoUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Override
    @PostMapping
    public ResponseEntity<PlatoResponseDTO> crear(
            @RequestHeader("X-Cocinera-Id") UUID cocineraId,
            @Valid @RequestBody PlatoRequestDTO request) {
        Plato plato = platoMapper.toDomain(request);
        plato.setCocineraId(cocineraId);
        Plato guardado = platoService.crear(plato);
        return ResponseEntity.status(HttpStatus.CREATED).body(platoMapper.toResponse(guardado));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PlatoResponseDTO> obtenerPorId(@PathVariable UUID id) {
        Plato plato = platoService.obtenerPorId(id);
        return ResponseEntity.ok(platoMapper.toResponse(plato));
    }

    @Override
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<PlatoResponseDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AjusteDisponibilidadRequest request) {
        Plato actualizado = platoService.ajustarDisponibilidad(id, request.tipo(), request.cantidad(), request.version());
        return ResponseEntity.ok(platoMapper.toResponse(actualizado));
    }

    @Override
    @GetMapping("/cercanos")
    public ResponseEntity<List<PlatoCercanoResponseDTO>> listarCercanos(
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud) {
        List<Plato> platos = platoService.buscarCercanos(latitud, longitud);

        List<PlatoCercanoResponseDTO> dtos = platos.stream().map(p -> {
            String conjunto = perfilCocineraRepository.findById(p.getCocineraId())
                    .map(PerfilCocinera::getConjuntoResidencial)
                    .orElse("Conjunto Residencial");

            Integer distancia = null;
            if (latitud != null && longitud != null && p.getLatitud() != null && p.getLongitud() != null) {
                distancia = GeoUtils.redondearDistanciaMultiplo100(
                        GeoUtils.calcularDistanciaEnMetros(latitud, longitud, p.getLatitud(), p.getLongitud())
                );
            }

            return PlatoCercanoResponseDTO.builder()
                    .id(p.getId())
                    .nombre(p.getNombre())
                    .fotoUrl(p.getFotoUrl())
                    .tipoComida(p.getTipoComida())
                    .restricciones(p.getRestricciones())
                    .precioPorcion(p.getPrecioPorcion())
                    .porcionesDisponibles(p.getPorcionesDisponibles())
                    .conjunto(conjunto)
                    .distanciaAproximada(distancia)
                    .tiempoRestante(GeoUtils.formatearTiempoRestante(p.getFechaExpiracion()))
                    .build();
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        platoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}