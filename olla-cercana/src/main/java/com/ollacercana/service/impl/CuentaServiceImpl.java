package com.ollacercana.service.impl;

import com.ollacercana.exception.ConflictoException;
import com.ollacercana.mapper.CuentaEntityMapper;
import com.ollacercana.model.domain.Cuenta;
import com.ollacercana.model.domain.EstadoCuenta;
import com.ollacercana.persistence.entity.CuentaEntity;
import com.ollacercana.repository.CuentaRepository;
import com.ollacercana.service.ICuentaService;
import com.ollacercana.validator.ICuentaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
        log.info("Registrando cuenta: {}",
                cuenta.getIdentidad() != null ? cuenta.getIdentidad().getCorreo() : "N/A");

        // 1. Validaciones
        if (cuenta.getIdentidad() != null) {
            cuentaValidator.validarCorreoUnico(cuenta.getIdentidad().getCorreo());
            cuentaValidator.validarCelularUnico(cuenta.getIdentidad().getCelular());
        }

        // 2. Hashear contraseña e inicializar celularVerificado
        if (cuenta.getCredenciales() != null) {
            cuentaValidator.validarPasswordSegura(cuenta.getCredenciales().getContrasenaHash());

            String passwordPlana = cuenta.getCredenciales().getContrasenaHash();
            cuenta.getCredenciales().setContrasenaHash(passwordEncoder.encode(passwordPlana));

        }

        // 3. Inicializar campos del sistema
        if (cuenta.getId() == null) {
            cuenta.setId(UUID.randomUUID());
        }
        if (cuenta.getEstado() == null) {
            cuenta.setEstado(EstadoCuenta.PENDIENTE_VERIFICACION);
        }
        if (cuenta.getFechaRegistro() == null) {
            cuenta.setFechaRegistro(LocalDateTime.now());
        }

        // 4. Persistir
        CuentaEntity entity = cuentaEntityMapper.toEntity(cuenta);
        CuentaEntity guardada = cuentaRepository.save(entity);

        log.info("Cuenta registrada con ID: {}", guardada.getId());
        return cuentaEntityMapper.toDomain(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Cuenta autenticar(String identificador, String password) {
        CuentaEntity entity = cuentaRepository.findByCorreoOrCelular(identificador, identificador)
                .orElseThrow(() -> new ConflictoException("Credenciales inválidas"));

        Cuenta cuenta = cuentaEntityMapper.toDomain(entity);

        if (cuenta.getEstado() == EstadoCuenta.BLOQUEADA_TEMPORAL
                || cuenta.getEstado() == EstadoCuenta.SUSPENDIDA) {
            throw new ConflictoException("La cuenta no está disponible");
        }

        if (cuenta.getCredenciales() == null
                || !passwordEncoder.matches(password, cuenta.getCredenciales().getContrasenaHash())) {
            throw new ConflictoException("Credenciales inválidas");
        }

        return cuenta;
    }
}