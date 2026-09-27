package com.ollacercana.service;

import com.ollacercana.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.dto.response.PerfilCocineraResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IPerfilCocineraService {
    PerfilCocineraResponseDTO crearPerfil(PerfilCocineraRequestDTO request);
    PerfilCocineraResponseDTO actualizarPerfil(UUID id, PerfilCocineraRequestDTO request);
    boolean verificarTelefono(UUID perfilId, String otp);
    PerfilCocineraResponseDTO obtenerPorCuentaId(Long cuentaId);
    List<PerfilCocineraResponseDTO> listarDestacadas();
}