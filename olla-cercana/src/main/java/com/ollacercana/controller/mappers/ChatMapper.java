package com.ollacercana.controller.mappers;

import com.ollacercana.controller.dtos.response.MensajeResponseDTO;
import com.ollacercana.persistence.document.MensajeChatDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMapper {

    public MensajeResponseDTO toDTO(MensajeChatDocument doc) {
        if (doc == null) return null;
        return MensajeResponseDTO.builder()
                .id(doc.getId())
                .autor(doc.getAutorRol() != null ? doc.getAutorRol().name() : doc.getAutorId())
                .autorRol(doc.getAutorRol())
                .autorId(doc.getAutorId())
                .texto(doc.getTexto())
                .fecha(doc.getFecha())
                .leido(doc.isLeido())
                .build();
    }

    public List<MensajeResponseDTO> toDTOList(List<MensajeChatDocument> docs) {
        if (docs == null) return List.of();
        return docs.stream().map(this::toDTO).toList();
    }
}