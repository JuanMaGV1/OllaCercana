package com.ollacercana.service;

import com.ollacercana.domain.Plato;
import com.ollacercana.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.exception.ConflictoVersionException;
import com.ollacercana.exception.PlatoNoEncontradoException;
import com.ollacercana.mapper.PlatoEntityMapper;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.validator.PlatoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository repository;
    private final PlatoEntityMapper entityMapper;
    private final PlatoValidator validator;

    @Override
    public Plato crear(Plato plato) {
        validator.validarParaPublicar(plato);
        plato.publicar();

        if (plato.getId() == null) {
            plato.setId(UUID.randomUUID());
        }

        Plato entidad = entityMapper.toEntity(plato);
        Plato guardado = repository.save(entidad);
        return entityMapper.toDomain(guardado);
    }

    @Override
    public Plato obtenerPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new PlatoNoEncontradoException(id));
    }

    @Override
    public Plato ajustarDisponibilidad(UUID platoId, AjusteDisponibilidadRequest request) {
        Plato plato = repository.findById(platoId)
                .orElseThrow(() -> new PlatoNoEncontradoException(platoId));

        validator.validarAjusteDisponibilidad(plato, request);

        plato.ajustarDisponibilidad(request.tipo(), request.cantidad());
        plato.setVersion(request.version());

        try {
            return repository.saveAndFlush(plato);
        } catch (ObjectOptimisticLockingFailureException e) {
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
            throw new PlatoNoEncontradoException(id);
        }
        repository.deleteById(id);
    }
}