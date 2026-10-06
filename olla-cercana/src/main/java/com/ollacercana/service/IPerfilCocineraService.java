package com.ollacercana.service;

import java.util.List;
import java.util.UUID;

import com.ollacercana.model.domain.PerfilCocinera;

public interface IPerfilCocineraService {
    PerfilCocinera crearPerfil(PerfilCocinera perfil, Long cuentaId);
    PerfilCocinera actualizarPerfil(UUID id, PerfilCocinera perfilActualizado);
    boolean verificarTelefono(UUID perfilId, String otp);
    PerfilCocinera obtenerPorCuentaId(Long cuentaId);
    List<PerfilCocinera> listarDestacadas();
}