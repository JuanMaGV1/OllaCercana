package com.ollacercana.service.impl;

import com.ollacercana.exception.ConflictoException;
import com.ollacercana.mapper.CuentaEntityMapper;
import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.model.domain.EstadoCuenta;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.service.ICuentaService;
import com.ollacercana.validator.ICuentaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
        // 1. Buscar por correo o celular
        Cuenta cuenta = cuentaRepository.findByIdentificador(identificador)
                .orElseThrow(() -> new ConflictoException("Credenciales inválidas"));

        // 2. Validar si la cuenta está bloqueada
        if (cuenta.getEstado() == EstadoCuenta.BLOQUEADO) {
            throw new ConflictoException("La cuenta se encuentra bloqueada");
        }

        // 3. Comparar el hash con BCrypt
        if (cuenta.getCredenciales() == null ||
                !passwordEncoder.matches(password, cuenta.getCredenciales().getContrasenaHash())) {
            throw new ConflictoException("Credenciales inválidas");
        }

        return cuenta;
    }
}