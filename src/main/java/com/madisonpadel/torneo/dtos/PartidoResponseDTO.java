package com.madisonpadel.torneo.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class PartidoResponseDTO {
    private Long partidoId;
    private String estado;
    private Integer setsPareja1;
    private Integer setsPareja2;
    private String mensaje; // Un texto lindo para leer rápido
}