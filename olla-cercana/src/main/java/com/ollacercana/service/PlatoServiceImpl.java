package com.ollacercana.service;

import com.ollacercana.domain.Plato;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
import com.ollacercana.mapper.PlatoDtoMapper;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.validator.PlatoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ollacercana.dto.request.AjusteDisponibilidadRequest;
import com.ollacercana.exception.PlatoNoEncontradoException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoDtoMapper mapper;
    private final PlatoValidator validator;
    private final PlatoRepository repository;

    @Override
    public PlatoResponseDTO crear(PlatoRequestDTO request, UUID cocineraId) {
        Plato plato = mapper.toDomain(request);
        plato.setCocineraId(cocineraId);

        // Valida RN-27 (precio/porciones), RN-28 (max 3 activos), RN-30 (restricciones)
        validator.validarParaPublicar(plato);

        // RN-02: fija estado ACTIVO, fechaPublicacion y fechaExpiracion (+4h)
        plato.publicar();

        Plato guardado = repository.save(plato);

        return mapper.toResponse(guardado);
    }

    @Override
    public PlatoResponseDTO ajustarDisponibilidad(UUID platoId, AjusteDisponibilidadRequest request) {
        Plato plato = repository.findById(platoId)
                .orElseThrow(() -> new PlatoNoEncontradoException(platoId));

        // Valida version, cantidad y reduccion por debajo de comprometidas
        validator.validarAjusteDisponibilidad(plato, request);

        plato.ajustarDisponibilidad(request.tipo(), request.cantidad());

        Plato actualizado = repository.save(plato);
        return mapper.toResponse(actualizado);
    }
}