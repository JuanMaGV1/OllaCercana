package com.ollacercana.repository;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.TipoComida;
import com.ollacercana.exception.ConflictoVersionException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PlatoRepository {

    private final List<Plato> platos = new ArrayList<>();

    // ============ CRUD básico ============

    public Plato save(Plato plato) {
        if (plato.getId() == null) {
            plato.setId(UUID.randomUUID());
            platos.add(plato);
            return plato;
        }

        Optional<Plato> existenteOpt = findById(plato.getId());
        if (existenteOpt.isPresent()) {
            Plato existente = existenteOpt.get();
            if (!existente.getVersion().equals(plato.getVersion())) {
                throw new ConflictoVersionException(
                        existente.getVersion(),
                        existente.getPorcionesTotales(),
                        existente.getPorcionesComprometidas(),
                        existente.getEstado().name()
                );
            }
            int index = platos.indexOf(existente);
            platos.set(index, plato);
        } else {
            platos.add(plato);
        }
        return plato;
    }

    public Optional<Plato> findById(UUID id) {
        return platos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public List<Plato> findAll() {
        return new ArrayList<>(platos);
    }

    public boolean existsById(UUID id) {
        return platos.stream().anyMatch(p -> p.getId().equals(id));
    }

    public void deleteById(UUID id) {
        platos.removeIf(p -> p.getId().equals(id));
    }

    public void deleteAll() {
        platos.clear();
    }

    // ============ Consultas con streams ============

    public List<Plato> findByEstado(EstadoPlato estado) {
        return platos.stream()
                .filter(p -> p.getEstado() == estado)
                .toList();
    }

    public List<Plato> findByTipoComida(TipoComida tipo) {
        return platos.stream()
                .filter(p -> p.getTipoComida() == tipo)
                .toList();
    }

    /**
     * Platos de una cocinera especifica en un estado dado para la RN-28
     * que es el limite por cocinera y para mis platos en el perfil.
     */
    public List<Plato> findByCocineraIdAndEstado(UUID cocineraId, EstadoPlato estado) {
        return platos.stream()
                .filter(p -> cocineraId != null && cocineraId.equals(p.getCocineraId()))
                .filter(p -> p.getEstado() == estado)
                .toList();
    }

    /**
     * RN-02: platos activos y vigentes (fecha de expiración > ahora).
     * RN-03: con porciones disponibles > 0.
     */
    public List<Plato> findActivosVigentes(EstadoPlato estado, LocalDateTime ahora) {
        return platos.stream()
                .filter(p -> p.getEstado() == estado)
                .filter(p -> p.getFechaExpiracion() != null && p.getFechaExpiracion().isAfter(ahora))
                .filter(p -> p.getPorcionesDisponibles() > 0)
                .toList();
    }

    public long countActivosVigentes(EstadoPlato estado, LocalDateTime ahora) {
        return platos.stream()
                .filter(p -> p.getEstado() == estado)
                .filter(p -> p.getFechaExpiracion() != null && p.getFechaExpiracion().isAfter(ahora))
                .count();
    }

    public long countActivosVigentesPorCocinera(UUID cocineraId, EstadoPlato estado, LocalDateTime ahora) {
        return platos.stream()
                .filter(p -> cocineraId != null && cocineraId.equals(p.getCocineraId()))
                .filter(p -> p.getEstado() == estado)
                .filter(p -> p.getFechaExpiracion() != null && p.getFechaExpiracion().isAfter(ahora))
                .count();
    }

    public List<Plato> findParaExpirar(List<EstadoPlato> estados, LocalDateTime ahora) {
        return platos.stream()
                .filter(p -> estados.contains(p.getEstado()))
                .filter(p -> p.getFechaExpiracion() != null && !p.getFechaExpiracion().isAfter(ahora))
                .toList();
    }
}