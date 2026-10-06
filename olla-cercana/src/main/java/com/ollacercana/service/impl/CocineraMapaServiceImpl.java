package com.ollacercana.service.impl;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.PerfilCocinera;
import com.ollacercana.domain.Plato;
import com.ollacercana.dto.request.MapaCocinerasRequestDTO;
import com.ollacercana.dto.response.CocineraMapaResponseDTO;
import com.ollacercana.repository.PerfilCocineraRepository;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.service.CocineraMapaService;
import com.ollacercana.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CocineraMapaServiceImpl implements CocineraMapaService {

    private final PlatoRepository platoRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;

    @org.springframework.beans.factory.annotation.Value("${ollacercana.mapa.ofuscacion-salt:olla-cercana-default-salt-dev}")
    private String ofuscacionSalt = "olla-cercana-default-salt-dev";

    public void setOfuscacionSalt(String ofuscacionSalt) {
        this.ofuscacionSalt = ofuscacionSalt;
    }

    @Override
    public List<CocineraMapaResponseDTO> buscarCocinerasEnMapa(MapaCocinerasRequestDTO request) {
        log.info("Buscando cocineras en el mapa: lat={}, lon={}, radio={}m",
                request.getLatitud(), request.getLongitud(), request.getRadio());

        // OC-232: Reutiliza findActivosVigentes para asegurar platos activos, no expirados y con porciones disponibles > 0
        List<Plato> platosActivos = platoRepository.findActivosVigentes(EstadoPlato.ACTIVO, LocalDateTime.now());

        // Mapa auxiliar para seleccionar la mejor oferta por cocinera dentro del radio
        Map<UUID, OfertaCocineraCandidata> mejoresOfertasPorCocinera = new LinkedHashMap<>();

        for (Plato plato : platosActivos) {
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

            // OC-233: Distancia estimada en metros calculada ANTES de ofuscar y redondeada a múltiplos de 100
            int distanciaEstimada = GeoUtils.redondearDistanciaMultiplo100(distanciaReal);

            // OC-235: Coordenadas ofuscadas con margen acotado derivado determinísticamente del ID de la cocinera y la sal secreta
            GeoUtils.CoordenadasOfuscadas coords = GeoUtils.ofuscarCoordenadas(cocineraId, ofuscacionSalt, plato.getLatitud(), plato.getLongitud());

            String nombreCocinera = perfilCocineraRepository.findById(cocineraId)
                    .map(perfil -> {
                        if (perfil.getCuenta() != null && perfil.getCuenta().getIdentidad() != null && perfil.getCuenta().getIdentidad().getNombre() != null) {
                            return perfil.getCuenta().getIdentidad().getNombre();
                        }
                        return perfil.getConjuntoResidencial();
                    })
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

    private record OfertaCocineraCandidata(Plato plato, double distanciaReal) {}
}
