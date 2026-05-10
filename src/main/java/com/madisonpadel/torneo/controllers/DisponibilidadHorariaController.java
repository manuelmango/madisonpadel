package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.entities.DisponibilidadHoraria;
import com.madisonpadel.torneo.entities.DiaTorneo;
import com.madisonpadel.torneo.services.DisponibilidadHorariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class DisponibilidadHorariaController {

    private final DisponibilidadHorariaService service;

    // 1. Guardar o actualizar la configuración de un día
    @PostMapping
    public ResponseEntity<DisponibilidadHoraria> guardarConfiguracion(@RequestBody DisponibilidadHoraria request) {
        DisponibilidadHoraria guardada = service.guardarConfiguracion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // 2. Ver todos los horarios cargados (ideal para revisar qué configuraste)
    @GetMapping
    public ResponseEntity<List<DisponibilidadHoraria>> obtenerTodos() {
        return ResponseEntity.ok(service.obtenerTodaLaDisponibilidad());
    }

    // 3. Ver el horario de un día específico (ej: GET /api/horarios/SABADO)
    @GetMapping("/{dia}")
    public ResponseEntity<DisponibilidadHoraria> obtenerPorDia(@PathVariable DiaTorneo dia) {
        try {
            return ResponseEntity.ok(service.obtenerPorDia(dia));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}