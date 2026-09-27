package com.ollacercana.exception;

public class ConflictoVersionException extends RuntimeException {

    private final Integer versionActual;
    private final Integer porcionesTotalesActuales;
    private final Integer porcionesComprometidasActuales;
    private final String estadoActual;

    public ConflictoVersionException(Integer versionActual,
                                      Integer porcionesTotalesActuales,
                                      Integer porcionesComprometidasActuales,
                                      String estadoActual) {
        super("El plato fue modificado por otra operación. Los valores mostrados ya no están vigentes.");
        this.versionActual = versionActual;
        this.porcionesTotalesActuales = porcionesTotalesActuales;
        this.porcionesComprometidasActuales = porcionesComprometidasActuales;
        this.estadoActual = estadoActual;
    }

    public Integer getVersionActual() {
        return versionActual;
    }

    public Integer getPorcionesTotalesActuales() {
        return porcionesTotalesActuales;
    }

    public Integer getPorcionesComprometidasActuales() {
        return porcionesComprometidasActuales;
    }

    public String getEstadoActual() {
        return estadoActual;
    }
}
