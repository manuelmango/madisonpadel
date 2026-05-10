package com.madisonpadel.torneo.services;

import java.util.List;
import com.madisonpadel.torneo.entities.RestriccionHoraria;
import com.madisonpadel.torneo.dtos.ParejaRequestDTO;
import com.madisonpadel.torneo.dtos.RestriccionRequestDTO;
import com.madisonpadel.torneo.entities.Categoria;
import com.madisonpadel.torneo.entities.Jugador;
import com.madisonpadel.torneo.entities.Pareja;
import com.madisonpadel.torneo.entities.RestriccionHoraria;
import com.madisonpadel.torneo.repositories.CategoriaRepository;
import com.madisonpadel.torneo.repositories.JugadorRepository;
import com.madisonpadel.torneo.repositories.ParejaRepository;
import com.madisonpadel.torneo.repositories.RestriccionHorariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ParejaService {

    private final ParejaRepository parejaRepository;
    private final JugadorRepository jugadorRepository;
    private final CategoriaRepository categoriaRepository;
    private final RestriccionHorariaRepository restriccionRepository;

    @Transactional
    public Pareja inscribirParejaCompleta(ParejaRequestDTO dto) {
        
        // 1. Buscamos a los jugadores y la categoría. 
        // Si no existen, el orElseThrow frena todo y tira un error claro.
        Jugador j1 = jugadorRepository.findByTelefono(dto.getTelefonoJugador1())
                .orElseThrow(() -> new IllegalArgumentException("Error: El jugador 1 con teléfono " + dto.getTelefonoJugador1() + " no está registrado."));

        Jugador j2 = jugadorRepository.findByTelefono(dto.getTelefonoJugador2())
                .orElseThrow(() -> new IllegalArgumentException("Error: El jugador 2 con teléfono " + dto.getTelefonoJugador2() + " no está registrado."));

        Categoria cat = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Error: La categoría seleccionada no existe."));

        // 2. Creamos y guardamos la pareja primero (para tener su ID en la base de datos)
        Pareja pareja = Pareja.builder()
                .jugador1(j1)
                .jugador2(j2)
                .categoria(cat)
                .build();
        
        Pareja parejaGuardada = parejaRepository.save(pareja);

        // 3. Si el DTO trae restricciones horarias, las guardamos y las atamos a esta pareja
        if (dto.getRestricciones() != null && !dto.getRestricciones().isEmpty()) {
            List<RestriccionHoraria> listaAConfigurar = dto.getRestricciones().stream()
                .map(resDto -> RestriccionHoraria.builder()
                        .pareja(parejaGuardada) // Acá usamos la pareja que acabamos de guardar
                        .dia(resDto.getDia())
                        .horaDesde(resDto.getHoraDesde())
                        .horaHasta(resDto.getHoraHasta())
                        .build())
                .toList();
            
            restriccionRepository.saveAll(listaAConfigurar);
        }

        return parejaGuardada;
    }
}