package com.ollacercana.service.impl;

import com.ollacercana.domain.CodigoOTP;
import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ResourceNotFoundException;
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

    @Override
    @Transactional
    public PerfilCocinera crearPerfil(PerfilCocinera perfil, Long cuentaId) {
        log.info("Creando perfil de cocinera para cuentaId: {}", cuentaId);
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new ConflictoException("La cuenta asociada con ID " + cuentaId + " no existe"));

        validator.validarParaCrear(perfil, cuenta);
        perfil.setCuenta(cuenta);

        PerfilCocinera guardado = perfilRepository.save(perfil);

        String codigoGenerado = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        CodigoOTP otp = CodigoOTP.builder()
                .perfilId(guardado.getId())
                .codigo(codigoGenerado)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .usado(false)
                .build();
        codigoOTPRepository.save(otp);
        log.info("OTP generado para perfil {}: {}", guardado.getId(), codigoGenerado);

        return guardado;
    }

    @Override
    @Transactional
    public PerfilCocinera actualizarPerfil(UUID id, PerfilCocinera perfilActualizado) {
        PerfilCocinera perfilExistente = perfilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PerfilCocinera", id));

        validator.validarParaActualizar(id, perfilActualizado);

        perfilExistente.setPresentacion(perfilActualizado.getPresentacion());
        perfilExistente.setConjuntoResidencial(perfilActualizado.getConjuntoResidencial());
        perfilExistente.setEspecialidades(perfilActualizado.getEspecialidades());
        perfilExistente.setMediosPago(perfilActualizado.getMediosPago());
        perfilExistente.setNumeroNequi(perfilActualizado.getNumeroNequi());
        perfilExistente.setNumeroDaviplata(perfilActualizado.getNumeroDaviplata());

        return perfilRepository.save(perfilExistente);
    }

    @Override
    @Transactional
    public boolean verificarTelefono(UUID perfilId, String otp) {
        PerfilCocinera perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new ResourceNotFoundException("PerfilCocinera", perfilId));

        CodigoOTP codigoOTP = codigoOTPRepository.findTopByPerfilIdAndUsadoFalseOrderByFechaExpiracionDesc(perfilId)
                .orElseThrow(() -> new ConflictoException("No hay ningún código OTP activo para este perfil"));

        if (!codigoOTP.esValido(otp)) {
            throw new ConflictoException("El código OTP ingresado es inválido o ya ha expirado");
        }

        codigoOTP.setUsado(true);
        codigoOTPRepository.save(codigoOTP);

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
                .orElseThrow(() -> new ConflictoException("No existe perfil para la cuenta ID " + cuentaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilCocinera> listarDestacadas() {
        return perfilRepository.findByEsDestacadaTrue();
    }
}