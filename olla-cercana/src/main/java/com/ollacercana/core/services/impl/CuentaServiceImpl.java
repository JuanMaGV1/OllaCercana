package com.ollacercana.core.services.impl;

import com.ollacercana.controller.handlers.exception.ConflictoException;
import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.enums.EstadoCuenta;
import com.ollacercana.core.services.ICuentaService;
import com.ollacercana.core.validators.ICuentaValidator;
import com.ollacercana.persistence.entities.CuentaEntity;
import com.ollacercana.persistence.mappers.CuentaEntityMapper;
import com.ollacercana.persistence.repository.CuentaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements ICuentaService {

    private final CuentaRepository cuentaRepository;
    private final ICuentaValidator cuentaValidator;
    private final PasswordEncoder passwordEncoder;
    private final CuentaEntityMapper entityMapper;

    @Override
    @Transactional
    public Cuenta registrar(Cuenta cuenta) {
        log.info("Registrando cuenta para correo: {}",
                cuenta.getIdentidad() != null ? cuenta.getIdentidad().getCorreo() : "N/A");

        if (cuenta.getIdentidad() != null) {
            cuentaValidator.validarCorreoUnico(cuenta.getIdentidad().getCorreo());
            cuentaValidator.validarCelularUnico(cuenta.getIdentidad().getCelular());
        }

        if (cuenta.getCredenciales() != null) {
            cuentaValidator.validarPasswordSegura(cuenta.getCredenciales().getContrasenaHash());
            String hasheada = passwordEncoder.encode(cuenta.getCredenciales().getContrasenaHash());
            cuenta.getCredenciales().setContrasenaHash(hasheada);
        }

        cuenta.inicializar();  // ← asigna defaults de dominio

        CuentaEntity guardada = cuentaRepository.save(entityMapper.toEntity(cuenta));
        return entityMapper.toDomain(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Cuenta autenticar(String identificador, String password) {
        CuentaEntity entity = cuentaRepository.findByIdentificador(identificador)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        Cuenta cuenta = entityMapper.toDomain(entity);

        if (cuenta.getEstado() == EstadoCuenta.BLOQUEADO) {
            throw new LockedException("La cuenta se encuentra bloqueada");
        }
        if (cuenta.getCredenciales() == null ||
                !passwordEncoder.matches(password, cuenta.getCredenciales().getContrasenaHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        return cuenta;
    }
}