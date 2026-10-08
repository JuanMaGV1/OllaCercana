package com.ollacercana.persistence.repository;

import com.ollacercana.core.models.enums.CodigoMedalla;
import com.ollacercana.persistence.entities.MedallaUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedallaUsuarioRepository extends JpaRepository<MedallaUsuarioEntity, UUID> {

    boolean existsByUsuarioIdAndMedallaCodigo(Long usuarioId, CodigoMedalla codigo);

    Optional<MedallaUsuarioEntity> findByUsuarioIdAndMedallaCodigo(Long usuarioId, CodigoMedalla codigo);

    @Query("SELECT m FROM MedallaUsuarioEntity m WHERE m.usuarioId = :usuarioId " +
            "AND (m.vigenteHasta IS NULL OR m.vigenteHasta > :ahora) " +
            "ORDER BY m.fechaOtorgada DESC")
    List<MedallaUsuarioEntity> findVigentes(@Param("usuarioId") Long usuarioId,
                                            @Param("ahora") LocalDateTime ahora);
}
