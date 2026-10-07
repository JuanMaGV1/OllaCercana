package com.ollacercana.repository;

import com.ollacercana.domain.EstadoPlato;
import com.ollacercana.domain.Plato;
import com.ollacercana.domain.TipoComida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PlatoRepository extends JpaRepository<Plato, UUID> {

    List<Plato> findByEstado(EstadoPlato estado);

    List<Plato> findByTipoComida(TipoComida tipo);

       
                                                                        
                                        
       
    List<Plato> findByCocineraIdAndEstado(UUID cocineraId, EstadoPlato estado);

       
                                                                      
                                            
       
    @Query("SELECT p FROM Plato p WHERE p.estado = :estado AND p.fechaExpiracion > :ahora " +
            "AND (p.porcionesTotales - COALESCE(p.porcionesComprometidas, 0)) > 0")
    List<Plato> findActivosVigentes(@Param("estado") EstadoPlato estado, @Param("ahora") LocalDateTime ahora);

       
                                                         
       
    long countByEstadoAndFechaExpiracionAfter(EstadoPlato estado, LocalDateTime ahora);

       
                                                                             
       
    long countByCocineraIdAndEstadoAndFechaExpiracionAfter(UUID cocineraId, EstadoPlato estado, LocalDateTime ahora);

       
                                                  
       
    List<Plato> findByEstadoInAndFechaExpiracionLessThanEqual(List<EstadoPlato> estados, LocalDateTime ahora);
}