package com.ollacercana.validator;

import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.PerfilCocinera;

import java.util.UUID;

public interface IPerfilCocineraValidator {
    void validarParaCrear(PerfilCocinera perfil, Cuenta cuenta);
    void validarParaActualizar(UUID perfilId, PerfilCocinera perfil);
}