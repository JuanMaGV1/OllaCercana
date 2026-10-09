package com.ollacercana.controller.docs;

import com.ollacercana.config.OpenApiConfig;
import com.ollacercana.controller.dtos.response.ErrorResponseDTO;
import com.ollacercana.controller.dtos.response.HistorialPaginadoResponseDTO;
import com.ollacercana.controller.dtos.response.MetricasCocineraResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Cocineras", description = "Resumen de historial e ingresos referenciales de la cocinera (HU-20)")
@RequestMapping("/api/v1/cocineras")
public interface CocineraApi {

    @Operation(
            summary = "Consultar métricas de ingresos referenciales (HU-20)",
            description = "Retorna el total de ingresos referenciales, las porciones entregadas y el plato con más pedidos "
                    + "de la cocinera autenticada. El periodo es opcional; sin periodo devuelve el acumulado. "
                    + "Una cocinera sin ventas recibe ceros y plato más pedido vacío.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas calculadas",
                    content = @Content(schema = @Schema(implementation = MetricasCocineraResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Formato de fecha inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol COCINERA",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Periodo inválido (desde posterior a hasta)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/metricas")
    ResponseEntity<MetricasCocineraResponseDTO> obtenerMetricas(
            @Parameter(description = "Fecha inicial del periodo (yyyy-MM-dd), opcional")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Fecha final del periodo (yyyy-MM-dd), opcional")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    );

    @Operation(
            summary = "Consultar historial de pedidos con detalle (HU-20)",
            description = "Historial paginado, del más reciente al más antiguo, solo de reservas COMPLETADAS de la cocinera. "
                    + "Cada pedido incluye fecha, nombre del comprador, porciones y calificación (nula si aún no la tiene).",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página del historial (contenido vacío si no hay ventas)",
                    content = @Content(schema = @Schema(implementation = HistorialPaginadoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol COCINERA",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/historial")
    ResponseEntity<HistorialPaginadoResponseDTO> obtenerHistorial(
            @Parameter(description = "Número de página, desde 0") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Tamaño de página (1 a 50)") @RequestParam(defaultValue = "10") int tamanio
    );
}