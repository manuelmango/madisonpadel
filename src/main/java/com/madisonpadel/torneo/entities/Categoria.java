package com.madisonpadel.torneo.entities;

import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.madisonpadel.torneo.entities.Torneo;

@Entity
@Table(name = "categorias")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Categoria {
    // En Categoria.java, agregar:
    @ManyToOne
    @JoinColumn(name = "torneo_id")
    @JsonIgnore  // ← AGREGAR ESTO
    private Torneo torneo;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // Ej: "7ma", "6ta", "Suma 12"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero; // Reutilizamos el Enum Genero que ya creaste

    @Column(nullable = false)
    private Integer nivel; // Valor numérico para lógica: 1 (mejor) a 8 (inicial)
}