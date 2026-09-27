package com.ollacercana.validator;

import com.ollacercana.domain.Cuenta;
import com.ollacercana.dto.request.PerfilCocineraRequestDTO;

import java.util.UUID;

public interface IPerfilCocineraValidator {
    void validarParaCrear(PerfilCocineraRequestDTO request, Cuenta cuenta);
    void validarParaActualizar(UUID perfilId, PerfilCocineraRequestDTO request);
}