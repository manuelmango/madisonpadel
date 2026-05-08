package com.madisonpadel.torneo.dtos;

import com.madisonpadel.torneo.entities.DiaTorneo;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Getter @Setter
public class RestriccionRequestDTO {
    private DiaTorneo dia;
    private LocalTime horaDesde;
    private LocalTime horaHasta;
}