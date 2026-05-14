package com.madisonpadel.torneo.controllers;

import com.madisonpadel.torneo.dtos.PosicionZonaDTO;
import com.madisonpadel.torneo.dtos.ZonaResumenDTO;
import com.madisonpadel.torneo.services.TablaPosicionesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.madisonpadel.torneo.entities.Zona;
import com.madisonpadel.torneo.repositories.ZonaRepository;
import java.util.List;

@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
public class ZonaController {
    private final ZonaRepository zonaRepository;

    private final TablaPosicionesService tablaPosicionesService;
    @GetMapping
    public ResponseEntity<List<ZonaResumenDTO>> listarTodas() {
        return ResponseEntity.ok(zonaRepository.findAll().stream()
            .map(this::toDTO)
            .toList());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ZonaResumenDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(zonaRepository.findByCategoriaId(categoriaId).stream()
            .map(this::toDTO)
            .toList());
    }

    private ZonaResumenDTO toDTO(Zona zona) {
        List<ZonaResumenDTO.PartidoResumenDTO> partidos = zona.getPartidos() == null ? List.of() :
            zona.getPartidos().stream()
                .map(p -> ZonaResumenDTO.PartidoResumenDTO.builder()
                    .id(p.getId())
                    .pareja1(p.getPareja1() != null ? 
                        p.getPareja1().getJugador1().getNombre() + " / " + p.getPareja1().getJugador2().getNombre() : "Por definir")
                    .pareja2(p.getPareja2() != null ? 
                        p.getPareja2().getJugador1().getNombre() + " / " + p.getPareja2().getJugador2().getNombre() : "Por definir")
                    .dia(p.getDia() != null ? p.getDia().name() : null)
                    .hora(p.getHora())
                    .numeroCancha(p.getNumeroCancha())
                    .estado(p.getEstado().name())
                    .setsPareja1(p.getSetsPareja1())
                    .setsPareja2(p.getSetsPareja2())
                    .build())
                .toList();

        return ZonaResumenDTO.builder()
            .id(zona.getId())
            .nombre(zona.getNombre())
            .partidos(partidos)
            .build();
    }
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