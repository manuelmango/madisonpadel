package com.madisonpadel.torneo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InscripcionRequestDTO {
    private String telefonoJ1;
    private String telefonoJ2; // <--- ¡Esta es la que te falta!
    private Long idCategoria;
}