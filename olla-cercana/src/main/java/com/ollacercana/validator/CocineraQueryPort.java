package com.ollacercana.validator;

import java.util.UUID;

/**
 * Puerto que desacopla PlatoValidator del módulo de Cuenta/PerfilCocinera
 * (que están construyendo Daniel y Kevin en paralelo, HU-01/02/03).
 *
 * Mientras ese módulo no exista en develop, se puede implementar un stub
 * temporal (siempre true/false según el caso de prueba) para no bloquearse.
 * Cuando el módulo real esté listo, solo se conecta una implementación real
 * de esta interfaz — PlatoValidator no cambia.
 */
public interface CocineraQueryPort {

    boolean estaVerificada(UUID cocineraId);

    boolean estaPausada(UUID cocineraId);
}