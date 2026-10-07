package com.ollacercana.service;

import com.ollacercana.domain.MedallaUsuario;
import com.ollacercana.dto.response.MedallaUsuarioResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedallaService {

    /**
     * OC-279
     *
     * @return la medalla otorgada, o vacío si no corresponde otorgarla
     */
    Optional<MedallaUsuario> evaluarVecinoFiel(Long compradorId, UUID cocineraId, LocalDateTime fechaCompletada);

    /**
     * OC-299
     * @return nombres de los conjuntos premiados
     */
    List<String> calcularBalanceSemanal(LocalDateTime ahora);

    /** OC-279: medallas vigentes del usuario. */
    List<MedallaUsuarioResponseDTO> listarVigentes(Long usuarioId, LocalDateTime ahora);
}
