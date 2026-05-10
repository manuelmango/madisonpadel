package com.madisonpadel.torneo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "zonas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Ej: "Zona A"

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    @Transient
    private DiaTorneo diaDefecto;
    @Transient
    private LocalTime horaDefecto;
    // Las 3 parejas que componen la matriz
    @ManyToMany
    @JoinTable(
        name = "zona_parejas",
        joinColumns = @JoinColumn(name = "zona_id"),
        inverseJoinColumns = @JoinColumn(name = "pareja_id")
    )
    private List<Pareja> parejas;

    // Los partidos que se ven en los cuadraditos de la matriz
    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Partido> partidos;
}