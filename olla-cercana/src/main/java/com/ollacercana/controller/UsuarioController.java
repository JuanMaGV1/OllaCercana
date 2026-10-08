package com.ollacercana.controller;

import com.ollacercana.controller.docs.UsuarioApi;
import com.ollacercana.core.services.MedallaService;
import com.ollacercana.controller.dtos.response.MedallaUsuarioResponseDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController implements UsuarioApi {

    private final MedallaService medallaService;

    @Override
    @GetMapping("/{id}/medallas")
    public ResponseEntity<List<MedallaUsuarioResponseDTO>> listarMedallas(@PathVariable("id") Long id) {
        return ResponseEntity.ok(medallaService.listarVigentes(id, LocalDateTime.now()));
    }
}
