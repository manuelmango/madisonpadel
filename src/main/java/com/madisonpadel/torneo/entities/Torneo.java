package com.madisonpadel.torneo.entities;

import com.madisonpadel.torneo.entities.enums.EstadoTorneo;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "torneos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoTorneo estado = EstadoTorneo.INSCRIPCION;

    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Categoria> categorias;
    // En Torneo.java agregar:
    private LocalTime inicioViernes;
    private LocalTime finViernes;
    private Integer canchasViernes;

    private LocalTime inicioSabado;
    private LocalTime finSabado;
    private Integer canchasSabado;

    private LocalTime inicioDomingo;
    private LocalTime finDomingo;
    private Integer canchasDomingo;
}