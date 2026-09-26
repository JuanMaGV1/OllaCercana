package com.ollacercana.service;

import com.ollacercana.domain.Cuenta;
import com.ollacercana.mapper.CuentaEntityMapper;
import com.ollacercana.repository.CuentaRepository;
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
}