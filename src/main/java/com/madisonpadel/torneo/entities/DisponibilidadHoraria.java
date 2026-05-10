package com.madisonpadel.torneo.entities; 
import jakarta.persistence.*;
import lombok.*;            
import java.time.LocalTime;  
import com.madisonpadel.torneo.entities.DiaTorneo;

@Entity
@Table(name = "disponibilidad_horaria")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadHoraria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DiaTorneo dia; // VIERNES, SABADO, DOMINGO

    private LocalTime horaInicio;
    
    private Integer cantidadCanchas;
    
    @Builder.Default
    private Integer duracionTurnoMinutos = 60;

    private LocalTime horaFin;
}