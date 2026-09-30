package com.ollacercana.controller.docs;

import com.ollacercana.dto.request.DecisionReservaRequestDTO;
import com.ollacercana.dto.response.ErrorResponseDTO;
import com.ollacercana.dto.response.ReservaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Reservas", description = "Gestión de solicitudes de reserva por parte de la cocinera")
@RequestMapping("/api/v1/reservas")
public interface ReservaApi {

    @Operation(
            summary = "Confirmar o rechazar una solicitud de reserva (HU-12)",
            description = "CONFIRMAR: la reserva pasa a CONFIRMADA, se conservan las porciones descontadas y se habilita "
                    + "la coordinación de la entrega (requiere horaEstimada). RECHAZAR: la reserva pasa a RECHAZADA y las "
                    + "porciones vuelven al plato (requiere motivo; comentario obligatorio si el motivo es OTRO). "
                    + "En ambos casos el comprador recibe un aviso con la decisión."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva actualizada",
                    content = @Content(schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la decisión inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "La reserva no pertenece a la cocinera",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "La reserva ya fue gestionada o venció su tiempo de respuesta (RN-04)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "La decisión no cumple las reglas de negocio",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{id}/decision")
    ResponseEntity<ReservaResponseDTO> decidir(
            @Parameter(description = "Id de la reserva") @PathVariable UUID id,
            @Parameter(description = "Id del perfil de la cocinera que decide", example = "11111111-1111-1111-1111-111111111111")
            @RequestHeader("X-Cocinera-Id") UUID cocineraId,
            @Valid @RequestBody DecisionReservaRequestDTO request
    );

    @Operation(summary = "Listar las solicitudes pendientes de la cocinera (HU-12)",
            description = "Solo devuelve reservas PENDIENTES que aún están dentro de su tiempo de respuesta, la más urgente primero.")
    @ApiResponse(responseCode = "200", description = "Solicitudes pendientes",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReservaResponseDTO.class))))
    @GetMapping("/pendientes")
    ResponseEntity<List<ReservaResponseDTO>> listarPendientes(
            @Parameter(description = "Id del perfil de la cocinera", example = "11111111-1111-1111-1111-111111111111")
            @RequestHeader("X-Cocinera-Id") UUID cocineraId
    );

    @Operation(summary = "Consultar una reserva por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada",
                    content = @Content(schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/{id}")
    ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable UUID id);
}
