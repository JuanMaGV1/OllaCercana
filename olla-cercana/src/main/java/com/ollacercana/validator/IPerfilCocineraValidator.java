package com.ollacercana.validator;

import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.model.dto.request.PerfilCocineraRequestDTO;

import java.util.UUID;

public interface IPerfilCocineraValidator {
    void validarParaCrear(PerfilCocineraRequestDTO request, Cuenta cuenta);
    void validarParaActualizar(UUID perfilId, PerfilCocineraRequestDTO request);
}