package com.ollacercana.controller.dtos.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OC-231: pruebas de validación del DTO de consulta del mapa de cocineras.
 */
class MapaCocinerasRequestDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("OC-231: DTO válido no tiene violaciones")
    void valido_sinViolaciones() {
        MapaCocinerasRequestDTO dto = MapaCocinerasRequestDTO.builder()
                .latitud(4.6789).longitud(-74.0567).radio(3000.0)
                .build();

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("OC-231: latitud fuera de rango → violación")
    void latitudFueraDeRango() {
        MapaCocinerasRequestDTO dto = MapaCocinerasRequestDTO.builder()
                .latitud(100.0).longitud(-74.0567).radio(3000.0).build();

        Set<ConstraintViolation<MapaCocinerasRequestDTO>> v = validator.validate(dto);
        assertFalse(v.isEmpty());
        assertTrue(v.stream().anyMatch(c -> c.getPropertyPath().toString().equals("latitud")));
    }

    @Test
    @DisplayName("OC-231: longitud fuera de rango → violación")
    void longitudFueraDeRango() {
        MapaCocinerasRequestDTO dto = MapaCocinerasRequestDTO.builder()
                .latitud(4.67).longitud(-200.0).radio(3000.0).build();

        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("OC-231: radio negativo → violación")
    void radioNegativo() {
        MapaCocinerasRequestDTO dto = MapaCocinerasRequestDTO.builder()
                .latitud(4.67).longitud(-74.05).radio(-10.0).build();

        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("OC-231: radio superior al máximo → violación")
    void radioExcedeMaximo() {
        MapaCocinerasRequestDTO dto = MapaCocinerasRequestDTO.builder()
                .latitud(4.67).longitud(-74.05)
                .radio(MapaCocinerasRequestDTO.RADIO_MAXIMO_METROS + 1.0)
                .build();

        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("OC-231: radio en el máximo permitido → válido")
    void radioEnElMaximo() {
        MapaCocinerasRequestDTO dto = MapaCocinerasRequestDTO.builder()
                .latitud(4.67).longitud(-74.05)
                .radio(MapaCocinerasRequestDTO.RADIO_MAXIMO_METROS)
                .build();

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    @DisplayName("OC-231: campos nulos → violaciones")
    void camposNulos() {
        MapaCocinerasRequestDTO dto = new MapaCocinerasRequestDTO();
        assertEquals(3, validator.validate(dto).size());
    }
}