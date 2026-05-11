package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.dtos.InscripcionRequestDTO;
import com.madisonpadel.torneo.services.InscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Le dice a Spring: "Soy una API REST, devuelvo datos (JSON o texto), no páginas HTML"
@RequestMapping("/api/inscripciones") // La ruta base en el navegador o Postman
@RequiredArgsConstructor
public class InscripcionController {

    private final InscripcionService inscripcionService;

    @PostMapping("/validar")
    public ResponseEntity<String> validarInscripcion(@RequestBody InscripcionRequestDTO request) {
        try {
            // Llamamos a nuestro gerente (el Service) pasándole los datos que llegaron en el JSON
            String resultado = inscripcionService.procesarInscripcion(
                request.getTelefonoJ1(), 
                request.getTelefonoJ2(), 
                request.getIdCategoria()
                );

            return ResponseEntity.ok(resultado);     
            } catch (IllegalArgumentException e) {
                    // Si el gerente detectó una regla rota (ej. bajó de categoría), atrapamos la Exception
                    // y devolvemos un HTTP 400 (Bad Request) con el texto exacto del error
                    return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}