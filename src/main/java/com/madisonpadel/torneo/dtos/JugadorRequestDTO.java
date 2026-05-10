package com.madisonpadel.torneo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class JugadorRequestDTO {
    private String nombre;
    private String apellido;
    private String telefono;
}