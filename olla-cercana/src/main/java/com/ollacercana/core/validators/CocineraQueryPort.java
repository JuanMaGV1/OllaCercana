package com.ollacercana.core.validators;

import java.util.UUID;
 
public interface CocineraQueryPort {

    boolean estaVerificada(UUID cocineraId);

    boolean estaPausada(UUID cocineraId);
}