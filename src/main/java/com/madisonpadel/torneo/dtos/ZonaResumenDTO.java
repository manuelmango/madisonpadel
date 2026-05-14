package com.madisonpadel.torneo.dtos;

import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class ZonaResumenDTO {
    private Long id;
    private String nombre;
    private List<PartidoResumenDTO> partidos;

    @Data
    @Builder
    public static class PartidoResumenDTO {
        private Long id;
        private String pareja1;
        private String pareja2;
        private String dia;
        private LocalTime hora;
        private Integer numeroCancha;
        private String estado;
        private Integer setsPareja1;
        private Integer setsPareja2;
    }
}