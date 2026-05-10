package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.dtos.PartidoResponseDTO;
import com.madisonpadel.torneo.dtos.ResultadoRequestDTO;
import com.madisonpadel.torneo.entities.Partido;
import com.madisonpadel.torneo.services.PartidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partidos")
public class PartidoController {

    @Autowired
    private PartidoService partidoService;

    @PutMapping("/{id}/resultado")
    public ResponseEntity<?> cargarResultado(
            @PathVariable Long id, 
            @RequestBody ResultadoRequestDTO resultado) {
        
        try {
            // 1. El Service hace el trabajo pesado y nos devuelve la entidad gigante
            Partido partido = partidoService.cargarResultado(id, resultado);

            // 2. Armamos nuestra "bandejita" limpia solo con lo que queremos mostrar
            PartidoResponseDTO response = PartidoResponseDTO.builder()
                    .partidoId(partido.getId())
                    .estado(partido.getEstado().name())
                    .setsPareja1(partido.getSetsPareja1())
                    .setsPareja2(partido.getSetsPareja2())
                    .mensaje("¡Resultado cargado con éxito! El partido está " + partido.getEstado().name())
                    .build();

            // 3. Devolvemos el JSON hermoso y chiquito
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno del servidor");
        }
    }
}