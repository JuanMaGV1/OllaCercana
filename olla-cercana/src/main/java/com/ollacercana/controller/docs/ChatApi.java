package com.ollacercana.controller.docs;

import com.ollacercana.config.OpenApiConfig;
import com.ollacercana.controller.dtos.request.EnviarMensajeRequestDTO;
import com.ollacercana.controller.dtos.response.ErrorResponseDTO;
import com.ollacercana.controller.dtos.response.MensajeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Chat de Reservas", description = "Endpoints para mensajería entre comprador y cocinera (OC-247)")
@RequestMapping("/api/v1/reservas/{id}/mensajes")
public interface ChatApi {

    @Operation(summary = "Enviar mensaje al chat", security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje enviado", content = @Content(schema = @Schema(implementation = MensajeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Texto inválido o vacío", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "No perteneces a esta reserva", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Chat finalizado", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    ResponseEntity<MensajeResponseDTO> enviarMensaje(
            @Parameter(description = "ID de la reserva") @PathVariable("id") UUID id,
            @Valid @RequestBody EnviarMensajeRequestDTO request
    );

    @Operation(summary = "Listar mensajes del chat (con polling por cursor)", security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de mensajes", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MensajeResponseDTO.class)))),
            @ApiResponse(responseCode = "403", description = "No perteneces a esta reserva", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    ResponseEntity<List<MensajeResponseDTO>> listarMensajes(
            @Parameter(description = "ID de la reserva") @PathVariable("id") UUID id,
            @Parameter(description = "Fecha cursor para polling (mensajes posteriores a esta fecha)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde
    );

    @Operation(summary = "Marcar mensajes de la contraparte como leídos", security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mensajes marcados como leídos"),
            @ApiResponse(responseCode = "403", description = "No perteneces a esta reserva", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/leidos")
    ResponseEntity<Void> marcarLeidos(
            @Parameter(description = "ID de la reserva") @PathVariable("id") UUID id
    );

    @Operation(summary = "Obtener conteo de mensajes no leídos", security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conteo de no leídos"),
            @ApiResponse(responseCode = "403", description = "No perteneces a esta reserva", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/no-leidos")
    ResponseEntity<Map<String, Long>> contarNoLeidos(
            @Parameter(description = "ID de la reserva") @PathVariable("id") UUID id
    );
}