package com.ollacercana.repository;

import java.util.UUID;

/**
 * Resultado de agrupar las reservas completadas por plato (HU-20).
 */
public interface PlatoPedidosProjection {

    UUID getPlatoId();

    Long getTotalPedidos();

    Long getTotalPorciones();
}
