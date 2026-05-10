package com.madisonpadel.torneo.entities.enums;

public enum FasePartido {
    ZONA, DIECISEISAVOS, OCTAVOS, CUARTOS, SEMIFINAL, FINAL;

    // Magia pura para el árbol: saber qué fase viene antes
    public FasePartido faseAnterior() {
        switch (this) {
            case FINAL: return SEMIFINAL;
            case SEMIFINAL: return CUARTOS;
            case CUARTOS: return OCTAVOS;
            case OCTAVOS: return DIECISEISAVOS;
            default: return null;
        }
    }
}