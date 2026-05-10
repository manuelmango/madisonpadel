package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.dtos.PosicionZonaDTO;
import com.madisonpadel.torneo.entities.Pareja;
import com.madisonpadel.torneo.entities.Partido;
import com.madisonpadel.torneo.repositories.PartidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TablaPosicionesService {

    private final PartidoRepository partidoRepository;

    public List<PosicionZonaDTO> calcularTablaDeZona(Long zonaId) {
        
        // 1. Buscamos todos los partidos programados para esta Zona
        List<Partido> partidos = partidoRepository.findByZonaId(zonaId);

        // 2. Creamos un "Mapa" (diccionario) para ir anotando los puntos de cada pareja por su ID
        Map<Long, PosicionZonaDTO> tabla = new HashMap<>();

        // 3. Recorremos partido por partido
        for (Partido partido : partidos) {
            
            // Registramos a las parejas en la tabla con 0 puntos (por si todavía no jugaron)
            inicializarPareja(tabla, partido.getPareja1());
            inicializarPareja(tabla, partido.getPareja2());

            // Si el partido ya se jugó, hacemos la matemática
            if (partido.estaTerminado()) {
                PosicionZonaDTO statsP1 = tabla.get(partido.getPareja1().getId());
                PosicionZonaDTO statsP2 = tabla.get(partido.getPareja2().getId());

                statsP1.setPartidosJugados(statsP1.getPartidosJugados() + 1);
                statsP2.setPartidosJugados(statsP2.getPartidosJugados() + 1);

                // Sumamos los Sets
                statsP1.setSetsAFavor(statsP1.getSetsAFavor() + partido.getSetsPareja1());
                statsP1.setSetsEnContra(statsP1.getSetsEnContra() + partido.getSetsPareja2());
                statsP2.setSetsAFavor(statsP2.getSetsAFavor() + partido.getSetsPareja2());
                statsP2.setSetsEnContra(statsP2.getSetsEnContra() + partido.getSetsPareja1());

                // Sumamos los Games
                statsP1.setGamesAFavor(statsP1.getGamesAFavor() + partido.getGamesPareja1());
                statsP1.setGamesEnContra(statsP1.getGamesEnContra() + partido.getGamesPareja2());
                statsP2.setGamesAFavor(statsP2.getGamesAFavor() + partido.getGamesPareja2());
                statsP2.setGamesEnContra(statsP2.getGamesEnContra() + partido.getGamesPareja1());

                // Calculamos quién ganó y sumamos la victoria/derrota
                if (partido.getGanador() != null) {
                    if (partido.getGanador().getId().equals(partido.getPareja1().getId())) {
                        statsP1.setPartidosGanados(statsP1.getPartidosGanados() + 1);
                        statsP2.setPartidosPerdidos(statsP2.getPartidosPerdidos() + 1);
                    } else {
                        statsP2.setPartidosGanados(statsP2.getPartidosGanados() + 1);
                        statsP1.setPartidosPerdidos(statsP1.getPartidosPerdidos() + 1);
                    }
                }
            }
        }

        // 4. Agarramos todas las filas que calculamos y las pasamos a una lista
        List<PosicionZonaDTO> tablaFinal = new ArrayList<>(tabla.values());

        // 5. ¡LA MAGIA DE JAVA! Esto ordena la lista de mejor a peor usando tu método compareTo()
        Collections.sort(tablaFinal);

        return tablaFinal;
    }

    // Método auxiliar para crear la fila vacía de la pareja si es la primera vez que la leemos
    private void inicializarPareja(Map<Long, PosicionZonaDTO> tabla, Pareja pareja) {
        if (!tabla.containsKey(pareja.getId())) {
            PosicionZonaDTO dto = new PosicionZonaDTO();
            dto.setParejaId(pareja.getId());
            
            // Asumiendo que tenés la relación jugador1 y jugador2 en Pareja. Si no, ajustalo a cómo tengas el nombre.
            String nombre = pareja.getJugador1().getNombre() + " - " + pareja.getJugador2().getNombre();
            dto.setNombrePareja(nombre);
            
            tabla.put(pareja.getId(), dto);
        }
    }
}