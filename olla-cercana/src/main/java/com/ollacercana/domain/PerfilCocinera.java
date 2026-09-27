package com.ollacercana.domain;

import java.util.UUID;

/**
 * Versión inicial mínima — solo con lo que PlatoValidator necesita para HU-04
 *
 */
public record PerfilCocinera(UUID id, boolean verificada, boolean pausada) {

}