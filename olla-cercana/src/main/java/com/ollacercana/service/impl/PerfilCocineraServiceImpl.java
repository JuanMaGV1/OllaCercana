package com.ollacercana.service.impl;

import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ResourceNotFoundException;
import com.ollacercana.mapper.CuentaEntityMapper;
import com.ollacercana.mapper.PerfilCocineraEntityMapper;
import com.ollacercana.model.domain.CodigoOTP;
import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.model.domain.EstadoCuenta;
import com.ollacercana.model.domain.PerfilCocinera;
import com.ollacercana.model.dto.request.PerfilCocineraRequestDTO;
import com.ollacercana.model.dto.response.PerfilCocineraResponseDTO;
import com.ollacercana.persistence.entity.CodigoOTPEntity;
import com.ollacercana.persistence.entity.CuentaEntity;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import com.ollacercana.repository.CodigoOTPRepository;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.service.IPerfilCocineraService;
import com.ollacercana.validator.IPerfilCocineraValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilCocineraServiceImpl implements IPerfilCocineraService {

    private final PerfilCocineraRepository perfilRepository;
    private final CuentaRepository cuentaRepository;
    private final CodigoOTPRepository codigoOTPRepository;
    private final PerfilCocineraEntityMapper perfilMapper;
    private final CuentaEntityMapper cuentaMapper;
    private final IPerfilCocineraValidator validator;

    @Override
    @Transactional
    public PerfilCocineraResponseDTO crearPerfil(PerfilCocineraRequestDTO request) {
        CuentaEntity cuentaEntity = cuentaRepository.findById(request.getCuentaId())
                .orElseThrow(() -> new ConflictoException(
                        "La cuenta asociada con ID " + request.getCuentaId() + " no existe"));

        Cuenta cuenta = cuentaMapper.toDomain(cuentaEntity);
        validator.validarParaCrear(request, cuenta);

        PerfilCocinera perfil = PerfilCocinera.builder()
        .id(UUID.randomUUID())   // ← id
        .cuentaId(request.getCuentaId())
        .presentacion(request.getPresentacion())
        .conjuntoResidencial(request.getConjuntoResidencial())
        .especialidades(request.getEspecialidades())
        .mediosPago(request.getMediosPago())
        .numeroNequi(request.getNumeroNequi())
        .numeroDaviplata(request.getNumeroDaviplata())
        .promedioCalificacion(0.0)
        .resenasPositivas(0)
        .esDestacada(false)
        .verificada(false)
        .pausada(false)
        .build();

        PerfilCocineraEntity entity = perfilMapper.toEntity(perfil);
        PerfilCocineraEntity guardado = perfilRepository.save(entity);

        // Generar OTP
        String codigoGenerado = String.format("%06d", new Random().nextInt(999999));
        CodigoOTPEntity otp = CodigoOTPEntity.builder()
                .id(UUID.randomUUID())   // ← id
                .perfilId(guardado.getId())
                .codigo(codigoGenerado)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .usado(false)
                .build();
        codigoOTPRepository.save(otp);

        log.info("=================================================");
        log.info(" OTP para perfil {}: {}", guardado.getId(), codigoGenerado);
        log.info("=================================================");

        return toResponseDTO(perfilMapper.toDomain(guardado), cuenta);
    }

    @Override
    @Transactional
    public PerfilCocineraResponseDTO actualizarPerfil(UUID id, PerfilCocineraRequestDTO request) {
        PerfilCocineraEntity entity = perfilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PerfilCocinera", id));

        PerfilCocinera perfil = perfilMapper.toDomain(entity);
        validator.validarParaActualizar(id, request);

        perfil.setPresentacion(request.getPresentacion());
        perfil.setConjuntoResidencial(request.getConjuntoResidencial());
        perfil.setEspecialidades(request.getEspecialidades());
        perfil.setMediosPago(request.getMediosPago());
        perfil.setNumeroNequi(request.getNumeroNequi());
        perfil.setNumeroDaviplata(request.getNumeroDaviplata());

        PerfilCocineraEntity actualizado = perfilRepository.save(perfilMapper.toEntity(perfil));
        Cuenta cuenta = cuentaRepository.findById(actualizado.getCuentaId())
                .map(cuentaMapper::toDomain)
                .orElse(null);

        return toResponseDTO(perfilMapper.toDomain(actualizado), cuenta);
    }

    @Override
    @Transactional
    public boolean verificarTelefono(UUID perfilId, String otp) {
        PerfilCocineraEntity entity = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new ResourceNotFoundException("PerfilCocinera", perfilId));

        CodigoOTPEntity otpEntity = codigoOTPRepository
                .findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId)
                .orElseThrow(() -> new ConflictoException(
                        "No hay ningún código OTP activo para este perfil"));

        CodigoOTP codigo = CodigoOTP.builder()
                .id(otpEntity.getId())
                .perfilId(otpEntity.getPerfilId())
                .codigo(otpEntity.getCodigo())
                .fechaExpiracion(otpEntity.getFechaExpiracion())
                .usado(otpEntity.isUsado())
                .build();

        if (!codigo.esValido(otp)) {
            throw new ConflictoException("Código OTP inválido o expirado");
        }

        // Marcar OTP como usado
        otpEntity.setUsado(true);
        codigoOTPRepository.save(otpEntity);

        // Activar perfil
        PerfilCocinera perfil = perfilMapper.toDomain(entity);
        perfil.marcarVerificada();
        perfilRepository.save(perfilMapper.toEntity(perfil));

        // Activar cuenta
        cuentaRepository.findById(entity.getCuentaId()).ifPresent(cuentaEntity -> {
            Cuenta cuenta = cuentaMapper.toDomain(cuentaEntity);
            cuenta.activar();
            if (cuenta.getCredenciales() != null) {
                cuenta.getCredenciales().setCelularVerificado(true);
            }
            cuentaRepository.save(cuentaMapper.toEntity(cuenta));
        });

        log.info("Teléfono verificado para perfil {}", perfilId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilCocineraResponseDTO obtenerPorCuentaId(UUID cuentaId) {
        PerfilCocineraEntity entity = perfilRepository.findByCuentaId(cuentaId)
                .orElseThrow(() -> new ConflictoException(
                        "No existe perfil asociado a la cuenta ID " + cuentaId));

        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .map(cuentaMapper::toDomain)
                .orElse(null);

        return toResponseDTO(perfilMapper.toDomain(entity), cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilCocineraResponseDTO> listarDestacadas() {
        return perfilRepository.findByEsDestacadaTrue().stream()
                .map(entity -> {
                    Cuenta cuenta = cuentaRepository.findById(entity.getCuentaId())
                            .map(cuentaMapper::toDomain)
                            .orElse(null);
                    return toResponseDTO(perfilMapper.toDomain(entity), cuenta);
                })
                .toList();
    }

    // ============ Helper ============

    private PerfilCocineraResponseDTO toResponseDTO(PerfilCocinera perfil, Cuenta cuenta) {
        return PerfilCocineraResponseDTO.builder()
                .id(perfil.getId())
                .cuentaId(perfil.getCuentaId())
                .nombreCocinera(cuenta != null && cuenta.getIdentidad() != null
                        ? cuenta.getIdentidad().getNombre() : null)
                .presentacion(perfil.getPresentacion())
                .conjuntoResidencial(perfil.getConjuntoResidencial())
                .especialidades(perfil.getEspecialidades())
                .mediosPago(perfil.getMediosPago())
                .numeroNequi(perfil.getNumeroNequi())
                .numeroDaviplata(perfil.getNumeroDaviplata())
                .promedioCalificacion(perfil.getPromedioCalificacion())
                .resenasPositivas(perfil.getResenasPositivas())
                .esDestacada(perfil.getEsDestacada())
                .verificada(perfil.isVerificada())
                .pausada(perfil.isPausada())
                .build();
    }
}