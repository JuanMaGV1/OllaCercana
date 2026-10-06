package com.ollacercana.repository;

import com.ollacercana.model.domain.EstadoCuenta;
import com.ollacercana.model.domain.Rol;
import com.ollacercana.persistence.entity.CredencialesEmbeddable;
import com.ollacercana.persistence.entity.CuentaEntity;
import com.ollacercana.persistence.entity.IdentidadEmbeddable;
import com.ollacercana.persistence.entity.PerfilCocineraEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PerfilCocineraRepositoryTest {

    @Autowired private PerfilCocineraRepository perfilRepository;
    @Autowired private CuentaRepository cuentaRepository;

    @Test
    @DisplayName("findByCuentaId debe retornar el perfil asociado a la cuenta")
    void findByCuentaId_DebeRetornarPerfil() {
        CuentaEntity cuenta = CuentaEntity.builder()
                .identidad(IdentidadEmbeddable.builder()
                        .nombre("Maria Perez")
                        .correo("maria@gmail.com")
                        .celular("3001112233")
                        .build())
                .credenciales(CredencialesEmbeddable.builder()
                        .contrasenaHash("Hash123")
                        .build())
                .roles(Set.of(Rol.COCINERA))
                .estado(EstadoCuenta.ACTIVO)
                .build();
        cuenta = cuentaRepository.save(cuenta);

        PerfilCocineraEntity perfil = PerfilCocineraEntity.builder()
                .conjuntoResidencial("Torres del Parque")
                .presentacion("Especialista en comida típica")
                .cuenta(cuenta)
                .esDestacada(false)
                .promedioCalificacion(0.0)
                .resenasPositivas(0)
                .build();
        perfilRepository.save(perfil);

        Optional<PerfilCocineraEntity> resultado = perfilRepository.findByCuentaId(cuenta.getId());

        assertTrue(resultado.isPresent());
        assertEquals("Torres del Parque", resultado.get().getConjuntoResidencial());
        assertEquals(cuenta.getId(), resultado.get().getCuenta().getId());
    }

    @Test
    @DisplayName("findByEsDestacadaTrue debe listar solo los perfiles marcados como destacados")
    void findByEsDestacadaTrue_DebeRetornarPerfilesDestacados() {
        PerfilCocineraEntity destacada = PerfilCocineraEntity.builder()
                .conjuntoResidencial("Residencial Los Álamos")
                .esDestacada(true)
                .promedioCalificacion(0.0)
                .resenasPositivas(0)
                .build();

        PerfilCocineraEntity noDestacada = PerfilCocineraEntity.builder()
                .conjuntoResidencial("Residencial Los Pinos")
                .esDestacada(false)
                .promedioCalificacion(0.0)
                .resenasPositivas(0)
                .build();

        perfilRepository.save(destacada);
        perfilRepository.save(noDestacada);

        List<PerfilCocineraEntity> destacados = perfilRepository.findByEsDestacadaTrue();

        assertFalse(destacados.isEmpty());
        assertTrue(destacados.stream().allMatch(PerfilCocineraEntity::getEsDestacada));
    }
}