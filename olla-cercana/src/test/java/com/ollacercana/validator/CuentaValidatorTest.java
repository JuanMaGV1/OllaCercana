package com.ollacercana.validator;

import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.repository.CuentaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaValidatorTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private CuentaValidator validator;

    @Nested
    @DisplayName("Validación de Correo Único")
    class ValidarCorreoTests {

        @Test
        @DisplayName("Correo null no consulta repositorio ni lanza excepción")
        void validarCorreoUnico_correoNull_noHaceNada() {
            assertDoesNotThrow(() -> validator.validarCorreoUnico(null));
            verify(cuentaRepository, never()).existsByCorreo(any());
        }

        @Test
        @DisplayName("Correo no existente es válido")
        void validarCorreoUnico_noExiste_pasaExitoso() {
            when(cuentaRepository.existsByCorreo("nuevo@correo.com")).thenReturn(false);
            assertDoesNotThrow(() -> validator.validarCorreoUnico("nuevo@correo.com"));
        }

        @Test
        @DisplayName("Correo duplicado lanza ConflictoException")
        void validarCorreoUnico_yaExiste_lanzaConflictoException() {
            when(cuentaRepository.existsByCorreo("duplicado@correo.com")).thenReturn(true);

            ConflictoException ex = assertThrows(ConflictoException.class,
                    () -> validator.validarCorreoUnico("duplicado@correo.com"));
            assertTrue(ex.getMessage().contains("duplicado@correo.com"));
        }
    }

    @Nested
    @DisplayName("Validación de Celular Único")
    class ValidarCelularTests {

        @Test
        @DisplayName("Celular null no consulta repositorio ni lanza excepción")
        void validarCelularUnico_celularNull_noHaceNada() {
            assertDoesNotThrow(() -> validator.validarCelularUnico(null));
            verify(cuentaRepository, never()).existsByCelular(any());
        }

        @Test
        @DisplayName("Celular no existente es válido")
        void validarCelularUnico_noExiste_pasaExitoso() {
            when(cuentaRepository.existsByCelular("3001234567")).thenReturn(false);
            assertDoesNotThrow(() -> validator.validarCelularUnico("3001234567"));
        }

        @Test
        @DisplayName("Celular duplicado lanza ConflictoException")
        void validarCelularUnico_yaExiste_lanzaConflictoException() {
            when(cuentaRepository.existsByCelular("3001234567")).thenReturn(true);

            ConflictoException ex = assertThrows(ConflictoException.class,
                    () -> validator.validarCelularUnico("3001234567"));
            assertTrue(ex.getMessage().contains("3001234567"));
        }
    }

    @Nested
    @DisplayName("Validación de Contraseña Segura")
    class ValidarPasswordTests {

        @Test
        @DisplayName("Contraseña null lanza ReglaDeNegocioException")
        void validarPasswordSegura_null_lanzaReglaDeNegocio() {
            assertThrows(ReglaDeNegocioException.class, () -> validator.validarPasswordSegura(null));
        }

        @Test
        @DisplayName("Contraseña con menos de 8 caracteres lanza ReglaDeNegocioException")
        void validarPasswordSegura_corta_lanzaReglaDeNegocio() {
            assertThrows(ReglaDeNegocioException.class, () -> validator.validarPasswordSegura("Abc12"));
        }

        @Test
        @DisplayName("Contraseña sin números lanza ReglaDeNegocioException")
        void validarPasswordSegura_sinNumeros_lanzaReglaDeNegocio() {
            assertThrows(ReglaDeNegocioException.class, () -> validator.validarPasswordSegura("SoloLetrasSinDigito"));
        }

        @Test
        @DisplayName("Contraseña solo números lanza ReglaDeNegocioException")
        void validarPasswordSegura_sinLetras_lanzaReglaDeNegocio() {
            assertThrows(ReglaDeNegocioException.class, () -> validator.validarPasswordSegura("1234567890"));
        }

        @Test
        @DisplayName("Contraseña válida (>= 8 caracteres, letras y números) pasa exitosamente")
        void validarPasswordSegura_valida_pasaExitoso() {
            assertDoesNotThrow(() -> validator.validarPasswordSegura("Password123"));
            assertDoesNotThrow(() -> validator.validarPasswordSegura("c0ntrasenaSegura"));
        }
    }
}