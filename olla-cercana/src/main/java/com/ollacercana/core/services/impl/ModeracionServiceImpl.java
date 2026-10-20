package com.ollacercana.core.services.impl;

import com.ollacercana.controller.dtos.request.EjecutarDecisionDTO;
import com.ollacercana.controller.handlers.exception.ReglaDeNegocioException;
import com.ollacercana.controller.handlers.exception.ResourceNotFoundException;
import com.ollacercana.core.models.DecisionModeracion;
import com.ollacercana.core.models.Notificacion;
import com.ollacercana.core.models.PerfilCocinera;
import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.EstadoCuenta;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.EstadoReporte;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import com.ollacercana.core.models.enums.Rol;
import com.ollacercana.core.models.enums.TipoDecision;
import com.ollacercana.core.models.enums.TipoNotificacion;
import com.ollacercana.core.services.IModeracionService;
import com.ollacercana.persistence.entities.CuentaEntity;
import com.ollacercana.persistence.entities.PerfilCocineraEntity;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.entities.ReporteEntity;
import com.ollacercana.persistence.mappers.CuentaEntityMapper;
import com.ollacercana.persistence.mappers.DecisionModeracionEntityMapper;
import com.ollacercana.persistence.mappers.NotificacionDocumentMapper;
import com.ollacercana.persistence.mappers.PerfilCocineraDomainMapper;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.mappers.ReporteEntityMapper;
import com.ollacercana.persistence.repository.CuentaRepository;
import com.ollacercana.persistence.repository.DecisionModeracionRepository;
import com.ollacercana.persistence.repository.PerfilCocineraRepository;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReporteRepository;
import com.ollacercana.persistence.repository.mongo.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModeracionServiceImpl implements IModeracionService {

    private final ReporteRepository reporteRepository;
    private final DecisionModeracionRepository decisionRepository;
    private final PlatoRepository platoRepository;
    private final CuentaRepository cuentaRepository;
    private final PerfilCocineraRepository perfilRepository;
    private final NotificacionRepository notificacionRepository;

    private final ReporteEntityMapper reporteMapper;
    private final DecisionModeracionEntityMapper decisionMapper;
    private final PlatoEntityMapper platoMapper;
    private final CuentaEntityMapper cuentaMapper;
    private final PerfilCocineraDomainMapper perfilDomainMapper;
    private final NotificacionDocumentMapper notificacionMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<Reporte> obtenerReportes(EstadoReporte estado, Pageable pageable) {
        Page<ReporteEntity> pagina = (estado != null)
                ? reporteRepository.findByEstado(estado, pageable)
                : reporteRepository.findAll(pageable);

        List<Reporte> dominios = pagina.getContent().stream()
                .map(reporteMapper::toDomain)
                .toList();

        return new PageImpl<>(dominios, pageable, pagina.getTotalElements());
    }

    @Override
    public Reporte obtenerDetalle(UUID reporteId) {
        return reporteRepository.findById(reporteId)
                .map(reporteMapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte", reporteId));
    }

    @Override
    @Transactional
    public void resolver(UUID reporteId, EjecutarDecisionDTO dto, Long administradorId) {
        Reporte reporte = obtenerDetalle(reporteId);

        DecisionModeracion decision = DecisionModeracion.builder()
                .reporte(reporte)
                .decision(dto.getDecision())
                .justificacion(dto.getJustificacion())
                .administradorId(administradorId)
                .fecha(LocalDateTime.now(ZoneId.systemDefault()))
                .build();
        decisionRepository.save(decisionMapper.toEntity(decision));

        aplicarDecision(reporte, dto.getDecision());

        reporte.setEstado(EstadoReporte.RESUELTO);
        reporteRepository.save(reporteMapper.toEntity(reporte));

        enviarNotificacionDecision(reporte, dto);
    }

    private void aplicarDecision(Reporte reporte, TipoDecision decision) {
        if (reporte.getObjetivo() == ObjetivoReporte.PLATO) {
            aplicarDecisionPlato(reporte, decision);
        } else if (reporte.getObjetivo() == ObjetivoReporte.CUENTA && decision == TipoDecision.SUSPENDER_CUENTA) {
            aplicarDecisionCuenta(reporte);
        }
    }

    private void aplicarDecisionPlato(Reporte reporte, TipoDecision decision) {
        PlatoEntity platoEntity = platoRepository.findById(reporte.getObjetivoId())
                .orElseThrow(() -> new ResourceNotFoundException("Plato", reporte.getObjetivoId()));

        if (decision == TipoDecision.INHABILITAR_PUBLICACION) {
            platoEntity.setEstado(EstadoPlato.OCULTO);
            platoRepository.save(platoEntity);
        } else if (decision == TipoDecision.RESTAURAR_PUBLICACION) {
            int totales = platoEntity.getPorcionesTotales() == null ? 0 : platoEntity.getPorcionesTotales();
            int comprometidas = platoEntity.getPorcionesComprometidas() == null ? 0 : platoEntity.getPorcionesComprometidas();
            platoEntity.setEstado((totales - comprometidas > 0) ? EstadoPlato.ACTIVO : EstadoPlato.AGOTADO);
            platoRepository.save(platoEntity);
        }
    }

    private void aplicarDecisionCuenta(Reporte reporte) {
        CuentaEntity cuentaEntity = obtenerCuentaObjetivoEntity(reporte);
        cuentaEntity.setEstado(EstadoCuenta.SUSPENDIDO);
        cuentaRepository.save(cuentaEntity);
    }

    private void enviarNotificacionDecision(Reporte reporte, EjecutarDecisionDTO dto) {
        Long compradorId = null;
        UUID cocineraId = null;
        Rol rolDestinatario = Rol.COMPRADOR;

        if (reporte.getObjetivo() == ObjetivoReporte.PLATO) {
            PlatoEntity plato = platoRepository.findById(reporte.getObjetivoId()).orElse(null);
            if (plato != null) {
                cocineraId = plato.getCocineraId();
                rolDestinatario = Rol.COCINERA;
            }
        } else {
            CuentaEntity afectada = obtenerCuentaObjetivoEntity(reporte);
            if (afectada.getRoles() != null && afectada.getRoles().contains(Rol.COCINERA)) {
                cocineraId = perfilRepository.findByCuentaId(afectada.getId())
                        .map(PerfilCocineraEntity::getId)
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
                .fechaCreacion(LocalDateTime.now(ZoneId.systemDefault()))
                .leida(false)
                .build();
        notificacionRepository.save(notificacionMapper.toDocument(notif));
    }

    private CuentaEntity obtenerCuentaObjetivoEntity(Reporte reporte) {
        if (reporte.getCuentaObjetivoId() != null) {
            return cuentaRepository.findById(reporte.getCuentaObjetivoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cuenta", reporte.getCuentaObjetivoId()));
        }
        PerfilCocineraEntity perfil = perfilRepository.findById(reporte.getObjetivoId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil", reporte.getObjetivoId()));
        if (perfil.getCuenta() == null) {
            throw new ResourceNotFoundException("Cuenta", reporte.getObjetivoId());
        }
        return perfil.getCuenta();
    }

    @Override
    public List<PerfilCocinera> listarPerfilesPausados() {
        return perfilRepository.findByPausadaTrue().stream()
                .map(perfilDomainMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void reactivarPerfil(UUID cocineraId, String justificacion, Long administradorId) {
        PerfilCocineraEntity perfil = perfilRepository.findById(cocineraId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil", cocineraId));

        if (!perfil.isPausada()) {
            throw new ReglaDeNegocioException("El perfil no está pausado");
        }

        perfil.setPausada(false);
        perfil.setFechaReactivacion(LocalDateTime.now(ZoneId.systemDefault()));
        perfilRepository.save(perfil);

        DecisionModeracion decision = DecisionModeracion.builder()
                .decision(TipoDecision.REACTIVAR_PERFIL)
                .justificacion(justificacion)
                .administradorId(administradorId)
                .fecha(LocalDateTime.now(ZoneId.systemDefault()))
                .build();
        decisionRepository.save(decisionMapper.toEntity(decision));

        Notificacion notif = Notificacion.builder()
                .tipo(TipoNotificacion.MODERACION_DECISION)
                .titulo("Perfil Reactivado")
                .mensaje("Tu perfil ha sido reactivado. Razón: " + justificacion)
                .cocineraId(cocineraId)
                .rolDestinatario(Rol.COCINERA)
                .fechaCreacion(LocalDateTime.now(ZoneId.systemDefault()))
                .leida(false)
                .build();
        notificacionRepository.save(notificacionMapper.toDocument(notif));
    }
}