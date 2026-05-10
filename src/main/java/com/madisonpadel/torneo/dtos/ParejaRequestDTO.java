package com.madisonpadel.torneo.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class ParejaRequestDTO {
    private String telefonoJugador1; // Antes decía dni
    private String telefonoJugador2; // Antes decía dni
    private Long idCategoria;
    private List<RestriccionRequestDTO> restricciones; 
}