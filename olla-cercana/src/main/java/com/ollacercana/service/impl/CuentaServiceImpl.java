package com.ollacercana.service.impl;

import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.EstadoCuenta;
import com.ollacercana.mapper.CuentaEntityMapper;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.service.ICuentaService;
import com.ollacercana.validator.ICuentaValidator;
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
    private final CuentaEntityMapper cuentaEntityMapper;
    private final ICuentaValidator cuentaValidator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Cuenta registrar(Cuenta cuenta) {
        log.info("Iniciando proceso de registro de cuenta para correo: {}",
                cuenta.getIdentidad() != null ? cuenta.getIdentidad().getCorreo() : "N/A");

        if (cuenta.getIdentidad() != null) {
            cuentaValidator.validarCorreoUnico(cuenta.getIdentidad().getCorreo());
            cuentaValidator.validarCelularUnico(cuenta.getIdentidad().getCelular());
        }

        if (cuenta.getCredenciales() != null) {
            cuentaValidator.validarPasswordSegura(cuenta.getCredenciales().getContrasenaHash());

            String passwordPlana = cuenta.getCredenciales().getContrasenaHash();
            String passwordHasheada = passwordEncoder.encode(passwordPlana);
            cuenta.getCredenciales().setContrasenaHash(passwordHasheada);
        }

        Cuenta entidad = cuentaEntityMapper.toEntity(cuenta);
        Cuenta guardada = cuentaRepository.save(entidad);

        log.info("Cuenta registrada exitosamente con ID: {}", guardada.getId());

        return cuentaEntityMapper.toDomain(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Cuenta autenticar(String identificador, String password) {
        Cuenta cuenta = cuentaRepository.findByIdentificador(identificador)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

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