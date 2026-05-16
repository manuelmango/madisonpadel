package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.dtos.NuevoJugadorDTO;
import com.madisonpadel.torneo.entities.Jugador;
import com.madisonpadel.torneo.repositories.JugadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    public List<Jugador> findAll() {
        return jugadorRepository.findAll(Sort.by("apellido", "nombre"));
    }
    /**
     * Recibe el DTO, valida que el DNI no exista y guarda al jugador.
     */
    public Jugador crearJugador(NuevoJugadorDTO dto) {
        
        // 1. Validar duplicados (Regla de negocio)
        // El isPresent() es de Java Optional. Pregunta: ¿Encontraste a alguien con este DNI?
        if (jugadorRepository.findByTelefono(dto.getTelefono()).isPresent()) {
            throw new IllegalArgumentException("Error: Ya existe un jugador registrado con el telefono " + dto.getTelefono());
        }

        // 2. Si el DNI está libre, armamos la entidad usando el Builder
        Jugador nuevoJugador = Jugador.builder()
                .telefono(dto.getTelefono())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .genero(dto.getGenero())
                .categoriaBase(dto.getCategoriaBase())
                .build();

        // 3. Lo guardamos en la base de datos y lo devolvemos
        return jugadorRepository.save(nuevoJugador);
    }
}