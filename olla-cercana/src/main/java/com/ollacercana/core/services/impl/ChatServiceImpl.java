package com.ollacercana.core.services.impl;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.dtos.request.EnviarMensajeRequestDTO;
import com.ollacercana.controller.dtos.response.MensajeResponseDTO;
import com.ollacercana.controller.mappers.ChatMapper;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.services.ChatService;
import com.ollacercana.core.validators.ChatValidator;
import com.ollacercana.persistence.document.MensajeChatDocument;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.repository.mongo.MensajeChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final MensajeChatRepository mensajeChatRepository;
    private final ChatValidator chatValidator;
    private final ChatMapper chatMapper;
    private final UsuarioActual usuarioActual;

    @Override
    public MensajeResponseDTO enviarMensaje(UUID reservaId, EnviarMensajeRequestDTO request) {
        Long cuentaId = usuarioActual.getCuentaId();
        UUID cocineraId = usuarioActual.getCocineraIdOpt().orElse(null);

        ReservaEntity reserva = chatValidator.validarParaEnviarMensaje(reservaId, cuentaId, cocineraId, request.texto());

        Rol autorRol = (cocineraId != null && cocineraId.equals(reserva.getCocineraId())) ? Rol.COCINERA : Rol.COMPRADOR;
        String autorId = (autorRol == Rol.COCINERA) ? cocineraId.toString() : cuentaId.toString();

        MensajeChatDocument doc = MensajeChatDocument.builder()
                .reservaId(reservaId)
                .autorRol(autorRol)
                .autorId(autorId)
                .texto(request.texto().trim())
                .fecha(LocalDateTime.now())
                .leido(false)
                .build();

        MensajeChatDocument guardado = mensajeChatRepository.save(doc);
        return chatMapper.toDTO(guardado);
    }

    @Override
    public List<MensajeResponseDTO> listarMensajes(UUID reservaId, LocalDateTime desde) {
        Long cuentaId = usuarioActual.getCuentaId();
        UUID cocineraId = usuarioActual.getCocineraIdOpt().orElse(null);

        chatValidator.validarAccesoChat(reservaId, cuentaId, cocineraId);

        List<MensajeChatDocument> mensajes = (desde != null)
                ? mensajeChatRepository.findByReservaIdAndFechaAfterOrderByFechaAsc(reservaId, desde)
                : mensajeChatRepository.findByReservaIdOrderByFechaAsc(reservaId);

        return chatMapper.toDTOList(mensajes);
    }

    @Override
    public void marcarLeidos(UUID reservaId) {
        Long cuentaId = usuarioActual.getCuentaId();
        UUID cocineraId = usuarioActual.getCocineraIdOpt().orElse(null);

        ReservaEntity reserva = chatValidator.validarAccesoChat(reservaId, cuentaId, cocineraId);
        Rol miRol = (cocineraId != null && cocineraId.equals(reserva.getCocineraId())) ? Rol.COCINERA : Rol.COMPRADOR;

        List<MensajeChatDocument> noLeidos = mensajeChatRepository.findByReservaIdAndLeidoFalse(reservaId);
        for (MensajeChatDocument mensaje : noLeidos) {
            if (mensaje.getAutorRol() != miRol) {
                mensaje.setLeido(true);
                mensajeChatRepository.save(mensaje);
            }
        }
    }

    @Override
    public long contarNoLeidos(UUID reservaId) {
        Long cuentaId = usuarioActual.getCuentaId();
        UUID cocineraId = usuarioActual.getCocineraIdOpt().orElse(null);

        ReservaEntity reserva = chatValidator.validarAccesoChat(reservaId, cuentaId, cocineraId);
        Rol miRol = (cocineraId != null && cocineraId.equals(reserva.getCocineraId())) ? Rol.COCINERA : Rol.COMPRADOR;

        return mensajeChatRepository.countByReservaIdAndLeidoFalseAndAutorRolNot(reservaId, miRol);
    }
}
