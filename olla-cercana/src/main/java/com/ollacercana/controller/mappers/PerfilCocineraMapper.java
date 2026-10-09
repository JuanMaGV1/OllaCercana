package com.ollacercana.controller.mappers;

import com.ollacercana.controller.dtos.request.PerfilCocineraRequestDTO;
import com.ollacercana.controller.dtos.response.PerfilCocineraResponseDTO;
import com.ollacercana.core.models.PerfilCocinera;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PerfilCocineraMapper {

    public PerfilCocinera toDomain(PerfilCocineraRequestDTO request) {
        if (request == null) return null;

        PerfilCocinera perfil = new PerfilCocinera();
        perfil.setPresentacion(request.getPresentacion());
        perfil.setConjuntoResidencial(request.getConjuntoResidencial());
        perfil.setEspecialidades(request.getEspecialidades());
        perfil.setMediosPago(request.getMediosPago());
        perfil.setNumeroNequi(request.getNumeroNequi());
        perfil.setNumeroDaviplata(request.getNumeroDaviplata());
        return perfil;
    }

    public PerfilCocineraResponseDTO toResponseDTO(PerfilCocinera perfil) {
        if (perfil == null) return null;

        return PerfilCocineraResponseDTO.builder()
                .id(perfil.getId())
                .cuentaId(perfil.getCuenta() != null ? perfil.getCuenta().getId() : null)
                .nombreCocinera(perfil.getCuenta() != null && perfil.getCuenta().getIdentidad() != null
                        ? perfil.getCuenta().getIdentidad().getNombre()
                        : null)
                .presentacion(perfil.getPresentacion())
                .conjuntoResidencial(perfil.getConjuntoResidencial())
                .especialidades(perfil.getEspecialidades())
                .mediosPago(perfil.getMediosPago())
                .numeroNequi(perfil.getNumeroNequi())
                .numeroDaviplata(perfil.getNumeroDaviplata())
                .promedioCalificacion(perfil.getPromedioCalificacion())
                .resenasPositivas(perfil.getResenasPositivas())
                .esDestacada(perfil.getEsDestacada())
                .verificada(perfil.isVerificada())
                .pausada(perfil.isPausada())
                .build();
    }

    public List<PerfilCocineraResponseDTO> toResponseList(List<PerfilCocinera> perfiles) {
        if (perfiles == null) return List.of();
        return perfiles.stream().map(this::toResponseDTO).toList();
    }
}