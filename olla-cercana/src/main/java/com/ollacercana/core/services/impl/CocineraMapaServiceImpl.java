package com.ollacercana.core.services.impl;

import com.ollacercana.controller.dtos.request.MapaCocinerasRequestDTO;
import com.ollacercana.controller.dtos.response.CocineraMapaResponseDTO;
import com.ollacercana.core.models.Plato;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.services.CocineraMapaService;
import com.ollacercana.core.util.GeoUtils;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CocineraMapaServiceImpl implements CocineraMapaService {

    private final PlatoRepository platoRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;
    private final PlatoEntityMapper platoEntityMapper;

    @Value("${ollacercana.mapa.ofuscacion-salt:olla-cercana-default-salt-dev}")
    private String ofuscacionSalt = "olla-cercana-default-salt-dev";

    public void setOfuscacionSalt(String ofuscacionSalt) {
        this.ofuscacionSalt = ofuscacionSalt;
    }

    @Override
    public List<CocineraMapaResponseDTO> buscarCocinerasEnMapa(MapaCocinerasRequestDTO request) {
        log.info("Buscando cocineras en el mapa: lat={}, lon={}, radio={}m",
                request.getLatitud(), request.getLongitud(), request.getRadio());

        // Repo devuelve entities → convertir a dominio
        List<PlatoEntity> platosActivosEntity = platoRepository.findActivosVigentes(
                EstadoPlato.ACTIVO, LocalDateTime.now());

        List<Plato> platosActivos = platosActivosEntity.stream()
                .map(platoEntityMapper::toDomain)
                .toList();

        Map<UUID, OfertaCocineraCandidata> mejoresOfertasPorCocinera = new LinkedHashMap<>();

        for (Plato plato : platosActivos) {
            if (plato.getLatitud() == null || plato.getLongitud() == null || plato.getCocineraId() == null) {
                continue;
            }

            double distanciaReal = GeoUtils.calcularDistanciaEnMetros(
                    request.getLatitud(), request.getLongitud(),
                    plato.getLatitud(), plato.getLongitud()
            );

            if (distanciaReal <= request.getRadio()) {
                UUID cocineraId = plato.getCocineraId();
                OfertaCocineraCandidata existente = mejoresOfertasPorCocinera.get(cocineraId);
                if (existente == null || distanciaReal < existente.distanciaReal) {
                    mejoresOfertasPorCocinera.put(cocineraId, new OfertaCocineraCandidata(plato, distanciaReal));
                }
            }
        }

        List<CocineraMapaResponseDTO> resultado = new ArrayList<>();

        for (Map.Entry<UUID, OfertaCocineraCandidata> entry : mejoresOfertasPorCocinera.entrySet()) {
            UUID cocineraId = entry.getKey();
            Plato plato = entry.getValue().plato;
            double distanciaReal = entry.getValue().distanciaReal;

            int distanciaEstimada = GeoUtils.redondearDistanciaMultiplo100(distanciaReal);

            GeoUtils.CoordenadasOfuscadas coords = GeoUtils.ofuscarCoordenadas(
                    cocineraId, ofuscacionSalt, plato.getLatitud(), plato.getLongitud());

            String nombreCocinera = perfilCocineraRepository.findById(cocineraId)
                    .map(this::resolverNombreCocinera)
                    .orElse("Cocinera Local");

            resultado.add(CocineraMapaResponseDTO.builder()
                    .cocineraId(cocineraId)
                    .nombreCocinera(nombreCocinera)
                    .latitudOfuscada(coords.latitud())
                    .longitudOfuscada(coords.longitud())
                    .fotoPlato(plato.getFotoUrl())
                    .nombrePlato(plato.getNombre())
                    .precio(plato.getPrecioPorcion())
                    .distanciaMetros(distanciaEstimada)
                    .build());
        }

        resultado.sort(Comparator.comparingInt(CocineraMapaResponseDTO::getDistanciaMetros));
        return resultado;
    }

    /** Usa la entity directamente, sin mapear a dominio. */
    private String resolverNombreCocinera(PerfilCocineraEntity perfil) {
        if (perfil.getCuenta() != null
                && perfil.getCuenta().getIdentidad() != null
                && perfil.getCuenta().getIdentidad().getNombre() != null) {
            return perfil.getCuenta().getIdentidad().getNombre();
        }
        return perfil.getConjuntoResidencial();
    }

    private record OfertaCocineraCandidata(Plato plato, double distanciaReal) {}
}