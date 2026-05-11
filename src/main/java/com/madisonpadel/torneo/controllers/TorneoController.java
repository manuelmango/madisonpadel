package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.dtos.ConfiguracionTorneoDTO;
import com.madisonpadel.torneo.services.PlayoffService;
import com.madisonpadel.torneo.services.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/torneos")
@RequiredArgsConstructor
public class TorneoController {

    private final ZonaService zonaService;
    private final PlayoffService playoffService;

    /**
     * Endpoint para el "Viernes": Genera los grupos de una categoría
     * aplicando ranking y restricciones horarias.
     */
    @PostMapping("/categorias/{categoriaId}/zonas")
    public ResponseEntity<?> generarZonas(@PathVariable Long categoriaId,@RequestBody ConfiguracionTorneoDTO config ) {
        try {
            zonaService.generarZonasPorCategoria(categoriaId, config);
            return ResponseEntity.ok("Zonas generadas con éxito para la categoría. Se han respetado los rankings y bloqueos horarios.");
        } catch (IllegalArgumentException e) {
            // Caso: La categoría no existe o no tiene suficientes parejas
            return ResponseEntity.badRequest().body("Error en la solicitud: " + e.getMessage());
        } catch (Exception e) {
            // Error genérico para no romper la app
            return ResponseEntity.internalServerError().body("Error interno al procesar las zonas: " + e.getMessage());
        }
    }

    /**
     * Endpoint para el "Domingo": Toma los resultados de las zonas,
     * arma la tabla general y vincula las parejas reales a los playoffs.
     */
    @PostMapping("/playoffs/iniciar-domingo")
    public ResponseEntity<?> iniciarDomingoPlayoffs(@RequestBody ConfiguracionTorneoDTO config) { // <--- 1. Recibimos el DTO
        try {
            // 2. Se lo pasamos al servicio
            playoffService.generarPlayoffsDomingo(config); 
            
            return ResponseEntity.ok("¡Proceso completado! Las llaves de Playoffs han sido vinculadas y los horarios asignados.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Conflicto en el estado del torneo: " + e.getMessage());
        } catch (Exception e) {
            // Imprimimos el error en consola para que vos puedas debugear si algo falla
            e.printStackTrace(); 
            return ResponseEntity.internalServerError().body("Ocurrió un error inesperado al armar el cuadro de playoffs.");
        }
    }
}