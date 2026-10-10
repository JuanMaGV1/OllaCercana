package com.ollacercana.controller;

import com.ollacercana.controller.docs.ChatApi;
import com.ollacercana.controller.dtos.request.EnviarMensajeRequestDTO;
import com.ollacercana.controller.dtos.response.MensajeResponseDTO;
import com.ollacercana.core.services.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller REST de Chat de Reservas (MongoDB).
 *
 * HU-13 · RN-17 · RN-18
 * Solo accesible por COMPRADOR y COCINERA de la reserva.
 *
 * @see ChatApi
 * @see OC-247 Endpoints de chat
 * @see OC-263 Envío con evento
 * @see OC-264 Polling con cursor
 * @see OC-265 Mensajes no leídos
 */
@RestController
@RequestMapping("/api/v1/reservas/{id}/mensajes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPRADOR', 'COCINERA')")
public class ChatController implements ChatApi {

    private final ChatService chatService;

    @Override
    @PostMapping
    public ResponseEntity<MensajeResponseDTO> enviarMensaje(
            @PathVariable("id") UUID id,
            @Valid @RequestBody EnviarMensajeRequestDTO request) {
        MensajeResponseDTO respuesta = chatService.enviarMensaje(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<MensajeResponseDTO>> listarMensajes(
            @PathVariable("id") UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde) {
        return ResponseEntity.ok(chatService.listarMensajes(id, desde));
    }

    @Override
    @PatchMapping("/leidos")
    public ResponseEntity<Void> marcarLeidos(@PathVariable("id") UUID id) {
        chatService.marcarLeidos(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/no-leidos")
    public ResponseEntity<Map<String, Long>> contarNoLeidos(@PathVariable("id") UUID id) {
        long conteo = chatService.contarNoLeidos(id);
        return ResponseEntity.ok(Map.of("noLeidos", conteo));
    }
}
