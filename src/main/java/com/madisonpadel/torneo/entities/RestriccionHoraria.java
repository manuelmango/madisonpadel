package com.madisonpadel.torneo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "restricciones_horarias")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RestriccionHoraria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A qué pareja pertenece esta restricción
    @ManyToOne(optional = false)
    @JoinColumn(name = "pareja_id", nullable = false)
    private Pareja pareja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiaTorneo dia; // VIERNES, SABADO o DOMINGO

    // La hora exacta en la que arranca su restricción (ej. 14:00)
    @Column(name = "hora_desde", nullable = false)
    private LocalTime horaDesde;

    // La hora exacta en la que termina su restricción (ej. 18:00)
    @Column(name = "hora_hasta", nullable = false)
    private LocalTime horaHasta;
}