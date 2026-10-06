package com.ollacercana.service.impl;

import com.ollacercana.exception.ConflictoVersionException;
import com.ollacercana.exception.PlatoNoEncontradoException;
import com.ollacercana.filter.FiltroCompuestoPlato;
import com.ollacercana.filter.FiltroDistanciaMaxima;
import com.ollacercana.mapper.PlatoEntityMapper;
import com.ollacercana.model.domain.EstadoPlato;
import com.ollacercana.model.domain.Plato;
import com.ollacercana.model.domain.TipoAjustePorciones;
import com.ollacercana.observer.PublicadorEventosPorciones;
import com.ollacercana.persistence.entity.PlatoEntity;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.service.PlatoService;
import com.ollacercana.validator.PlatoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository repository;
    private final PlatoValidator validator;
    private final PlatoEntityMapper entityMapper;
    private PublicadorEventosPorciones publicadorEventosPorciones;

    @Autowired(required = false)
    public void setPublicadorEventosPorciones(PublicadorEventosPorciones publicadorEventosPorciones) {
        this.publicadorEventosPorciones = publicadorEventosPorciones;
    }

    @Override
    @Transactional
    public Plato crear(Plato plato) {
        log.info("Publicando nuevo plato: nombre='{}', cocineraId={}", plato.getNombre(), plato.getCocineraId());
        validator.validarParaPublicar(plato);
        plato.publicar();
        if (plato.getId() == null) plato.setId(UUID.randomUUID());

        PlatoEntity guardado = repository.save(entityMapper.toEntity(plato));
        return entityMapper.toDomain(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Plato obtenerPorId(UUID id) {
        return repository.findById(id)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> new PlatoNoEncontradoException(id));
    }

    @Override
    @Transactional
    public Plato ajustarDisponibilidad(UUID platoId, TipoAjustePorciones tipo, Integer cantidad, Integer version) {
        Plato plato = obtenerPorId(platoId);
        validator.validarAjusteDisponibilidad(plato, tipo, cantidad);

        int disponiblesAntes = plato.getPorcionesDisponibles();
        plato.ajustarDisponibilidad(tipo, cantidad);
        plato.setVersion(version);

        try {
            PlatoEntity guardado = repository.saveAndFlush(entityMapper.toEntity(plato));
            if (publicadorEventosPorciones != null) {
                publicadorEventosPorciones.publicarSiCambio(disponiblesAntes, entityMapper.toDomain(guardado));
            }
            return entityMapper.toDomain(guardado);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConflictoVersionException(plato.getVersion(), plato.getPorcionesTotales(),
                    plato.getPorcionesComprometidas(), plato.getEstado().name());
        }
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        if (!repository.existsById(id)) throw new PlatoNoEncontradoException(id);
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plato> buscarCercanos(Double latitudCliente, Double longitudCliente) {
        List<Plato> activos = repository.findActivosVigentes(EstadoPlato.ACTIVO, LocalDateTime.now())
                .stream().map(entityMapper::toDomain).toList();

        FiltroCompuestoPlato filtro = new FiltroCompuestoPlato();
        if (latitudCliente != null && longitudCliente != null) {
            filtro.agregar(new FiltroDistanciaMaxima(latitudCliente, longitudCliente, 2000.0));
        }
        return activos.stream().filter(filtro::cumple).toList();
    }
}