package com.ollacercana.controller.handlers;

import com.ollacercana.controller.dtos.response.ErrorResponseDTO;
import com.ollacercana.controller.handlers.exception.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

   
                                                                              
   
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String ERROR_VALIDACION = "Validación fallida";
    private static final String MENSAJE_VALIDACION = "Existen errores en los campos del formulario";
    private static final String MENSAJE_ERROR_INESPERADO = "Error inesperado del servidor";

                              
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(CocineraNoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> handleCocineraNoEncontrada(CocineraNoEncontradaException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

                              
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> handleReglaDeNegocio(ReglaDeNegocioException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI());
    }

                              
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflicto(ConflictoException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

                                          
    @ExceptionHandler({AccesoDenegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponseDTO> handleAccesoDenegado(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        String mensaje = path.startsWith("/api/v1/admin")
                ? "No tiene permisos para ver esta sección"
                : ex.getMessage();
        return buildResponse(HttpStatus.FORBIDDEN, mensaje, path);
    }

                                          
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponseDTO> handleAutenticacion(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", request.getRequestURI());
    }

                              
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDTO> handleHeaderFaltante(MissingRequestHeaderException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Falta el encabezado obligatorio " + ex.getHeaderName(), request.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleParametroFaltante(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' tiene un formato inválido", request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleCuerpoIlegible(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es válido: revisa el formato JSON y los valores permitidos", request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String campo = propertyPath.contains(".")
                    ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                    : propertyPath;
            errores.put(campo, violation.getMessage());
        }
        return buildValidationResponse(errores, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errores.put(err.getField(), err.getDefaultMessage())
        );
        return buildValidationResponse(errores, request.getRequestURI());
    }

                              
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneral(Exception ex, HttpServletRequest request) {
                                                                                                     
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.resolve(errorResponse.getStatusCode().value());
            return buildResponse(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
        }
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, MENSAJE_ERROR_INESPERADO, request.getRequestURI());
    }

                                 

    private ResponseEntity<ErrorResponseDTO> buildValidationResponse(Map<String, String> errores, String path) {
        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(ERROR_VALIDACION)
                .mensaje(MENSAJE_VALIDACION)
                .message(MENSAJE_VALIDACION)
                .detalles(errores)
                .path(path)
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status, String mensaje, String path) {
        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .mensaje(mensaje)
                .message(mensaje)
                .path(path)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}