package com.ollacercana.validator;

import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CuentaValidator implements ICuentaValidator {

    private final CuentaRepository cuentaRepository;

    // Regla: mínimo 8 caracteres, al menos una letra y un número
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    @Override
    public void validarCorreoUnico(String correo) {
        if (correo != null && cuentaRepository.existsByCorreo(correo)) {
            throw new ConflictoException("El correo " + correo + " ya se encuentra registrado en el sistema");
        }
    }

    @Override
    public void validarCelularUnico(String celular) {
        if (celular != null && cuentaRepository.existsByCelular(celular)) {
            throw new ConflictoException("El número celular " + celular + " ya se encuentra registrado en el sistema");
        }
    }

    @Override
    public void validarPasswordSegura(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ReglaDeNegocioException("La contraseña debe tener al menos 8 caracteres, e incluir letras y números");
        }
    }
}