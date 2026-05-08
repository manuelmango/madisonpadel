package com.madisonpadel.torneo.dtos;

import com.madisonpadel.torneo.entities.Genero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NuevoJugadorDTO {
    // Solo pedimos los datos necesarios para crear a la persona
    private String dni;
    private String nombre;
    private String apellido;
    private Genero genero;
    private Integer categoriaBase; // Ej: 7 (para séptima), 5 (para quinta)
}