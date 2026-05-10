package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.services.PlayoffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playoffs")
@RequiredArgsConstructor
public class PlayoffController {

    private final PlayoffService playoffService;

    @PostMapping("/iniciar-domingo")
    public ResponseEntity<String> arrancarPlayoffs() {
        playoffService.iniciarDomingoDePlayoffs();
        return ResponseEntity.ok("¡El puente se ha cruzado! Las parejas clasificadas ya están en sus llaves con horario y cancha asignados.");
    }
}