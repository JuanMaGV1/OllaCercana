package com.ollacercana.service;

import java.util.List;
import java.util.UUID;

import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.domain.TipoAjustePorciones;

public interface PlatoService {

    List<Plato> buscarCercanos(Double latitud, Double longitud);

    Plato crear(Plato plato);

    Plato obtenerPorId(UUID id);

    Plato ajustarDisponibilidad(UUID platoId, TipoAjustePorciones tipo, Integer cantidad, Integer version);

    void eliminar(UUID id);
}