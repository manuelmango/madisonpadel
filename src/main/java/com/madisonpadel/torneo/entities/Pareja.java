package com.madisonpadel.torneo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parejas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Pareja {
    // 1. Relación para que la pareja sepa sus restricciones (Soluciona getRestricciones)
    @OneToMany(mappedBy = "pareja", cascade = CascadeType.ALL)
    private List<RestriccionHoraria> restricciones = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el primer jugador
    @ManyToOne(optional = false)
    @JoinColumn(name = "jugador_1_id", nullable = false)
    private Jugador jugador1;

    // Relación con el segundo jugador
    @ManyToOne(optional = false)
    @JoinColumn(name = "jugador_2_id", nullable = false)
    private Jugador jugador2;

    // A qué categoría se están anotando juntos
    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // Atributo dinámico que armamos sobre la marcha para mostrar en pantalla
    // Ej: "Belasteguín - Coello"
    public String getNombreEquipo() {
        return jugador1.getApellido() + " - " + jugador2.getApellido();
    }
    public int getPuntosTotales() {
            int puntosJ1 = (jugador1.getPuntosRanking() != null) ? jugador1.getPuntosRanking() : 0;
            int puntosJ2 = (jugador2.getPuntosRanking() != null) ? jugador2.getPuntosRanking() : 0;
            return puntosJ1 + puntosJ2;
        }
    // Puntos totales de la pareja (para el Snake Draft)
    // No lo guardamos en BD, lo calculamos al vuelo pidiendo los puntos a otra tabla
    @Transient // Esta anotación le dice a Spring "no crees una columna para esto en SQL"
    @Builder.Default
    private Integer puntosTotales = 0;
}