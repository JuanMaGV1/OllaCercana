package com.ollacercana.model.domain;


public enum MotivoRechazo {
    INGREDIENTES_INSUFICIENTES("Me quedé sin ingredientes para esta porción"),
    SIN_TIEMPO_DE_ENTREGA("No alcanzo a entregar el pedido a tiempo"),
    PROBLEMA_DE_ENTREGA("No puedo hacer la entrega en el punto acordado"),
    IMPREVISTO_PERSONAL("Tuve un imprevisto personal"),
    OTRO("Otro motivo");

    private final String descripcion;

    MotivoRechazo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}