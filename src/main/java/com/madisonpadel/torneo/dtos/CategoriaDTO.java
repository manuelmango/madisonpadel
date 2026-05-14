package com.madisonpadel.torneo.dtos;

import com.madisonpadel.torneo.entities.Genero;
import lombok.Data;

@Data
public class CategoriaDTO {
    private String nombre;
    private Genero genero;
    private Integer nivel;
    private Long torneoId;
}