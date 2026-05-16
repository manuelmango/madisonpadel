package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.dtos.JugadorRequestDTO;
import com.madisonpadel.torneo.dtos.NuevoJugadorDTO;
import com.madisonpadel.torneo.entities.Jugador;
import com.madisonpadel.torneo.services.JugadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/jugadores")
@RequiredArgsConstructor
public class JugadorController {

    private final JugadorService jugadorService;
    @PostMapping
    public ResponseEntity<?> crearJugador(@RequestBody NuevoJugadorDTO request) {
        try {
            // Le pasamos la planilla al Service
            Jugador jugadorCreado = jugadorService.crearJugador(request);
            
            // Si todo salió bien, devolvemos un 201 Created y los datos del jugador
            return ResponseEntity.status(HttpStatus.CREATED).body(jugadorCreado);
            
        } catch (IllegalArgumentException e) {
            // Si el Service saltó porque el teléfono estaba duplicado, atajamos el error acá
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<List<Jugador>> listarTodos() {
        return ResponseEntity.ok(jugadorService.findAll());
    }
}