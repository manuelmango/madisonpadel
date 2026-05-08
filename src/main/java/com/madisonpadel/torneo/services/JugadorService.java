package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.dtos.NuevoJugadorDTO;
import com.madisonpadel.torneo.entities.Jugador;
import com.madisonpadel.torneo.repositories.JugadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JugadorService {

    private final JugadorRepository jugadorRepository;

    /**
     * Recibe el DTO, valida que el DNI no exista y guarda al jugador.
     */
    public Jugador crearJugador(NuevoJugadorDTO dto) {
        
        // 1. Validar duplicados (Regla de negocio)
        // El isPresent() es de Java Optional. Pregunta: ¿Encontraste a alguien con este DNI?
        if (jugadorRepository.findByDni(dto.getDni()).isPresent()) {
            throw new IllegalArgumentException("Error: Ya existe un jugador registrado con el DNI " + dto.getDni());
        }

        // 2. Si el DNI está libre, armamos la entidad usando el Builder
        Jugador nuevoJugador = Jugador.builder()
                .dni(dto.getDni())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .genero(dto.getGenero())
                .categoriaBase(dto.getCategoriaBase())
                .build();

        // 3. Lo guardamos en la base de datos y lo devolvemos
        return jugadorRepository.save(nuevoJugador);
    }
}