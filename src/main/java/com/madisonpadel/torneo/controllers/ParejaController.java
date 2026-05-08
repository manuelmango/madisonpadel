package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.dtos.ParejaRequestDTO;
import com.madisonpadel.torneo.entities.Pareja;
import com.madisonpadel.torneo.services.ParejaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parejas")
@RequiredArgsConstructor
public class ParejaController {

    private final ParejaService parejaService;

    @PostMapping("/inscribir")
    public ResponseEntity<?> inscribirPareja(@RequestBody ParejaRequestDTO request) {
        try {
            // Fíjate qué limpio queda: le pasamos todo el DTO (DNI 1, DNI 2, Categoria y Restricciones)
            parejaService.inscribirParejaCompleta(request);
            
            return ResponseEntity.ok("Pareja inscrita con éxito. Jugadores vinculados y horarios bloqueados guardados.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Siempre es buena práctica atajar otros errores por las dudas
            return ResponseEntity.internalServerError().body("Ocurrió un error al inscribir: " + e.getMessage());
        }
    }
}