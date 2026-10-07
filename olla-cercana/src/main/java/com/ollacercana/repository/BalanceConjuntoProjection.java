package com.ollacercana.repository;

/**
 * HU-21: porciones publicadas y vendidas de un conjunto residencial en un periodo.
 */
public interface BalanceConjuntoProjection {

    String getConjunto();

    Long getPublicadas();

    Long getVendidas();
}
