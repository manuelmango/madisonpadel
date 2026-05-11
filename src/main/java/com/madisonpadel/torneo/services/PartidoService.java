package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.dtos.ResultadoRequestDTO;
import com.madisonpadel.torneo.entities.Pareja;
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

        // 4. Determinamos quién ganó y quién perdió guardándolos en variables
        Pareja parejaGanadora;
        Pareja parejaPerdedora;

        if (resultado.getSetsPareja1() > resultado.getSetsPareja2()) {
            parejaGanadora = partido.getPareja1();
            parejaPerdedora = partido.getPareja2();
        } else if (resultado.getSetsPareja2() > resultado.getSetsPareja1()) {
            parejaGanadora = partido.getPareja2();
            parejaPerdedora = partido.getPareja1();
        } else {
            throw new IllegalArgumentException("Error: En el pádel no existen los empates. Revisá los sets ingresados.");
        }
        // Guardamos al ganador en la entidad como ya lo hacías
        partido.setGanador(parejaGanadora);
        // 5. Cambiamos el estado a FINALIZADO
        partido.setEstado(EstadoPartido.FINALIZADO);
        // Movemos al ganador a su próximo partido (Domingo o P3 de zona)
        if (partido.getSiguientePartido() != null) {
            acomodarEnSillaVacia(parejaGanadora, partido.getSiguientePartido());
        }
        // Movemos al perdedor a su próximo partido (Solo en P1 y P2 de zonas de 4)
        if (partido.getSiguientePartidoPerdedor() != null) {
            acomodarEnSillaVacia(parejaPerdedora, partido.getSiguientePartidoPerdedor());
        }
        return partidoRepository.save(partido);
    }
    private void acomodarEnSillaVacia(Pareja pareja, Partido partidoDestino) {
    // Si no hay destino, no hacemos nada
    if (partidoDestino == null) return; 

    // Nos fijamos qué silla está libre y lo sentamos
    if (partidoDestino.getPareja1() == null) {
        partidoDestino.setPareja1(pareja);
    } else if (partidoDestino.getPareja2() == null) {
        partidoDestino.setPareja2(pareja);
    }
    
    // Guardamos el partido destino actualizado
    partidoRepository.save(partidoDestino);
}
}