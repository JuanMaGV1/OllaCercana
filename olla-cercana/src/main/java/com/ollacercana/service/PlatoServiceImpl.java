package com.ollacercana.service;

import com.ollacercana.domain.Plato;
import com.ollacercana.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.exception.PlatoNoEncontradoException;
import com.ollacercana.mapper.PlatoEntityMapper;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.validator.PlatoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * OC-92: crear() valida, calcula fecha de expiración = ahora + 4h (RN-02).
 */
@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository repository;
    private final PlatoEntityMapper entityMapper;
    private final PlatoValidator validator;

    @Override
    public Plato crear(Plato plato) {
        // Valida cocinera habilitada, RN-27 (precio/porciones), RN-28 (max 3 activos), RN-30 (restricciones)
        validator.validarParaPublicar(plato);

        // RN-02: fija estado ACTIVO, fechaPublicacion y fechaExpiracion (+4h)
        plato.publicar();

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

        return repository.save(plato);
    }

    @Override
    public void eliminar(UUID id) {
        if (!repository.existsById(id)) {
            throw new PlatoNoEncontradoException(id);
        }
        repository.deleteById(id);
    }
}
