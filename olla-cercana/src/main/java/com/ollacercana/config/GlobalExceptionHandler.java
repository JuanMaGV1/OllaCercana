package com.ollacercana.config;

import com.ollacercana.dto.response.ErrorResponseDTO;
import com.ollacercana.exception.AccesoDenegadoException;
import com.ollacercana.exception.BusinessRuleException;
import com.ollacercana.exception.CocineraNoEncontradaException;
import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

/**
 * Manejo centralizado de errores. Todas las respuestas usan ErrorResponseDTO.
 * 400 solicitud inválida, 403 acceso denegado, 404 no encontrado, 409 conflicto,
 * 422 regla de negocio y 500 error inesperado sin exponer el detalle al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String ERROR_VALIDACION = "Validación fallida";
    private static final String MENSAJE_VALIDACION = "Existen errores en los campos del formulario";
    private static final String MENSAJE_ERROR_INESPERADO =
            "Ocurrió un error inesperado. Intenta de nuevo más tarde";

    // ---------- 404 ----------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(CocineraNoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> handleCocineraNoEncontrada(CocineraNoEncontradaException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    // ---------- 422 ----------

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> handleReglaDeNegocio(ReglaDeNegocioException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI());
    }

    // ---------- 409 ----------

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflicto(ConflictoException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    // ---------- 403 ----------

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccesoDenegado(AccesoDenegadoException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    }

    // ---------- 400 ----------

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDTO> handleHeaderFaltante(MissingRequestHeaderException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Falta el encabezado obligatorio " + ex.getHeaderName(), request.getRequestURI());
    }

    // Antes respondía 500 (por ejemplo, GET /platos/cercanos sin latitud).
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleParametroFaltante(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un formato inválido", request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleCuerpoIlegible(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no es válido: revisa el formato JSON y los valores permitidos",
                request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errores.put(err.getField(), err.getDefaultMessage())
        );
        return buildValidationResponse(errores, request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        ex.getConstraintViolations().forEach(violacion -> {
            String ruta = violacion.getPropertyPath().toString();
            int punto = ruta.lastIndexOf('.');
            String campo = punto >= 0 ? ruta.substring(punto + 1) : ruta;
            errores.put(campo, violacion.getMessage());
        });
        return buildValidationResponse(errores, request.getRequestURI());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneral(Exception ex, HttpServletRequest request) {

        if (ex instanceof ErrorResponse errorSpring) {
            HttpStatus status = HttpStatus.resolve(errorSpring.getStatusCode().value());
            if (status != null) {
                return buildResponse(status, status.getReasonPhrase(), request.getRequestURI());
            }
        }

        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, MENSAJE_ERROR_INESPERADO, request.getRequestURI());
    }

    // ---------- util ----------

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