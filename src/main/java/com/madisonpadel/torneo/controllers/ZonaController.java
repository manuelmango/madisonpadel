package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.dtos.PosicionZonaDTO;
import com.madisonpadel.torneo.services.TablaPosicionesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
public class ZonaController {

    private final TablaPosicionesService tablaPosicionesService;

    @GetMapping("/{id}/posiciones")
    public ResponseEntity<List<PosicionZonaDTO>> obtenerTablaPosiciones(@PathVariable Long id) {
        try {
            // Llamamos a nuestro motor matemático
            List<PosicionZonaDTO> tabla = tablaPosicionesService.calcularTablaDeZona(id);
            
            // Devolvemos la lista ordenada en formato JSON con un código 200 OK
            return ResponseEntity.ok(tabla);
            
        } catch (Exception e) {
            // Si algo explota (ej: la zona no existe), atajamos el error
            return ResponseEntity.internalServerError().build();
        }
    }
}