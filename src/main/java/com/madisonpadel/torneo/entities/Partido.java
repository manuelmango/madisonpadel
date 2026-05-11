package com.madisonpadel.torneo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import com.madisonpadel.torneo.entities.enums.EstadoPartido;
import com.madisonpadel.torneo.entities.enums.FasePartido;

@Entity
@Table(name = "partidos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Partido {
    private Integer numeroCancha; // Ej: 1, 2, 3...
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Pareja pareja1;
    @ManyToOne
    private Pareja pareja2;
    @ManyToOne
    @JoinColumn(name = "zona_id")
    private Zona zona;
    
    // --- PROGRAMACIÓN ---
    @Enumerated(EnumType.STRING)
    private DiaTorneo dia;
    private LocalTime hora;

    // --- ESTADO DEL PARTIDO ---
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoPartido estado = EstadoPartido.PENDIENTE;
    
    @Enumerated(EnumType.STRING)
    
    private FasePartido fase; // ZONA, OCTAVOS, CUARTOS, SEMI, FINAL
    
    private String origenPareja1; 
    private String origenPareja2;
    
    @ManyToOne
    @JoinColumn(name = "siguiente_partido_id")
    private Partido siguientePartido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "siguiente_partido_perdedor_id")
    private Partido siguientePartidoPerdedor;

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