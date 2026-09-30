package com.ollacercana.service;

import java.util.List;
import java.util.UUID;

import com.ollacercana.model.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.model.dto.response.PerfilCocineraResponseDTO;

public interface IPerfilCocineraService {
    PerfilCocineraResponseDTO crearPerfil(PerfilCocineraRequestDTO request);
    PerfilCocineraResponseDTO actualizarPerfil(UUID id, PerfilCocineraRequestDTO request);
    boolean verificarTelefono(UUID perfilId, String otp);
    PerfilCocineraResponseDTO obtenerPorCuentaId(Long cuentaId);
    List<PerfilCocineraResponseDTO> listarDestacadas();
}