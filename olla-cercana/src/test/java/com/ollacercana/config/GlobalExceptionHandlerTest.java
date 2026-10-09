package com.ollacercana.config;

import com.ollacercana.controller.dtos.response.ErrorResponseDTO;
import com.ollacercana.controller.handlers.exception.AccesoDenegadoException;
import com.ollacercana.controller.handlers.exception.AutoReservaException;
import com.ollacercana.controller.handlers.exception.ConflictoException;
import com.ollacercana.controller.handlers.exception.PlatoNoEncontradoException;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.controller.handlers.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter. api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private static final String RUTA = "/api/v1/prueba";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", RUTA);

    @Test
    @DisplayName("Recurso no encontrado responde 404 con el formato uniforme")
    void recursoNoEncontrado_Retorna404() {
                  
        UUID id = UUID.randomUUID();
        var ex = new PlatoNoEncontradoException(id);

              
        ResponseEntity<ErrorResponseDTO> resp = handler.handleNotFound(ex, request);

                 
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        ErrorResponseDTO cuerpo = resp.getBody();
        assertNotNull(cuerpo);
        assertEquals(404, cuerpo.getStatus());
        assertEquals("Not Found", cuerpo.getError());
        assertTrue(cuerpo.getMessage().contains(id.toString()));
        assertEquals(cuerpo.getMessage(), cuerpo.getMensaje());
        assertEquals(RUTA, cuerpo.getPath());
        assertNotNull(cuerpo.getTimestamp());
        assertNull(cuerpo.getDetalles());
    }

    @Test
    @DisplayName("Conflicto responde 409")
    void conflicto_Retorna409() {
                  
        var ex = new ConflictoException("El correo ya se encuentra registrado");

              
        ResponseEntity<ErrorResponseDTO> resp = handler.handleConflicto(ex, request);

                 
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(409, resp.getBody().getStatus());
        assertEquals("El correo ya se encuentra registrado", resp.getBody().getMensaje());
    }

    @Test
    @DisplayName("Regla de negocio responde 422")
    void reglaDeNegocio_Retorna422() {
                  
        var ex = new ReglaDeNegocioException("Regla incumplida");

              
        ResponseEntity<ErrorResponseDTO> resp = handler.handleReglaDeNegocio(ex, request);

                 
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(422, resp.getBody().getStatus());
        assertEquals("Regla incumplida", resp.getBody().getMessage());
    }

    @Test
    @DisplayName("BusinessRuleException (autoreserva, RN-14) responde 422")
    void businessRule_Retorna422() {
                  
        var ex = new AutoReservaException();

              
        ResponseEntity<ErrorResponseDTO> resp = handler.handleBusinessRule(ex, request);

                 
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().getMessage().contains("RN-14"));
    }

    @Test
    @DisplayName("Acceso denegado responde 403")
    void accesoDenegado_Retorna403() {
                  
        var ex = new AccesoDenegadoException("No tienes permiso sobre este recurso");

              
        ResponseEntity<ErrorResponseDTO> resp = handler.handleAccesoDenegado(ex, request);

                 
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(403, resp.getBody().getStatus());
    }

    @Test
    @DisplayName("Validación del cuerpo responde 400 con los campos fallidos en detalles")
    void validacionDelCuerpo_Retorna400ConDetalles() throws Exception {
                  
        var metodo = ControladorFalso.class.getDeclaredMethod("crear", Object.class);
        var parametro = new MethodParameter(metodo, 0);
        var resultado = new BeanPropertyBindingResult(new Object(), "solicitud");
        resultado.addError(new FieldError("solicitud", "radio", "debe estar entre 500 y 2000"));
        var ex = new MethodArgumentNotValidException(parametro, resultado);

              
        ResponseEntity<ErrorResponseDTO> resp = handler.handleValidation(ex, request);

                 
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ErrorResponseDTO cuerpo = resp.getBody();
        assertNotNull(cuerpo);
        assertEquals("Validación fallida", cuerpo.getError());
        assertEquals("debe estar entre 500 y 2000", cuerpo.getDetalles().get("radio"));
    }

    @Test
    @DisplayName("Validación de parámetros (ConstraintViolation) responde 400 en lugar de 500")
    void validacionDeParametros_Retorna400() {
                  
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violacion = mock(ConstraintViolation.class);
        Path ruta = mock(Path.class);
        when(ruta.toString()).thenReturn("buscarCercanos.radio");
        when(violacion.getPropertyPath()).thenReturn(ruta);
        when(violacion.getMessage()).thenReturn("debe ser mayor o igual a 500");
        var ex = new ConstraintViolationException(Set.of(violacion));

              
        ResponseEntity<ErrorResponseDTO> resp = handler.handleConstraintViolation(ex, request);

                 
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("debe ser mayor o igual a 500", resp.getBody().getDetalles().get("radio"));
    }

    @Test
    @DisplayName("Parámetro obligatorio ausente responde 400 en lugar de 500")
    void parametroFaltante_Retorna400() {
                  
        var ex = new MissingServletRequestParameterException("latitud", "Double");

              
        ResponseEntity<ErrorResponseDTO> resp = handler.handleParametroFaltante(ex, request);

                 
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().getMessage().contains("latitud"));
    }

    @Test
    @DisplayName("Cuerpo JSON ilegible responde 400")
    void cuerpoIlegible_Retorna400() {
                  
        var ex = new HttpMessageNotReadableException(
                "JSON mal formado", new MockHttpInputMessage(new byte[0]));

              
        ResponseEntity<ErrorResponseDTO> resp = handler.handleCuerpoIlegible(ex, request);

                 
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(400, resp.getBody().getStatus());
    }

    @Test
    @DisplayName("OC-252: Deserializacion de enum invalido responde 400 y lista los valores permitidos")
    void cuerpoIlegible_conEnumInvalido_Retorna400YListaValoresPermitidos(){
        // Arrange
        com.fasterxml.jackson.databind.JsonMappingException.Reference ref =
                new com.fasterxml.jackson.databind.JsonMappingException.Reference(null, "medioPago");
        var ifx = com.fasterxml.jackson.databind.exc.InvalidFormatException.from(
                null, "Cannot deserialize value", "BITCOIN", com.ollacercana.core.models.enums.MedioPago.class);
        ifx.prependPath(ref);
        var ex = new HttpMessageNotReadableException("JSON parse error", ifx, new MockHttpInputMessage(new byte[0]));

        // Act
        ResponseEntity<ErrorResponseDTO> resp = handler.handleCuerpoIlegible(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(400, resp.getBody().getStatus());
        String msg = resp.getBody().getMessage();
        assertTrue(msg.contains("Valor inválido 'BITCOIN' para el campo 'medioPago'"));
        assertTrue(msg.contains("Valores permitidos: [NEQUI, DAVIPLATA, EFECTIVO, TRANSFERENCIA_BANCARIA]"));
    }

    @Test
    @DisplayName("OC-255: MedioPagoNoAceptadoException responde 400 Bad Request")
    void medioPagoNoAceptado_Retorna400() {
        var ex = new com.ollacercana.controller.handlers.exception.MedioPagoNoAceptadoException(com.ollacercana.core.models.enums.MedioPago.DAVIPLATA);
        ResponseEntity<ErrorResponseDTO> resp = handler.handleMedioPagoNoAceptado(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(400, resp.getBody().getStatus());
        assertTrue(resp.getBody().getMessage().contains("DAVIPLATA"));
    }

    @Test
    @DisplayName("Error de Spring MVC (405) conserva su código en lugar de volverse 500")
    void errorDeSpringMvc_ConservaSuCodigo() {
                  
        var ex = new HttpRequestMethodNotSupportedException("DELETE");

              
        ResponseEntity<ErrorResponseDTO> resp = handler.handleGeneral(ex, request);

                 
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(405, resp.getBody().getStatus());
    }

    @Test
    @DisplayName("Error inesperado responde 500 sin exponer el detalle interno")
    void errorInesperado_Retorna500SinExponerDetalle() {
                  
        var ex = new RuntimeException("password=secreto en la base de datos");

              
        ResponseEntity<ErrorResponseDTO> resp = handler.handleGeneral(ex, request);

                 
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        ErrorResponseDTO cuerpo = resp.getBody();
        assertNotNull(cuerpo);
        assertEquals(500, cuerpo.getStatus());
        assertFalse(cuerpo.getMessage().contains("secreto"));
        assertFalse(cuerpo.getMensaje().contains("secreto"));
    }

                                                                                          
    private static class ControladorFalso {
        @SuppressWarnings("unused")
        void crear(Object cuerpo) {
        }
    }
}