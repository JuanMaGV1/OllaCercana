package com.ollacercana.repository;

import com.ollacercana.domain.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cuenta c WHERE c.identidad.correo = :correo")
    boolean existsByCorreo(@Param("correo") String correo);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cuenta c WHERE c.identidad.celular = :celular")
    boolean existsByCelular(@Param("celular") String celular);

    @Query("SELECT c FROM Cuenta c WHERE c.identidad.correo = :correo")
    Optional<Cuenta> findByCorreo(@Param("correo") String correo);

    @Query("SELECT c FROM Cuenta c WHERE c.identidad.correo = :identificador OR c.identidad.celular = :identificador")
    Optional<Cuenta> findByIdentificador(@Param("identificador") String identificador);
}