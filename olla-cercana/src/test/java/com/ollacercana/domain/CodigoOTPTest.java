package com.ollacercana.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodigoOTPTest {

    @Test
    @DisplayName("esValido verifica estado de uso, coincidencia de código y no expiración")
    void esValido_todasLasRamas() {
        CodigoOTP otp = CodigoOTP.builder()
                .perfilId(UUID.randomUUID())
                .codigo("123456")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

                 
        assertTrue(otp.esValido("123456"));

                            
        assertFalse(otp.esValido("000000"));

                   
        otp.setUsado(true);
        assertFalse(otp.esValido("123456"));
        otp.setUsado(false);

                   
        otp.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));
        assertFalse(otp.esValido("123456"));
    }
}