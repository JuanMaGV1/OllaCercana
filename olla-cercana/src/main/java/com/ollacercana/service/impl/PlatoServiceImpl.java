package com.ollacercana.service.impl;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.TipoAjustePorciones;
import com.ollacercana.exception.ConflictoVersionException;
import com.ollacercana.exception.PlatoNoEncontradoException;
import com.ollacercana.filter.FiltroCompuestoPlato;
import com.ollacercana.filter.FiltroDistanciaMaxima;
import com.ollacercana.observer.PublicadorEventosPorciones;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.service.PlatoService;
import com.ollacercana.validator.PlatoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository repository;
    private final PlatoValidator validator;
    private PublicadorEventosPorciones publicadorEventosPorciones;

    @Autowired(required = false)
    public void setPublicadorEventosPorciones(PublicadorEventosPorciones publicadorEventosPorciones) {
        this.publicadorEventosPorciones = publicadorEventosPorciones;
    }

    @Override
    public Plato crear(Plato plato) {
        log.info("Publicando nuevo plato: nombre='{}', cocineraId={}", plato.getNombre(), plato.getCocineraId());
        validator.validarParaPublicar(plato);
        plato.publicar();

        if (plato.getId() == null) {
            plato.setId(UUID.randomUUID());
        }

        Plato guardado = repository.save(plato);
        log.info("Plato publicado exitosamente con id={}", guardado.getId());
        return guardado;
    }

    @Override
    public Plato obtenerPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Consulta fallida: plato con id={} no encontrado", id);
                    return new PlatoNoEncontradoException(id);
                });
    }

    @Override
    public Plato ajustarDisponibilidad(UUID platoId, TipoAjustePorciones tipo, Integer cantidad, Integer version) {
        log.info("Ajustando disponibilidad de plato {}: tipo={}, cantidad={}, version={}", platoId, tipo, cantidad, version);
        Plato plato = repository.findById(platoId)
                .orElseThrow(() -> new PlatoNoEncontradoException(platoId));

        validator.validarAjusteDisponibilidad(plato, tipo, cantidad);
        int disponiblesAntes = plato.getPorcionesDisponibles();
        plato.ajustarDisponibilidad(tipo, cantidad);
        plato.setVersion(version);

        try {
            Plato guardado = repository.saveAndFlush(plato);
            if (publicadorEventosPorciones != null) {
                publicadorEventosPorciones.publicarSiCambio(disponiblesAntes, guardado);
            }
            log.info("Disponibilidad actualizada para plato {}", platoId);
            return guardado;
        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Conflicto de concurrencia al actualizar plato {}", platoId);
            throw new ConflictoVersionException(
                    plato.getVersion(),
                    plato.getPorcionesTotales(),
                    plato.getPorcionesComprometidas(),
                    plato.getEstado().name()
            );
        }
    }

    @Override
    public void eliminar(UUID id) {
        if (!repository.existsById(id)) {
            log.warn("Intento de eliminar plato inexistente: id={}", id);
            throw new PlatoNoEncontradoException(id);
        }
        repository.deleteById(id);
        log.info("Plato eliminado con id={}", id);
    }

    @Override
    public List<Plato> buscarCercanos(Double latitudCliente, Double longitudCliente) {
        log.info("Buscando platos cercanos a coordenadas ({}, {})", latitudCliente, longitudCliente);
        List<Plato> platosActivos = repository.findActivosVigentes(EstadoPlato.ACTIVO, LocalDateTime.now());

        FiltroCompuestoPlato filtroComposite = new FiltroCompuestoPlato();
        if (latitudCliente != null && longitudCliente != null) {
            filtroComposite.agregar(new FiltroDistanciaMaxima(latitudCliente, longitudCliente, 2000.0));
        }

        return platosActivos.stream()
                .filter(filtroComposite::cumple)
                .toList();
    }
}