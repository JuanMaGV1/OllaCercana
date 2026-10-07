package com.ollacercana.core.patterns.observer;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** HU-16: registro en memoria de suscriptores, agrupados por plato. */
@Component
public class RegistroSuscripciones {

    private final Map<UUID, Set<ObservadorPorciones>> porPlato = new ConcurrentHashMap<>();

    public void suscribir(UUID platoId, ObservadorPorciones suscriptor) {
        porPlato.computeIfAbsent(platoId, k -> ConcurrentHashMap.newKeySet()).add(suscriptor);
    }

    public void cancelar(UUID platoId, ObservadorPorciones suscriptor) {
        porPlato.computeIfPresent(platoId, (k, set) -> {
            set.remove(suscriptor);
            return set.isEmpty() ? null : set;
        });
    }

    public List<ObservadorPorciones> de(UUID platoId) {
        return List.copyOf(porPlato.getOrDefault(platoId, Set.of()));
    }

    public int total() {
        return porPlato.values().stream().mapToInt(Set::size).sum();
    }
}
