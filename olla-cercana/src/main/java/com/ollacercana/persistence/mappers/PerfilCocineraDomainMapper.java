package com.ollacercana.persistence.mappers;

import com.ollacercana.core.models.Cuenta;
import com.ollacercana.core.models.Credenciales;
import com.ollacercana.core.models.Identidad;
import com.ollacercana.core.models.PerfilCocinera;
import com.ollacercana.persistence.entities.CuentaEntity;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import org.springframework.stereotype.Component;

@Component
public class PerfilCocineraDomainMapper {

    // ----------- Entity → Dominio -----------

    public PerfilCocinera toDomain(PerfilCocineraEntity e) {
        if (e == null) return null;

        PerfilCocinera perfil = new PerfilCocinera();
        perfil.setId(e.getId());
        perfil.setPresentacion(e.getPresentacion());
        perfil.setEspecialidades(e.getEspecialidades());
        perfil.setMediosPago(e.getMediosPago());
        perfil.setNumeroNequi(e.getNumeroNequi());
        perfil.setNumeroDaviplata(e.getNumeroDaviplata());
        perfil.setConjuntoResidencial(e.getConjuntoResidencial());
        perfil.setPromedioCalificacion(e.getPromedioCalificacion());
        perfil.setResenasPositivas(e.getResenasPositivas());
        perfil.setEsDestacada(e.getEsDestacada());
        perfil.setVerificada(e.isVerificada());
        perfil.setPausada(e.isPausada());
        perfil.setFechaReactivacion(e.getFechaReactivacion());
        perfil.setCuenta(toDomain(e.getCuenta()));
        return perfil;
    }

    // ----------- Dominio → Entity -----------

    public PerfilCocineraEntity toEntity(PerfilCocinera p) {
        if (p == null) return null;

        return PerfilCocineraEntity.builder()
                .id(p.getId())
                .presentacion(p.getPresentacion())
                .especialidades(p.getEspecialidades())
                .mediosPago(p.getMediosPago())
                .numeroNequi(p.getNumeroNequi())
                .numeroDaviplata(p.getNumeroDaviplata())
                .conjuntoResidencial(p.getConjuntoResidencial())
                .promedioCalificacion(p.getPromedioCalificacion())
                .resenasPositivas(p.getResenasPositivas())
                .esDestacada(p.getEsDestacada())
                .verificada(p.isVerificada())
                .pausada(p.isPausada())
                .fechaReactivacion(p.getFechaReactivacion())
                .cuenta(toEntity(p.getCuenta()))
                .build();
    }

    // ----------- Helpers privados -----------

    private Cuenta toDomain(CuentaEntity e) {
        if (e == null) return null;
        Cuenta c = new Cuenta();
        c.setId(e.getId());
        c.setEstado(e.getEstado());
        c.setRoles(e.getRoles());
        c.setFechaRegistro(e.getFechaRegistro());
        if (e.getIdentidad() != null) {
            c.setIdentidad(new Identidad(
                    e.getIdentidad().getNombre(),
                    e.getIdentidad().getCorreo(),
                    e.getIdentidad().getCelular(),
                    e.getIdentidad().getFotoUrl()));
        }
        if (e.getCredenciales() != null) {
            c.setCredenciales(new Credenciales(
                    e.getCredenciales().getContrasenaHash(),
                    e.getCredenciales().getTokenFCM(),
                    e.getCredenciales().getCelularVerificado()));
        }
        return c;
    }

    private CuentaEntity toEntity(Cuenta c) {
        if (c == null) return null;
        // NOTA: aquí solo se referencia por id para no duplicar la cuenta al guardar un perfil.
        return CuentaEntity.builder().id(c.getId()).build();
    }
}