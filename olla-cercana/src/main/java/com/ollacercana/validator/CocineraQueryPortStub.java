package com.ollacercana.validator;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component // Se crea temporal mientras no este cuenta ni perfil de cocinera
public class CocineraQueryPortStub implements CocineraQueryPort {
    @Override
    public boolean estaVerificada(UUID cocineraId) {
        return true;
    }

    @Override
    public boolean estaPausada(UUID cocineraId) {
        return false;
    }
}