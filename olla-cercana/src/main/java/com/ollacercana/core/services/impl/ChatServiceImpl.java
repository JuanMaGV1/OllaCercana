package com.ollacercana.core.services.impl;

import com.ollacercana.config.security.UsuarioActual;
import com.ollacercana.controller.dtos.request.EnviarMensajeRequestDTO;
import com.ollacercana.controller.dtos.response.MensajeResponseDTO;
import com.ollacercana.controller.mappers.ChatMapper;
import com.ollacercana.core.models.EventoReserva;
import com.ollacercana.core.models.enums.EstadoReserva;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.models.enums.TipoEvento;
import com.ollacercana.core.patterns.observer.PublicadorEventosReserva;
import com.ollacercana.core.services.ChatService;
import com.ollacercana.core.validators.ChatValidator;
import com.ollacercana.persistence.document.MensajeChatDocument;
import com.ollacercana.persistence.entities.ReservaEntity;
import com.ollacercana.persistence.repository.ReservaRepository;
import com.ollacercana.persistence.repository.mongo.MensajeChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    public static final int DIAS_RETENCION_CHAT = 30;

    private final MensajeChatRepository mensajeChatRepository;
    private final ReservaRepository reservaRepository;
    private final ChatValidator chatValidator;
    private final ChatMapper chatMapper;
    private final UsuarioActual usuarioActual;

    private PublicadorEventosReserva publicadorEventosReserva;

    @Autowired(required = false)
    public void setPublicadorEventosReserva(PublicadorEventosReserva publicadorEventosReserva) {
        this.publicadorEventosReserva = publicadorEventosReserva;
    }

    @Override
    public MensajeResponseDTO enviar(UUID reservaId, EnviarMensajeRequestDTO request) {
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

        // OC-263: Publicar el evento NUEVO_MENSAJE_CHAT
        if (publicadorEventosReserva != null) {
            EventoReserva evento = EventoReserva.builder()
                    .id(UUID.randomUUID().toString())
                    .tipo(TipoEvento.NUEVO_MENSAJE_CHAT)
                    .reservaId(reserva.getId())
                    .platoId(reserva.getPlatoId())
                    .compradorId(reserva.getCompradorId())
                    .cocineraId(reserva.getCocineraId())
                    .timestamp(guardado.getFecha())
                    .payload(Map.of(
                            "mensajeId", guardado.getId(),
                            "autorId", autorId,
                            "autorRol", autorRol.name(),
                            "texto", guardado.getTexto()
                    ))
                    .build();
            publicadorEventosReserva.publicar(evento);
        }

        return chatMapper.toDTO(guardado);
    }

    @Override
    public List<MensajeResponseDTO> listar(UUID reservaId, LocalDateTime desde) {
        Long cuentaId = usuarioActual.getCuentaId();
        UUID cocineraId = usuarioActual.getCocineraIdOpt().orElse(null);

        // OC-264: Valida acceso sin exigir que el chat esté ACTIVO (permite SOLO_LECTURA)
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

    @Override
    @Transactional
    public long purgarMensajesAntiguos() {
        LocalDateTime limite = LocalDateTime.now().minusDays(DIAS_RETENCION_CHAT);
        return purgarMensajesDeReservasCerradas(limite);
    }

    @Override
    @Transactional
    public long purgarMensajesDeReservasCerradas(LocalDateTime limite) {
        List<ReservaEntity> cerradasAntiguas = reservaRepository.findByEstadoAndFechaCompletadaLessThanEqual(
                EstadoReserva.COMPLETADA, limite);

        if (cerradasAntiguas.isEmpty()) {
            return 0L;
        }

        long totalBorrados = 0L;
        for (ReservaEntity reserva : cerradasAntiguas) {
            totalBorrados += mensajeChatRepository.deleteByReservaId(reserva.getId());
        }
        log.info("Purga RN-18 completada: {} mensajes borrados de {} reservas cerradas", totalBorrados, cerradasAntiguas.size());
        return totalBorrados;
    }
}