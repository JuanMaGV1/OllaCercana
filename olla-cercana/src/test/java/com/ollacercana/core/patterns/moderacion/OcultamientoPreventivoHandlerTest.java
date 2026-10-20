package com.ollacercana.core.patterns.moderacion;

import com.ollacercana.core.models.Reporte;
import com.ollacercana.core.models.enums.EstadoPlato;
import com.ollacercana.core.models.enums.ObjetivoReporte;
import com.ollacercana.persistence.entities.PlatoEntity;
import com.ollacercana.persistence.mappers.PlatoEntityMapper;
import com.ollacercana.persistence.repository.PlatoRepository;
import com.ollacercana.persistence.repository.ReporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcultamientoPreventivoHandlerTest {

    @Mock private ReporteRepository reporteRepository;
    @Mock private PlatoRepository platoRepository;
    @Mock private PlatoEntityMapper platoMapper;

    private OcultamientoPreventivoHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OcultamientoPreventivoHandler(reporteRepository, platoRepository, platoMapper);
    }

    @Test
    @DisplayName("Con 3 reportantes distintos se oculta el plato")
    void ocultamientoAplicadoAlTercerReporte() {
        UUID platoId = UUID.randomUUID();
        Reporte reporte = Reporte.builder()
                .objetivo(ObjetivoReporte.PLATO)
                .objetivoId(platoId)
                .build();

        PlatoEntity plato = PlatoEntity.builder()
                .id(platoId)
                .estado(EstadoPlato.ACTIVO)
                .build();

        when(reporteRepository.countDistinctReportanteIdByObjetivoIdAndObjetivo(platoId, ObjetivoReporte.PLATO))
                .thenReturn(3L);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        handler.procesar(reporte);

        verify(platoRepository).save(plato);
        assertEquals(EstadoPlato.OCULTO, plato.getEstado());
    }

    @Test
    @DisplayName("Con menos de 3 reportantes no se oculta el plato")
    void ocultamientoNoAplicadoMenosDeTresReportes() {
        UUID platoId = UUID.randomUUID();
        Reporte reporte = Reporte.builder()
                .objetivo(ObjetivoReporte.PLATO)
                .objetivoId(platoId)
                .build();

        when(reporteRepository.countDistinctReportanteIdByObjetivoIdAndObjetivo(platoId, ObjetivoReporte.PLATO))
                .thenReturn(2L);

        handler.procesar(reporte);

        verify(platoRepository, never()).findById(any());
        verify(platoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Si el objetivo no es PLATO, no toca el repositorio")
    void noAplicaParaObjetivoCuenta() {
        Reporte reporte = Reporte.builder()
                .objetivo(ObjetivoReporte.CUENTA)
                .objetivoId(UUID.randomUUID())
                .build();

        handler.procesar(reporte);

        verifyNoInteractions(platoRepository);
    }
}