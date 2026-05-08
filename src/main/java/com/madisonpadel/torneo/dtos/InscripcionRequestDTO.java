package com.madisonpadel.torneo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InscripcionRequestDTO {
    private String dniJugador1;
    private String dniJugador2; // <--- ¡Esta es la que te falta!
    private Long idCategoria;
}