package com.ollacercana.controller.docs;

import com.ollacercana.controller.dtos.request.MapaCocinerasRequestDTO;
import com.ollacercana.controller.dtos.response.CocineraMapaResponseDTO;
import com.ollacercana.controller.dtos.response.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "Cocineras Mapa", description = "Mapa interactivo y geolocalización de cocineras con oferta activa")
@RequestMapping("/api/v1/cocineras")
public interface CocineraMapaApi {

    @Operation(
            summary = "Consultar cocineras con oferta activa en el mapa (HU-09)",
            description = "Retorna las cocineras con oferta activa dentro de un radio geográfico con coordenadas ofuscadas y distancia estimada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de cocineras en el área (o lista vacía si no hay ofertas)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CocineraMapaResponseDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Criterios de búsqueda inválidos (latitud [-90,90], longitud [-180,180], radio positivo)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping("/mapa")
    ResponseEntity<List<CocineraMapaResponseDTO>> consultarMapa(@Valid @ParameterObject MapaCocinerasRequestDTO request);
}
