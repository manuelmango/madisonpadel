package com.madisonpadel.torneo.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity // Indica que esta clase es una tabla en la base de datos
@Table(name = "jugadores")
@Getter // Lombok genera los getNombre(), getApellido(), etc.
@Setter // Lombok genera los setNombre(), etc.
@NoArgsConstructor // Constructor vacío requerido por JPA
@AllArgsConstructor // Constructor con todos los atributos
@Builder // Permite crear objetos de forma fluida: Jugador.builder().nombre("...").build()
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String telefono;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Enumerated(EnumType.STRING) // Guarda el texto "MASCULINO" en la DB, no el número 0 o 1
    private Genero genero;

    @Column(name = "categoria_base")
    private Integer categoriaBase; // El número (1 a 8) que hablamos para las validaciones
    @Column(name = "puntos_ranking")
    private Integer puntosRanking = 0;
}