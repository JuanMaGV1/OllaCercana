package com.ollacercana.service;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.TipoComida;
import com.ollacercana.dto.request.PlatoRequestDTO;
import com.ollacercana.dto.response.PlatoResponseDTO;
import com.ollacercana.exception.ResourceNotFoundException;
import com.ollacercana.mapper.PlatoDtoMapper;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.validator.PlatoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository platoRepository;
    private final PlatoDtoMapper platoDtoMapper;
    private final PlatoValidator platoValidator;

    @Override
    public PlatoResponseDTO publicar(PlatoRequestDTO request) {
        log.info("Publicando nuevo plato: {}", request.nombre());

        // 1. DTO → Dominio
        Plato plato = platoDtoMapper.toDomain(request);

        // 2. Validar reglas de negocio (RN-27, RN-28, RN-30)
        platoValidator.validarParaPublicar(plato);

        // 3. Aplicar regla de publicación (RN-02)
        plato.publicar();

        // 4. Persistir en memoria (el repo asigna el UUID)
        Plato guardado = platoRepository.save(plato);

        log.info("Plato publicado con id: {}", guardado.getId());
        return platoDtoMapper.toResponse(guardado);
    }

    @Override
    public PlatoResponseDTO obtenerPorId(UUID id) {
        Plato plato = platoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plato", id));
        return platoDtoMapper.toResponse(plato);
    }

    @Override
    public List<PlatoResponseDTO> listarActivos() {
        return platoRepository.findActivosVigentes(EstadoPlato.ACTIVO, LocalDateTime.now())
            .stream()
            .map(platoDtoMapper::toResponse)
            .toList();
    }

    @Override
    public List<PlatoResponseDTO> listarPorTipo(TipoComida tipo) {
        return platoRepository.findByTipoComida(tipo).stream()
            .map(platoDtoMapper::toResponse)
            .toList();
    }

    @Override
    public PlatoResponseDTO ajustarPorciones(UUID id, int nuevaCantidad) {
        log.info("Ajustando porciones del plato {} a {}", id, nuevaCantidad);

        Plato plato = platoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plato", id));

        platoValidator.validarParaAjustar(plato);
        plato.cambiarPorcionesTotales(nuevaCantidad);

        Plato guardado = platoRepository.save(plato);
        return platoDtoMapper.toResponse(guardado);
    }

    @Override
    public void eliminar(UUID id) {
        if (!platoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plato", id);
        }
        platoRepository.deleteById(id);
        log.info("Plato eliminado: {}", id);
    }
}