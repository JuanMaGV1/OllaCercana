package com.ollacercana.service;

import com.ollacercana.domain.PerfilCocinera;
import java.util.List;
import java.util.UUID;

public interface IPerfilCocineraService {
    PerfilCocinera crearPerfil(PerfilCocinera perfil, Long cuentaId);
    PerfilCocinera actualizarPerfil(UUID id, PerfilCocinera perfilActualizado);
    boolean verificarTelefono(UUID perfilId, String otp);
    PerfilCocinera obtenerPorCuentaId(Long cuentaId);
    List<PerfilCocinera> listarDestacadas();
}