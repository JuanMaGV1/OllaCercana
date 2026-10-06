package com.ollacercana.service;

import com.ollacercana.domain.*;
import com.ollacercana.dto.EjecutarDecisionDto;
import com.ollacercana.exception.ReglaDeNegocioException;
import com.ollacercana.exception.ResourceNotFoundException;
import com.ollacercana.repository.*;
import com.ollacercana.repository.mongo.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModeracionService {

    private final ReporteRepository reporteRepository;
    private final DecisionModeracionRepository decisionRepository;
    private final PlatoRepository platoRepository;
    private final CuentaRepository cuentaRepository;
    private final PerfilCocineraRepository perfilRepository;
    private final NotificacionRepository notificacionRepository;

    public Page<Reporte> obtenerReportes(EstadoReporte estado, Pageable pageable) {
        if (estado != null) {
            // Need a method in repository or just use findAll if not present.
            // For now let's assume we create findByEstado in ReporteRepository.
            return reporteRepository.findByEstado(estado, pageable);
        }
        return reporteRepository.findAll(pageable);
    }

    public Reporte obtenerDetalle(UUID reporteId) {
        return reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));
    }

    @Transactional
    public void resolver(UUID reporteId, EjecutarDecisionDto dto, Long administradorId) {
        Reporte reporte = obtenerDetalle(reporteId);
        if (reporte.getEstado() == EstadoReporte.RESUELTO) {
            throw new ReglaDeNegocioException("El reporte ya ha sido resuelto");
        }

        DecisionModeracion decision = DecisionModeracion.builder()
                .reporte(reporte)
                .decision(dto.getDecision())
                .justificacion(dto.getJustificacion())
                .administradorId(administradorId)
                .fecha(LocalDateTime.now())
                .build();
        
        decisionRepository.save(decision);

        aplicarDecision(reporte, dto.getDecision());

        reporte.setEstado(EstadoReporte.RESUELTO);
        reporteRepository.save(reporte);

        enviarNotificacionDecision(reporte, dto);
    }

    private void aplicarDecision(Reporte reporte, TipoDecision decision) {
        if (reporte.getObjetivo() == ObjetivoReporte.PLATO) {
            Plato plato = platoRepository.findById(reporte.getObjetivoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado"));
            
            if (decision == TipoDecision.INHABILITAR_PUBLICACION) {
                plato.setEstado(EstadoPlato.OCULTO);
                platoRepository.save(plato);
            } else if (decision == TipoDecision.RESTAURAR_PUBLICACION) {
                if (plato.getPorcionesDisponibles() > 0) {
                    plato.setEstado(EstadoPlato.ACTIVO);
                } else {
                    plato.setEstado(EstadoPlato.AGOTADO);
                }
                platoRepository.save(plato);
            }
        } else if (reporte.getObjetivo() == ObjetivoReporte.CUENTA) {
            if (decision == TipoDecision.SUSPENDER_CUENTA) {
                PerfilCocinera perfil = perfilRepository.findById(reporte.getObjetivoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));
                
                Cuenta cuenta = perfil.getCuenta();
                if (cuenta != null) {
                    cuenta.setEstado(EstadoCuenta.SUSPENDIDO);
                    cuentaRepository.save(cuenta);
                }
            }
        }
    }

    private void enviarNotificacionDecision(Reporte reporte, EjecutarDecisionDto dto) {
        Long destinatarioId = null;
        UUID cocineraId = null;
        Rol rolDestinatario = Rol.COMPRADOR;

        if (reporte.getObjetivo() == ObjetivoReporte.PLATO) {
            Plato plato = platoRepository.findById(reporte.getObjetivoId()).orElse(null);
            if (plato != null) {
                cocineraId = plato.getCocineraId();
                rolDestinatario = Rol.COCINERA;
            }
        } else {
            cocineraId = reporte.getObjetivoId();
            rolDestinatario = Rol.COCINERA;
        }

        Notificacion notif = Notificacion.builder()
                .tipo(TipoNotificacion.MODERACION_DECISION)
                .titulo("Decisión de Moderación")
                .mensaje("Resolución: " + dto.getDecision() + ". Razón: " + dto.getJustificacion())
                .cocineraId(cocineraId)
                .rolDestinatario(rolDestinatario)
                .fechaCreacion(LocalDateTime.now())
                .leida(false)
                .build();
        
        notificacionRepository.save(notif);
    }

    public List<PerfilCocinera> listarPerfilesPausados() {
        return perfilRepository.findByPausadaTrue();
    }

    @Transactional
    public void reactivarPerfil(UUID cocineraId, String justificacion, Long administradorId) {
        PerfilCocinera perfil = perfilRepository.findById(cocineraId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));
        
        if (!perfil.isPausada()) {
            throw new ReglaDeNegocioException("El perfil no está pausado");
        }

        perfil.setPausada(false);
        perfil.setFechaReactivacion(LocalDateTime.now());
        perfilRepository.save(perfil);

        DecisionModeracion decision = DecisionModeracion.builder()
                .decision(TipoDecision.REACTIVAR_PERFIL)
                .justificacion(justificacion)
                .administradorId(administradorId)
                .fecha(LocalDateTime.now())
                .build();
        decisionRepository.save(decision);

        Notificacion notif = Notificacion.builder()
                .tipo(TipoNotificacion.MODERACION_DECISION)
                .titulo("Perfil Reactivado")
                .mensaje("Tu perfil ha sido reactivado. Razón: " + justificacion)
                .cocineraId(cocineraId)
                .rolDestinatario(Rol.COCINERA)
                .fechaCreacion(LocalDateTime.now())
                .leida(false)
                .build();
        notificacionRepository.save(notif);
    }
}
