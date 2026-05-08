package com.madisonpadel.torneo.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class ParejaRequestDTO {
    private String dniJugador1;
    private String dniJugador2;
    private Long idCategoria;
    private List<RestriccionRequestDTO> restricciones; // Nueva lista
}