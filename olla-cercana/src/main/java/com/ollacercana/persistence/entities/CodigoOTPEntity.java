package com.ollacercana.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "codigos_otp")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CodigoOTPEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private UUID perfilId;
    @Column(nullable = false, length = 10) private String codigo;
    @Column(nullable = false) private LocalDateTime fechaExpiracion;
    @Column(nullable = false) private boolean usado;
}