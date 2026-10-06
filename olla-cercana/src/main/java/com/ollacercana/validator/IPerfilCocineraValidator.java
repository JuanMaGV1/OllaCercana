package com.ollacercana.validator;

import java.util.UUID;

import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.model.domain.PerfilCocinera;

public interface IPerfilCocineraValidator {
    void validarParaCrear(PerfilCocinera perfil, Cuenta cuenta);
    void validarParaActualizar(UUID perfilId, PerfilCocinera perfil);
}