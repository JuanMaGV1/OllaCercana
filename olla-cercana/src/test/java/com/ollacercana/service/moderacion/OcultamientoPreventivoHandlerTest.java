package com.ollacercana.service.moderacion;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.ObjetivoReporte;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.Reporte;
import com.ollacercana.repository.PlatoRepository;
import com.ollacercana.repository.ReporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcultamientoPreventivoHandlerTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private PlatoRepository platoRepository;

    @InjectMocks
    private OcultamientoPreventivoHandler handler;

    @Test
    void testOcultamientoPreventivoAplicadoAlTercerReporte() {
        UUID platoId = UUID.randomUUID();
        Reporte reporte = Reporte.builder()
                .objetivo(ObjetivoReporte.PLATO)
                .objetivoId(platoId)
                .build();

        Plato plato = new Plato();
        plato.setId(platoId);
        plato.setEstado(EstadoPlato.ACTIVO);

        when(reporteRepository.countDistinctReportanteIdByObjetivoIdAndObjetivo(platoId, ObjetivoReporte.PLATO))
                .thenReturn(3L);
        when(platoRepository.findById(platoId)).thenReturn(Optional.of(plato));

        handler.procesar(reporte);

        verify(platoRepository, times(1)).save(plato);
        assertEquals(EstadoPlato.OCULTO, plato.getEstado());
    }

    @Test
    void testOcultamientoPreventivoNoAplicadoMenosDeTresReportes() {
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
}
