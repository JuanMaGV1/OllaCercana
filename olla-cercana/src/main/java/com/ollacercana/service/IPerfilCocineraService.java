package com.ollacercana.service;

import com.ollacercana.model.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.model.dto.response.PerfilCocineraResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IPerfilCocineraService {

    PerfilCocineraResponseDTO crearPerfil(PerfilCocineraRequestDTO request);

    PerfilCocineraResponseDTO actualizarPerfil(UUID id, PerfilCocineraRequestDTO request);

    boolean verificarTelefono(UUID perfilId, String otp);

    PerfilCocineraResponseDTO obtenerPorCuentaId(UUID cuentaId);

    List<PerfilCocineraResponseDTO> listarDestacadas();
}