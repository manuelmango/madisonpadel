package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.dtos.ResultadoRequestDTO;
import com.madisonpadel.torneo.entities.Partido;
import com.madisonpadel.torneo.entities.enums.EstadoPartido;
import com.madisonpadel.torneo.repositories.PartidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartidoService {

    @Autowired
    private PartidoRepository partidoRepository;

@Transactional
    public Partido cargarResultado(Long partidoId, ResultadoRequestDTO resultado) {
        
        // 1. Buscamos el partido
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("El partido no existe."));

        // 2. Validamos que no se esté cargando un partido ya finalizado
        if (partido.estaTerminado()) {
            throw new IllegalStateException("Este partido ya tiene un resultado cargado.");
        }

        // 3. Guardamos los números
        partido.setSetsPareja1(resultado.getSetsPareja1());
        partido.setSetsPareja2(resultado.getSetsPareja2());
        partido.setGamesPareja1(resultado.getGamesPareja1());
        partido.setGamesPareja2(resultado.getGamesPareja2());

        // 4. Determinamos automáticamente al ganador
        if (resultado.getSetsPareja1() > resultado.getSetsPareja2()) {
            partido.setGanador(partido.getPareja1());
        } else if (resultado.getSetsPareja2() > resultado.getSetsPareja1()) {
            partido.setGanador(partido.getPareja2());
        } else {
            throw new IllegalArgumentException("Error: En el pádel no existen los empates. Revisá los sets ingresados.");
        }

        // 5. Cambiamos el estado a FINALIZADO
        partido.setEstado(EstadoPartido.FINALIZADO);

        // --- 6. EL AVANCE AUTOMÁTICO (NUEVO) ---
        // Si el partido tiene una conexión hacia adelante (Ej: es una Semi que apunta a la Final)
        Partido siguiente = partido.getSiguientePartido();
        if (siguiente != null) {
            // Buscamos qué "silla" del siguiente partido está vacía y sentamos al ganador
            if (siguiente.getPareja1() == null) {
                siguiente.setPareja1(partido.getGanador());
            } else if (siguiente.getPareja2() == null) {
                siguiente.setPareja2(partido.getGanador());
            }
            // Guardamos el partido futuro para que la pareja ya quede registrada ahí
            partidoRepository.save(siguiente);
        }
        // ---------------------------------------

        // 7. Guardamos y devolvemos el partido actual
        return partidoRepository.save(partido);
    }
}