package com.ollacercana.security;

import com.ollacercana.domain.Cuenta;
import com.ollacercana.domain.Identidad;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.domain.Rol;
import com.ollacercana.repository.PerfilCocineraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceUnitTest {

    private static final String SECRET_TEST = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @Mock
    private PerfilCocineraRepository perfilCocineraRepository;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_TEST, 3600000L, perfilCocineraRepository);
    }

    @Test
    @DisplayName("Token válido: genera, valida y extrae claims correctamente")
    void generarToken_tokenValido_extraeClaims() {
                  
        UUID cocineraId = UUID.randomUUID();
        Cuenta cuenta = Cuenta.builder()
                .id(7L)
                .identidad(Identidad.builder().correo("chef@ollacercana.com").build())
                .roles(Set.of(Rol.COCINERA))
                .build();
        when(perfilCocineraRepository.findByCuentaId(7L))
                .thenReturn(Optional.of(PerfilCocinera.builder().id(cocineraId).build()));

              
        String token = jwtService.generarToken(cuenta);

                 
        assertNotNull(token);
        assertTrue(jwtService.validarToken(token));
        assertEquals("7", jwtService.extraerSubject(token));
        assertEquals(7L, jwtService.extraerCuentaId(token));
        assertEquals("chef@ollacercana.com", jwtService.extraerCorreo(token));
        assertEquals(cocineraId, jwtService.extraerCocineraId(token));
        assertTrue(jwtService.extraerRoles(token).contains("COCINERA"));
    }

    @Test
    @DisplayName("Token expirado: retorna false en validarToken")
    void validarToken_tokenExpirado_retornaFalse() {
                                                   
        JwtService servicioExpirado = new JwtService(SECRET_TEST, -1000L, perfilCocineraRepository);
        Cuenta cuenta = Cuenta.builder().id(1L).roles(Set.of(Rol.COMPRADOR)).build();
        when(perfilCocineraRepository.findByCuentaId(1L)).thenReturn(Optional.empty());

              
        String tokenExpirado = servicioExpirado.generarToken(cuenta);

                 
        assertFalse(jwtService.validarToken(tokenExpirado));
    }

    @Test
    @DisplayName("Token alterado: firma no coincide y retorna false")
    void validarToken_tokenAlterado_retornaFalse() {
                  
        Cuenta cuenta = Cuenta.builder().id(1L).roles(Set.of(Rol.COMPRADOR)).build();
        when(perfilCocineraRepository.findByCuentaId(1L)).thenReturn(Optional.empty());
        String tokenOriginal = jwtService.generarToken(cuenta);

                                                  
        String tokenAlterado = tokenOriginal.substring(0, tokenOriginal.length() - 4) + "XXXX";

                 
        assertFalse(jwtService.validarToken(tokenAlterado));
    }

    @Test
    @DisplayName("Token malformado: no cumple sintaxis JWT y retorna false")
    void validarToken_tokenMalformado_retornaFalse() {
                                 
        assertFalse(jwtService.validarToken("token.invalido.malformado"));
    }

    @Test
    @DisplayName("Token vacío o nulo: retorna false sin lanzar excepción")
    void validarToken_tokenVacioONulo_retornaFalse() {
                                 
        assertFalse(jwtService.validarToken(""));
        assertFalse(jwtService.validarToken(null));
    }
}