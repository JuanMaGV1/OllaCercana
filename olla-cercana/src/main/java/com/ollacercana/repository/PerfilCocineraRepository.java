package com.ollacercana.repository;

import com.ollacercana.domain.PerfilCocinera;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PerfilCocineraRepository {

    private final Map<UUID, PerfilCocinera> perfiles = new HashMap<>();

    public PerfilCocinera registrar(PerfilCocinera perfil) {
        perfiles.put(perfil.id(), perfil);
        return perfil;
    }

    public Optional<PerfilCocinera> findById(UUID id) {
        return Optional.ofNullable(perfiles.get(id));
    }
}