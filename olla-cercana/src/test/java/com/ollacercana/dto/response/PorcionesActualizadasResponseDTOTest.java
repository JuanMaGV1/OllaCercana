package com.ollacercana.dto.response;

import com.ollacercana.domain.EstadoPorciones;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PorcionesActualizadasResponseDTOTest {

    private Plato plato(int totales, int comprometidas, EstadoPlato estado) {
        return Plato.builder()
                .id(UUID.randomUUID())
                .porcionesTotales(totales)
                .porcionesComprometidas(comprometidas)
                .estado(estado)
                .build();
    }

    @Test
    @DisplayName("Un plato con porciones libres genera un evento DISPONIBLE")
    void platoConPorciones_EventoDisponible() {
        Plato plato = plato(3, 1, EstadoPlato.ACTIVO);

        PorcionesActualizadasResponseDTO evento = PorcionesActualizadasResponseDTO.de(plato);

        assertEquals(plato.getId(), evento.platoId());
        assertEquals(2, evento.porcionesDisponibles());
        assertEquals(EstadoPorciones.DISPONIBLE, evento.estado());
    }

    @Test
    @DisplayName("Un plato sin porciones libres genera un evento AGOTADO")
    void platoSinPorciones_EventoAgotado() {
        Plato plato = plato(2, 2, EstadoPlato.AGOTADO);

        PorcionesActualizadasResponseDTO evento = PorcionesActualizadasResponseDTO.de(plato);

        assertEquals(0, evento.porcionesDisponibles());
        assertEquals(EstadoPorciones.AGOTADO, evento.estado());
    }

    @Test
    @DisplayName("El evento se serializa correctamente a JSON")
    void evento_SeSerializaAJson() throws Exception {
        UUID id = UUID.randomUUID();
        var evento = new PorcionesActualizadasResponseDTO(id, 1, EstadoPorciones.DISPONIBLE);

        String json = new ObjectMapper().writeValueAsString(evento);
        JsonNode nodo = new ObjectMapper().readTree(json);

        assertEquals(id.toString(), nodo.get("platoId").asText());
        assertEquals(1, nodo.get("porcionesDisponibles").asInt());
        assertEquals("DISPONIBLE", nodo.get("estado").asText());
    }
}
