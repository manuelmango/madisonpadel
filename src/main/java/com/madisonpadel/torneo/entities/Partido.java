package com.madisonpadel.torneo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "partidos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "zona_id")
    private Zona zona;

    @ManyToOne
    private Pareja pareja1;

    @ManyToOne
    private Pareja pareja2;

    // El resultado (se llena después de jugar)
    private Integer setsPareja1;
    private Integer setsPareja2;

    // Programación (Lo que aparece en la matriz)
    @Enumerated(EnumType.STRING)
    private DiaTorneo dia;
    
    private LocalTime hora;

    private boolean jugado = false;
}