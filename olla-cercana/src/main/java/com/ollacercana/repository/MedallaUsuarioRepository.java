package com.ollacercana.repository;

import com.ollacercana.domain.CodigoMedalla;
import com.ollacercana.domain.MedallaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedallaUsuarioRepository extends JpaRepository<MedallaUsuario, UUID> {

    boolean existsByUsuarioIdAndMedallaCodigo(Long usuarioId, CodigoMedalla codigo);

    Optional<MedallaUsuario> findByUsuarioIdAndMedallaCodigo(Long usuarioId, CodigoMedalla codigo);

    /** Solo medallas vigentes: sin vencimiento o con vencimiento posterior a {@code ahora}. */
    @Query("SELECT m FROM MedallaUsuario m WHERE m.usuarioId = :usuarioId " +
            "AND (m.vigenteHasta IS NULL OR m.vigenteHasta > :ahora) " +
            "ORDER BY m.fechaOtorgada DESC")
    List<MedallaUsuario> findVigentes(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);
}
