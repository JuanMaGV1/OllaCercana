package com.ollacercana.core.validators;

import java.util.UUID;

import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.PerfilCocinera;

public interface IPerfilCocineraValidator {
    void validarParaCrear(PerfilCocinera perfil, Cuenta cuenta);
    void validarParaActualizar(UUID perfilId, PerfilCocinera perfil);
}