package com.ollacercana.core.services.impl;

import com.ollacercana.controller.dtos.request.MapaCocinerasRequestDTO;
import com.ollacercana.controller.dtos.response.CocineraMapaResponseDTO;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.services.CocineraMapaService;
import com.ollacercana.core.util.GeoUtils;
import com.ollacercana.persistence.entities.CuentaEntity;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CocineraMapaServiceImpl implements CocineraMapaService {

    private final PlatoRepository platoRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;

    @Value("${ollacercana.mapa.ofuscacion-salt:" + GeoUtils.DEFAULT_SALT + "}")
    private String ofuscacionSalt = GeoUtils.DEFAULT_SALT;

    public void setOfuscacionSalt(String ofuscacionSalt) {
        this.ofuscacionSalt = ofuscacionSalt;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CocineraMapaResponseDTO> buscarCocinerasEnMapa(MapaCocinerasRequestDTO request) {
        log.info("Buscando cocineras en el mapa: lat={}, lon={}, radio={}m",
                request.getLatitud(), request.getLongitud(), request.getRadio());

        // OC-232: Reutiliza findActivosVigentes para asegurar platos activos, no expirados y con porciones disponibles > 0
        List<PlatoEntity> platosActivos = platoRepository.findActivosVigentes(EstadoPlato.ACTIVO, LocalDateTime.now());

        // Mapa auxiliar para seleccionar la mejor oferta (la más cercana) por cocinera dentro del radio
        Map<UUID, OfertaCocineraCandidata> mejoresOfertasPorCocinera = new LinkedHashMap<>();

        for (PlatoEntity plato : platosActivos) {
            if (plato.getLatitud() == null || plato.getLongitud() == null || plato.getCocineraId() == null) {
                continue;
            }

            // OC-232: Reutiliza GeoUtils.calcularDistanciaEnMetros (Haversine)
            double distanciaReal = GeoUtils.calcularDistanciaEnMetros(
                    request.getLatitud(), request.getLongitud(),
                    plato.getLatitud(), plato.getLongitud()
            );

            if (distanciaReal <= request.getRadio()) {
                UUID cocineraId = plato.getCocineraId();
                OfertaCocineraCandidata existente = mejoresOfertasPorCocinera.get(cocineraId);
                if (existente == null || distanciaReal < existente.distanciaReal()) {
                    mejoresOfertasPorCocinera.put(cocineraId, new OfertaCocineraCandidata(plato, distanciaReal));
                }
            }
        }

        List<CocineraMapaResponseDTO> resultado = new ArrayList<>();

        for (Map.Entry<UUID, OfertaCocineraCandidata> entry : mejoresOfertasPorCocinera.entrySet()) {
            UUID cocineraId = entry.getKey();
            PlatoEntity plato = entry.getValue().plato();
            double distanciaReal = entry.getValue().distanciaReal();

            // OC-233: Distancia estimada en metros calculada ANTES de ofuscar y redondeada a múltiplos de 100
            int distanciaEstimada = GeoUtils.redondearDistanciaMultiplo100(distanciaReal);

            // OC-235: Coordenadas ofuscadas con margen acotado derivado determinísticamente del ID de la cocinera y la sal secreta
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

    private String resolverNombreCocinera(PerfilCocineraEntity perfil) {
        CuentaEntity cuenta = perfil.getCuenta();
        if (cuenta != null && cuenta.getIdentidad() != null && cuenta.getIdentidad().getNombre() != null) {
            return cuenta.getIdentidad().getNombre();
        }
        return perfil.getConjuntoResidencial() != null ? perfil.getConjuntoResidencial() : "Cocinera Local";
    }

    private record OfertaCocineraCandidata(PlatoEntity plato, double distanciaReal) {}
}
