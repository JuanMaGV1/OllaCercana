package com.ollacercana.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.ollacercana.model.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PerfilCocineraRepositoryTest {

    @Autowired
    private PerfilCocineraRepository perfilRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Test
    @DisplayName("findByCuentaId debe retornar el perfil asociado a la cuenta")
    void findByCuentaId_DebeRetornarPerfil() {
        // Arrange
        Cuenta cuenta = Cuenta.builder()
                .identidad(Identidad.builder().nombre("Maria Perez").correo("maria@gmail.com").celular("3001112233").build())
                .credenciales(Credenciales.builder().contrasenaHash("Hash123").build())
                .roles(Set.of(Rol.COCINERA))
                .estado(EstadoCuenta.ACTIVO)
                .build();
        cuenta = cuentaRepository.save(cuenta);

        PerfilCocinera perfil = PerfilCocinera.builder()
                .conjuntoResidencial("Torres del Parque")
                .presentacion("Especialista en comida típica")
                .cuenta(cuenta)
                .esDestacada(false)
                .build();
        perfilRepository.save(perfil);

        // Act
        Optional<PerfilCocinera> resultado = perfilRepository.findByCuentaId(cuenta.getId());

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Torres del Parque", resultado.get().getConjuntoResidencial());
        assertEquals(cuenta.getId(), resultado.get().getCuenta().getId());
    }

    @Test
    @DisplayName("findByEsDestacadaTrue debe listar solo los perfiles marcados como destacados")
    void findByEsDestacadaTrue_DebeRetornarPerfilesDestacados() {
        // Arrange
        PerfilCocinera destacada = PerfilCocinera.builder()
                .conjuntoResidencial("Residencial Los Álamos")
                .esDestacada(true)
                .build();

        PerfilCocinera noDestacada = PerfilCocinera.builder()
                .conjuntoResidencial("Residencial Los Pinos")
                .esDestacada(false)
                .build();

        perfilRepository.save(destacada);
        perfilRepository.save(noDestacada);

        // Act
        List<PerfilCocinera> destacados = perfilRepository.findByEsDestacadaTrue();

        // Assert
        assertFalse(destacados.isEmpty());
        assertTrue(destacados.stream().allMatch(PerfilCocinera::getEsDestacada));
    }
}