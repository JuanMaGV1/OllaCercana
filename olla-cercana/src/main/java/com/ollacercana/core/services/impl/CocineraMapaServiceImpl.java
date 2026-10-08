package com.ollacercana.core.services.impl;

import com.ollacercana.controller.dtos.response.CocineraMapaResponseDTO;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.services.CocineraMapaService;
import com.ollacercana.core.util.GeoUtils;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CocineraMapaServiceImpl implements CocineraMapaService {

    private final PlatoRepository platoRepository;
    private final PerfilCocineraRepository perfilCocineraRepository;

    private record OfertaCandidata(PlatoEntity plato, double distanciaReal) {}

    @Override
    @Transactional(readOnly = true)
    public List<CocineraMapaResponseDTO> buscarCocinerasEnMapa(Double latitud, Double longitud, Double radio) {
        if (latitud == null || longitud == null) {
            throw new IllegalArgumentException("La latitud y longitud son obligatorias");
        }
        double radioEfectivo = (radio != null && radio > 0) ? radio : 2000.0;

        List<PlatoEntity> ofertasActivas = platoRepository.findOfertasActivasConUbicacion(
                EstadoPlato.ACTIVO, LocalDateTime.now()
        );

        if (ofertasActivas.isEmpty()) {
            return Collections.emptyList();
        }

        // Agrupar por cocinera seleccionando la oferta activa más cercana dentro del radio
        Map<UUID, OfertaCandidata> mejoresOfertasPorCocinera = new LinkedHashMap<>();

        for (PlatoEntity plato : ofertasActivas) {
            UUID cocineraId = plato.getCocineraId();
            if (cocineraId == null) continue;

            double distanciaReal = GeoUtils.calcularDistanciaEnMetros(
                    latitud, longitud, plato.getLatitud(), plato.getLongitud()
            );

            if (distanciaReal <= radioEfectivo) {
                if (!mejoresOfertasPorCocinera.containsKey(cocineraId)
                        || distanciaReal < mejoresOfertasPorCocinera.get(cocineraId).distanciaReal()) {
                    mejoresOfertasPorCocinera.put(cocineraId, new OfertaCandidata(plato, distanciaReal));
                }
            }
        }

        if (mejoresOfertasPorCocinera.isEmpty()) {
            return Collections.emptyList();
        }

        List<CocineraMapaResponseDTO> respuesta = new ArrayList<>();

        for (Map.Entry<UUID, OfertaCandidata> entrada : mejoresOfertasPorCocinera.entrySet()) {
            UUID cocineraId = entrada.getKey();
            PlatoEntity plato = entrada.getValue().plato();
            double distanciaReal = entrada.getValue().distanciaReal();

            // Omitir si el perfil de la cocinera está pausado
            Optional<PerfilCocineraEntity> perfilOpt = perfilCocineraRepository.findById(cocineraId);
            if (perfilOpt.isPresent() && perfilOpt.get().isPausada()) {
                continue;
            }

            // Distancia calculada ANTES de ofuscar y redondeada a múltiplos de 100m
            int distanciaRedondeada = GeoUtils.redondearDistanciaMultiplo100(distanciaReal);

            // Ofuscar coordenadas con margen de seguridad
            double[] coordsOfuscadas = GeoUtils.ofuscarCoordenadas(plato.getLatitud(), plato.getLongitud());

            String nombreCocinera = perfilOpt
                    .map(perfil -> {
                        if (perfil.getCuenta() != null && perfil.getCuenta().getIdentidad() != null
                                && perfil.getCuenta().getIdentidad().getNombre() != null) {
                            return perfil.getCuenta().getIdentidad().getNombre();
                        }
                        return perfil.getConjuntoResidencial() != null
                                ? perfil.getConjuntoResidencial()
                                : "Cocinera";
                    })
                    .orElse("Cocinera");

            respuesta.add(CocineraMapaResponseDTO.builder()
                    .cocineraId(cocineraId)
                    .nombreCocinera(nombreCocinera)
                    .platoNombre(plato.getNombre())
                    .fotoPlato(plato.getFotoUrl())
                    .precio(plato.getPrecioPorcion())
                    .latitud(coordsOfuscadas[0])
                    .longitud(coordsOfuscadas[1])
                    .distanciaMetros(distanciaRedondeada)
                    .build());
        }

        respuesta.sort(Comparator.comparingInt(CocineraMapaResponseDTO::getDistanciaMetros));
        return respuesta;
    }
}