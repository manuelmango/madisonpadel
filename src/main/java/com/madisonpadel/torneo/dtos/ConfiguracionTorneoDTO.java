package com.madisonpadel.torneo.dtos;

import java.time.LocalTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ConfiguracionTorneoDTO {
    
    // Horarios del Viernes
    private LocalTime inicioViernes;
    private LocalTime finViernes;

    // Horarios del Sábado
    private LocalTime inicioSabado;
    private LocalTime finSabado;

    // Horarios del Domingo (Playoffs)
    private LocalTime inicioDomingo;
    private LocalTime finDomingo;

    // Getters y Setters (o la anotación @Data / @Getter @Setter si usás Lombok)

}