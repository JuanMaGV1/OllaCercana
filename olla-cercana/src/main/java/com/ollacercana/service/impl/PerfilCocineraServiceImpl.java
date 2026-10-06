package com.ollacercana.service.impl;

import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ResourceNotFoundException;
import com.ollacercana.mapper.CuentaEntityMapper;
import com.ollacercana.mapper.PerfilCocineraDomainMapper;
import com.ollacercana.mapper.PerfilCocineraPersistenceMapper;
import com.ollacercana.model.domain.CodigoOTP;
import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.model.domain.PerfilCocinera;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilCocineraServiceImpl implements IPerfilCocineraService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PerfilCocineraRepository perfilRepository;
    private final CuentaRepository cuentaRepository;
    private final CodigoOTPRepository codigoOTPRepository;
    private final IPerfilCocineraValidator validator;
    private final PerfilCocineraDomainMapper domainMapper;        // Entity → Dominio
    private final PerfilCocineraPersistenceMapper persistenceMapper; // Dominio → Entity
    private final CuentaEntityMapper cuentaMapper;

    @Override
    @Transactional
    public PerfilCocinera crearPerfil(PerfilCocinera perfil, Long cuentaId) {
        CuentaEntity cuentaEntity = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new ConflictoException("La cuenta asociada con ID " + cuentaId + " no existe"));
        Cuenta cuenta = cuentaMapper.toDomain(cuentaEntity);

        validator.validarParaCrear(perfil, cuenta);

        PerfilCocineraEntity entity = persistenceMapper.toEntity(perfil);
        entity.setCuenta(cuentaEntity);
        PerfilCocineraEntity guardado = perfilRepository.save(entity);

        String codigo = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        CodigoOTPEntity otp = CodigoOTPEntity.builder()
                .perfilId(guardado.getId())
                .codigo(codigo)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .usado(false)
                .build();
        codigoOTPRepository.save(otp);

        return domainMapper.toDomain(guardado);
    }

    @Override
    @Transactional
    public PerfilCocinera actualizarPerfil(UUID id, PerfilCocinera perfilActualizado) {
        PerfilCocineraEntity existente = perfilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PerfilCocinera", id));

        validator.validarParaActualizar(id, perfilActualizado);

        existente.setPresentacion(perfilActualizado.getPresentacion());
        existente.setConjuntoResidencial(perfilActualizado.getConjuntoResidencial());
        existente.setEspecialidades(perfilActualizado.getEspecialidades());
        existente.setMediosPago(perfilActualizado.getMediosPago());
        existente.setNumeroNequi(perfilActualizado.getNumeroNequi());
        existente.setNumeroDaviplata(perfilActualizado.getNumeroDaviplata());

        return domainMapper.toDomain(perfilRepository.save(existente));
    }

    @Override
    @Transactional
    public boolean verificarTelefono(UUID perfilId, String otp) {
        PerfilCocineraEntity perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new ResourceNotFoundException("PerfilCocinera", perfilId));

        CodigoOTPEntity otpEntity = codigoOTPRepository
                .findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId)
                .orElseThrow(() -> new ConflictoException("No hay ningún código OTP activo para este perfil"));

        CodigoOTP codigoDominio = CodigoOTP.builder()
                .codigo(otpEntity.getCodigo())
                .fechaExpiracion(otpEntity.getFechaExpiracion())
                .usado(otpEntity.isUsado())
                .build();

        if (!codigoDominio.esValido(otp)) {
            throw new ConflictoException("El código OTP ingresado es inválido o ya ha expirado");
        }

        otpEntity.setUsado(true);
        codigoOTPRepository.save(otpEntity);

        perfil.setVerificada(true);
        if (perfil.getCuenta() != null && perfil.getCuenta().getCredenciales() != null) {
            perfil.getCuenta().getCredenciales().setCelularVerificado(true);
        }
        perfilRepository.save(perfil);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilCocinera obtenerPorCuentaId(Long cuentaId) {
        return perfilRepository.findByCuentaId(cuentaId)
                .map(domainMapper::toDomain)
                .orElseThrow(() -> new ConflictoException("No existe perfil para la cuenta ID " + cuentaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilCocinera> listarDestacadas() {
        return perfilRepository.findByEsDestacadaTrue().stream()
                .map(domainMapper::toDomain)
                .toList();
    }
}