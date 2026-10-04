package com.ollacercana.validator;

import com.ollacercana.domain.*;
import com.ollacercana.exception.ConflictoException;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.repository.PerfilCocineraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilCocineraValidatorTest {

    @Mock
    private PerfilCocineraRepository perfilCocineraRepository;

    @InjectMocks
    private PerfilCocineraValidator validator;

    private Cuenta cuenta;
    private PerfilCocinera perfil;

    @BeforeEach
    void setUp() {
        cuenta = Cuenta.builder()
                .id(1L)
                .roles(Set.of(Rol.COCINERA))
                .build();

        perfil = PerfilCocinera.builder()
                .conjuntoResidencial("Torres del Parque")
                .mediosPago(new ArrayList<>(List.of(MedioPago.NEQUI, MedioPago.DAVIPLATA)))
                .numeroNequi("3001234567")
                .numeroDaviplata("3007654321")
                .build();
    }

    @Test
    @DisplayName("validarParaCrear: Conjunto null o vacío lanza ConflictoException")
    void validarParaCrear_conjuntoInvalido_lanzaConflicto() {
        perfil.setConjuntoResidencial(null);
        assertThrows(ConflictoException.class, () -> validator.validarParaCrear(perfil, cuenta));

        perfil.setConjuntoResidencial("    ");
        assertThrows(ConflictoException.class, () -> validator.validarParaCrear(perfil, cuenta));
    }

    @Test
    @DisplayName("validarParaCrear: Cuenta sin rol COCINERA lanza ReglaDeNegocioException")
    void validarParaCrear_sinRolCocinera_lanzaReglaDeNegocio() {
        cuenta.setRoles(null);
        assertThrows(ReglaDeNegocioException.class, () -> validator.validarParaCrear(perfil, cuenta));

        cuenta.setRoles(Set.of(Rol.COMPRADOR));
        assertThrows(ReglaDeNegocioException.class, () -> validator.validarParaCrear(perfil, cuenta));
    }

    @Test
    @DisplayName("validarParaCrear: Cuenta ya tiene perfil asociado lanza ConflictoException")
    void validarParaCrear_perfilYaExistente_lanzaConflicto() {
        when(perfilCocineraRepository.findByCuentaId(cuenta.getId()))
                .thenReturn(Optional.of(new PerfilCocinera()));

        assertThrows(ConflictoException.class, () -> validator.validarParaCrear(perfil, cuenta));
    }

    @Test
    @DisplayName("validarParaCrear: Medios de pago null o vacíos lanza ReglaDeNegocioException")
    void validarParaCrear_mediosPagoVacios_lanzaReglaDeNegocio() {
        perfil.setMediosPago(null);
        assertThrows(ReglaDeNegocioException.class, () -> validator.validarParaCrear(perfil, cuenta));

        perfil.setMediosPago(List.of());
        assertThrows(ReglaDeNegocioException.class, () -> validator.validarParaCrear(perfil, cuenta));
    }

    @Test
    @DisplayName("validarParaCrear: Nequi sin número lanza ReglaDeNegocioException")
    void validarParaCrear_nequiSinNumero_lanzaReglaDeNegocio() {
        perfil.setMediosPago(List.of(MedioPago.NEQUI));
        perfil.setNumeroNequi(null);
        assertThrows(ReglaDeNegocioException.class, () -> validator.validarParaCrear(perfil, cuenta));

        perfil.setNumeroNequi("   ");
        assertThrows(ReglaDeNegocioException.class, () -> validator.validarParaCrear(perfil, cuenta));
    }

    @Test
    @DisplayName("validarParaCrear: Daviplata sin número lanza ReglaDeNegocioException")
    void validarParaCrear_daviplataSinNumero_lanzaReglaDeNegocio() {
        perfil.setMediosPago(List.of(MedioPago.DAVIPLATA));
        perfil.setNumeroDaviplata(null);
        assertThrows(ReglaDeNegocioException.class, () -> validator.validarParaCrear(perfil, cuenta));

        perfil.setNumeroDaviplata("   ");
        assertThrows(ReglaDeNegocioException.class, () -> validator.validarParaCrear(perfil, cuenta));
    }

    @Test
    @DisplayName("validarParaCrear: Número Nequi duplicado por otra cocinera lanza ConflictoException")
    void validarParaCrear_nequiDuplicado_lanzaConflicto() {
        PerfilCocinera otraCocinera = PerfilCocinera.builder()
                .id(UUID.randomUUID())
                .numeroNequi("3001234567")
                .build();
        when(perfilCocineraRepository.findAll()).thenReturn(List.of(otraCocinera));

        assertThrows(ConflictoException.class, () -> validator.validarParaCrear(perfil, cuenta));
    }

    @Test
    @DisplayName("validarParaCrear: Número Daviplata duplicado por otra cocinera lanza ConflictoException")
    void validarParaCrear_daviplataDuplicado_lanzaConflicto() {
        PerfilCocinera otraCocinera = PerfilCocinera.builder()
                .id(UUID.randomUUID())
                .numeroDaviplata("3007654321")
                .build();
        when(perfilCocineraRepository.findAll()).thenReturn(List.of(otraCocinera));

        assertThrows(ConflictoException.class, () -> validator.validarParaCrear(perfil, cuenta));
    }

    @Test
    @DisplayName("validarParaCrear: Happy path exitoso")
    void validarParaCrear_datosValidos_pasaExitoso() {
        when(perfilCocineraRepository.findByCuentaId(cuenta.getId())).thenReturn(Optional.empty());
        when(perfilCocineraRepository.findAll()).thenReturn(List.of());

        assertDoesNotThrow(() -> validator.validarParaCrear(perfil, cuenta));
    }

    @Test
    @DisplayName("validarParaActualizar: Mismo perfil con sus propios números no entra en conflicto")
    void validarParaActualizar_mismoPerfil_pasaExitoso() {
        UUID perfilId = UUID.randomUUID();
        PerfilCocinera propio = PerfilCocinera.builder()
                .id(perfilId)
                .numeroNequi("3001234567")
                .numeroDaviplata("3007654321")
                .build();

        when(perfilCocineraRepository.findAll()).thenReturn(List.of(propio));

        assertDoesNotThrow(() -> validator.validarParaActualizar(perfilId, perfil));
    }

    @Test
    @DisplayName("validarParaActualizar: Otro perfil con el mismo Daviplata lanza ConflictoException")
    void validarParaActualizar_otroPerfilConMismoDaviplata_lanzaConflicto() {
        UUID perfilId = UUID.randomUUID();
        PerfilCocinera ajeno = PerfilCocinera.builder()
                .id(UUID.randomUUID())
                .numeroDaviplata("3007654321")
                .build();

        when(perfilCocineraRepository.findAll()).thenReturn(List.of(ajeno));

        assertThrows(ConflictoException.class, () -> validator.validarParaActualizar(perfilId, perfil));
    }
}