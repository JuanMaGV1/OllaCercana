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
            return reporteRepository.findByEstado(estado, pageable);
        }
        return reporteRepository.findAll(pageable);
    }

    public Reporte obtenerDetalle(UUID reporteId) {
        return reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte", reporteId));
    }

    @Transactional
    public void resolver(UUID reporteId, EjecutarDecisionDto dto, Long administradorId) {
        Reporte reporte = obtenerDetalle(reporteId);
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
                    .orElseThrow(() -> new ResourceNotFoundException("Plato", reporte.getObjetivoId()));
            
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
                Cuenta cuenta = obtenerCuentaObjetivo(reporte);
                cuenta.setEstado(EstadoCuenta.SUSPENDIDO);
                cuentaRepository.save(cuenta);
            }
        }
    }

    private void enviarNotificacionDecision(Reporte reporte, EjecutarDecisionDto dto) {
        Long compradorId = null;
        UUID cocineraId = null;
        Rol rolDestinatario = Rol.COMPRADOR;

        if (reporte.getObjetivo() == ObjetivoReporte.PLATO) {
            Plato plato = platoRepository.findById(reporte.getObjetivoId()).orElse(null);
            if (plato != null) {
                cocineraId = plato.getCocineraId();
                rolDestinatario = Rol.COCINERA;
            }
        } else {
            Cuenta afectada = obtenerCuentaObjetivo(reporte);
            if (afectada.getRoles() != null && afectada.getRoles().contains(Rol.COCINERA)) {
                cocineraId = perfilRepository.findByCuentaId(afectada.getId())
                        .map(PerfilCocinera::getId)
                        .orElse(null);
                rolDestinatario = Rol.COCINERA;
            } else {
                compradorId = afectada.getId();
                rolDestinatario = Rol.COMPRADOR;
            }
        }

        Notificacion notif = Notificacion.builder()
                .tipo(TipoNotificacion.MODERACION_DECISION)
                .titulo("Decisión de Moderación")
                .mensaje("Resolución: " + dto.getDecision() + ". Razón: " + dto.getJustificacion())
                .compradorId(compradorId)
                .cocineraId(cocineraId)
                .rolDestinatario(rolDestinatario)
                .fechaCreacion(LocalDateTime.now())
                .leida(false)
                .build();
        
        notificacionRepository.save(notif);
    }

    private Cuenta obtenerCuentaObjetivo(Reporte reporte) {
        if (reporte.getCuentaObjetivoId() != null) {
            return cuentaRepository.findById(reporte.getCuentaObjetivoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + reporte.getCuentaObjetivoId()));
        }
        PerfilCocinera perfil = perfilRepository.findById(reporte.getObjetivoId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil", reporte.getObjetivoId()));
        if (perfil.getCuenta() == null) throw new ResourceNotFoundException("Cuenta", reporte.getObjetivoId());
        return perfil.getCuenta();
    }

    public List<PerfilCocinera> listarPerfilesPausados() {
        return perfilRepository.findByPausadaTrue();
    }

    @Transactional
    public void reactivarPerfil(UUID cocineraId, String justificacion, Long administradorId) {
        PerfilCocinera perfil = perfilRepository.findById(cocineraId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil", cocineraId));
        
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
