package com.madisonpadel.torneo.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rankings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Builder.Default
    @Column(nullable = false)
    private Integer puntos = 0;
}