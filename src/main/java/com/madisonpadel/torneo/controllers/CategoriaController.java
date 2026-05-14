package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.dtos.CategoriaDTO;
import com.madisonpadel.torneo.entities.Categoria;
import com.madisonpadel.torneo.entities.Torneo;
import com.madisonpadel.torneo.repositories.CategoriaRepository;
import com.madisonpadel.torneo.repositories.TorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final TorneoRepository torneoRepository;

    @GetMapping
    public ResponseEntity<List<Categoria>> listarTodas() {
        return ResponseEntity.ok(categoriaRepository.findAll());
    }

    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<Categoria>> listarPorTorneo(@PathVariable Long torneoId) {
        return ResponseEntity.ok(categoriaRepository.findByTorneoId(torneoId));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CategoriaDTO dto) {
        Torneo torneo = torneoRepository.findById(dto.getTorneoId())
            .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        Categoria categoria = Categoria.builder()
            .nombre(dto.getNombre())
            .genero(dto.getGenero())
            .nivel(dto.getNivel())
            .torneo(torneo)
            .build();

        return ResponseEntity.status(201).body(categoriaRepository.save(categoria));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}