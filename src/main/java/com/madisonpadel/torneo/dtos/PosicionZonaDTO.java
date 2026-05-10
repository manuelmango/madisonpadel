package com.madisonpadel.torneo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PosicionZonaDTO implements Comparable<PosicionZonaDTO> {
    
    private Long parejaId;
    private String nombrePareja; // Ej: "Galán - Lebrón"
    
    private int partidosJugados = 0;
    private int partidosGanados = 0;
    private int partidosPerdidos = 0;
    
    private int setsAFavor = 0;
    private int setsEnContra = 0;
    
    private int gamesAFavor = 0;
    private int gamesEnContra = 0;

    // --- Fórmulas matemáticas automáticas ---
    public int getDiferenciaSets() {
        return this.setsAFavor - this.setsEnContra;
    }

    public int getDiferenciaGames() {
        return this.gamesAFavor - this.gamesEnContra;
    }

    // --- El Cerebro del Desempate ---
    // Este método le enseña a Java cómo ordenar las parejas de mejor a peor
    @Override
    public int compareTo(PosicionZonaDTO otraPareja) {
        // 1. Desempate por Partidos Ganados
        if (this.partidosGanados != otraPareja.partidosGanados) {
            return Integer.compare(otraPareja.partidosGanados, this.partidosGanados);
        }
        
        // 2. Si empatan en partidos, desempate por Diferencia de Sets
        if (this.getDiferenciaSets() != otraPareja.getDiferenciaSets()) {
            return Integer.compare(otraPareja.getDiferenciaSets(), this.getDiferenciaSets());
        }
        
        // 3. Si siguen empatados, desempate por Diferencia de Games
        return Integer.compare(otraPareja.getDiferenciaGames(), this.getDiferenciaGames());
    }
}