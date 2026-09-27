package com.ollacercana.controller.docs;

import com.ollacercana.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.dto.request.VerificarOtpRequestDTO;
import com.ollacercana.dto.response.ErrorResponseDTO;
import com.ollacercana.dto.response.PerfilCocineraResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Perfiles de Cocinera", description = "API para gestión, consulta y validación OTP del perfil de cocinera")
public interface PerfilCocineraApi {

    @Operation(summary = "Crear perfil de cocinera")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Perfil creado con éxito",
                    content = @Content(schema = @Schema(implementation = PerfilCocineraResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto o datos duplicados", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<PerfilCocineraResponseDTO> crearPerfil(@Valid @RequestBody PerfilCocineraRequestDTO request);

    @Operation(summary = "Actualizar perfil de cocinera")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado con éxito",
                    content = @Content(schema = @Schema(implementation = PerfilCocineraResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto de validación", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<PerfilCocineraResponseDTO> actualizarPerfil(@PathVariable UUID id, @Valid @RequestBody PerfilCocineraRequestDTO request);

    @Operation(summary = "Verificar teléfono mediante OTP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teléfono verificado correctamente"),
            @ApiResponse(responseCode = "409", description = "OTP inválido o expirado", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<Map<String, String>> verificarTelefono(@PathVariable UUID id, @Valid @RequestBody VerificarOtpRequestDTO request);

    @Operation(summary = "Consultar perfil por ID de cuenta (findByCuentaId)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado", content = @Content(schema = @Schema(implementation = PerfilCocineraResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "No existe perfil para la cuenta", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<PerfilCocineraResponseDTO> obtenerPorCuentaId(@PathVariable Long cuentaId);

    @Operation(summary = "Listar cocineras destacadas (findByEsDestacadaTrue)")
    @ApiResponse(responseCode = "200", description = "Listado de perfiles destacados")
    ResponseEntity<List<PerfilCocineraResponseDTO>> listarDestacadas();
}