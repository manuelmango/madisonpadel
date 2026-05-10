package com.madisonpadel.torneo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResultadoRequestDTO {
    private Integer setsPareja1;
    private Integer setsPareja2;
    private Integer gamesPareja1;
    private Integer gamesPareja2;
}