package com.madisonpadel.torneo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import com.madisonpadel.torneo.entities.enums.EstadoPartido;

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

    // --- PROGRAMACIÓN ---
    @Enumerated(EnumType.STRING)
    private DiaTorneo dia;
    
    private LocalTime hora;

    // --- ESTADO DEL PARTIDO ---
    // Reemplazamos el boolean 'jugado' por algo más completo
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoPartido estado = EstadoPartido.PENDIENTE;

    // --- RESULTADOS MATEMÁTICOS ---
    // Inicializamos en 0 para evitar NullPointerExceptions
    @Builder.Default
    private Integer setsPareja1 = 0;
    
    @Builder.Default
    private Integer setsPareja2 = 0;

    @Builder.Default
    private Integer gamesPareja1 = 0;

    @Builder.Default
    private Integer gamesPareja2 = 0;

    // --- GANADOR ---
    @ManyToOne
    @JoinColumn(name = "ganador_id")
    private Pareja ganador;

    // --- MÉTODOS ÚTILES ---
    // Este método reemplaza la necesidad de preguntar "if (partido.isJugado())"
    public boolean estaTerminado() {
        return this.estado == EstadoPartido.FINALIZADO || this.estado == EstadoPartido.WALKOVER;
    }
}