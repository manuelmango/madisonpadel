package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.dtos.ConfiguracionTorneoDTO;
import com.madisonpadel.torneo.services.PlayoffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playoffs")
@RequiredArgsConstructor
public class PlayoffController {

    private final PlayoffService playoffService;

    @PostMapping("/playoffs/generar")
    public ResponseEntity<String> generarPlayoffs(@RequestBody ConfiguracionTorneoDTO config) {
        playoffService.generarPlayoffsDomingo(config);
        return ResponseEntity.ok("Cuadro de domingo generado con éxito");
    }
}