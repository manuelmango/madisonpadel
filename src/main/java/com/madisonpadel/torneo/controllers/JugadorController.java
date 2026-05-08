package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.dtos.NuevoJugadorDTO;
import com.madisonpadel.torneo.entities.Jugador;
import com.madisonpadel.torneo.services.JugadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jugadores")
@RequiredArgsConstructor
public class JugadorController {

    private final JugadorService jugadorService;

    // Con @PostMapping indicamos que esta ruta sirve para CREAR datos nuevos
    @PostMapping
    public ResponseEntity<String> crearJugador(@RequestBody NuevoJugadorDTO request) {
        try {
            Jugador jugadorCreado = jugadorService.crearJugador(request);
            return ResponseEntity.ok("¡Éxito! Jugador " + jugadorCreado.getNombre() + " " + jugadorCreado.getApellido() + " registrado correctamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}