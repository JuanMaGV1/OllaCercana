package com.ollacercana.service;

import com.ollacercana.domain.Plato;
import com.ollacercana.domain.TipoAjustePorciones;

import java.util.List;
import java.util.UUID;

public interface PlatoService {

    List<Plato> buscarCercanos(Double latitud, Double longitud);

    Plato crear(Plato plato);

    Plato obtenerPorId(UUID id);

    Plato ajustarDisponibilidad(UUID platoId, TipoAjustePorciones tipo, Integer cantidad, Integer version);

    void eliminar(UUID id);
}