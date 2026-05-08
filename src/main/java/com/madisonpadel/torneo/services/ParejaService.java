package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.entities.Jugador;
import com.madisonpadel.torneo.dtos.ParejaRequestDTO;
import com.madisonpadel.torneo.entities.Categoria;
import com.madisonpadel.torneo.entities.Pareja;
import com.madisonpadel.torneo.repositories.JugadorRepository;
import com.madisonpadel.torneo.repositories.CategoriaRepository;
import com.madisonpadel.torneo.repositories.ParejaRepository;
import com.madisonpadel.torneo.entities.RestriccionHoraria;
import com.madisonpadel.torneo.repositories.RestriccionHorariaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParejaService {

    // Traemos a los empleados (repositorios) y al otro gerente (InscripcionService)
    private final ParejaRepository parejaRepository;
    private final JugadorRepository jugadorRepository;
    private final CategoriaRepository categoriaRepository;
    private final InscripcionService inscripcionService;
    private final RestriccionHorariaRepository restriccionRepository;

@Transactional
    public Pareja inscribirParejaCompleta(ParejaRequestDTO dto) {
        
        // 1. Validaciones de DNI y niveles (Usamos el método que ya teníamos)

        
        // (Podés llamar a la lógica de validación que ya escribimos arriba)        // 2. Creamos y guardamos la pareja primero (para tener su ID)
        Jugador j1 = jugadorRepository.findByDni(dto.getDniJugador1()).get();
        Jugador j2 = jugadorRepository.findByDni(dto.getDniJugador2()).get();
        Categoria cat = categoriaRepository.findById(dto.getIdCategoria()).get();

        Pareja pareja = Pareja.builder()
                .jugador1(j1)
                .jugador2(j2)
                .categoria(cat)
                .build();
        
        Pareja parejaGuardada = parejaRepository.save(pareja);

        // 3. Si el DTO trae restricciones, las guardamos
        if (dto.getRestricciones() != null && !dto.getRestricciones().isEmpty()) {
            List<RestriccionHoraria> listaAConfigurar = dto.getRestricciones().stream()
                .map(resDto -> RestriccionHoraria.builder()
                        .pareja(parejaGuardada)
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