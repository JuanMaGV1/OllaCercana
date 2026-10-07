package com.ollacercana.controller.docs;

import com.ollacercana.config.OpenApiConfig;
import com.ollacercana.dto.response.ErrorResponseDTO;
import com.ollacercana.dto.response.MedallaUsuarioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "Usuarios", description = "Medallas e insignias de fidelidad (HU-21)")
@RequestMapping("/api/v1/usuarios")
public interface UsuarioApi {

    @Operation(
            summary = "Listar medallas vigentes de un usuario (HU-21)",
            description = "Lista solo las medallas vigentes del usuario: las que no vencen y las que aún no han vencido "
                    + "(por ejemplo, CONJUNTO_OLLA_VERDE dura 7 días).",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medallas vigentes (lista vacía si no tiene)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MedallaUsuarioResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/{id}/medallas")
    ResponseEntity<List<MedallaUsuarioResponseDTO>> listarMedallas(
            @Parameter(description = "ID de la cuenta del usuario") @PathVariable("id") Long id
    );
}
