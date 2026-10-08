package com.ollacercana.core.services;

import java.util.List;
import java.util.UUID;

import com.ollacercana.core.models.PerfilCocinera;

public interface IPerfilCocineraService {
    PerfilCocinera crearPerfil(PerfilCocinera perfil, Long cuentaId);
    PerfilCocinera actualizarPerfil(UUID id, PerfilCocinera perfilActualizado);
    boolean verificarTelefono(UUID perfilId, String otp);
    PerfilCocinera obtenerPorCuentaId(Long cuentaId);
    List<PerfilCocinera> listarDestacadas();
}