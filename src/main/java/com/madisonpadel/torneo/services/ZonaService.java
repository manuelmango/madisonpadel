package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.entities.*;
import com.madisonpadel.torneo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ZonaService {

    private final ZonaRepository zonaRepository;
    private final ParejaRepository parejaRepository;

    private List<Zona> crearZonasVacias(int cantidad) {
        List<Zona> zonas = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            char letra = (char) ('A' + i);
            zonas.add(Zona.builder()
                    .nombre("Zona " + letra)
                    .parejas(new ArrayList<>())
                    .partidos(new ArrayList<>())
                    .build());
        }
        return zonas;
    }    
@Transactional
    public void generarZonasPorCategoria(Long categoriaId) {
        // --- LA LÍNEA QUE FALTABA ---
        // Buscamos a todas las parejas que se anotaron en esta categoría
        List<Pareja> todasLasParejas = parejaRepository.findByCategoriaId(categoriaId);
        // ----------------------------

        // 1. Separamos por privilegio (Sin Restricciones vs Con Restricciones)
        List<Pareja> sinRestricciones = todasLasParejas.stream()
                .filter(p -> p.getRestricciones().isEmpty())
                .sorted(Comparator.comparingInt(Pareja::getPuntosTotales).reversed()) 
                .toList();

        List<Pareja> conRestricciones = todasLasParejas.stream()
                .filter(p -> !p.getRestricciones().isEmpty())
                .sorted(Comparator.comparingInt(Pareja::getPuntosTotales).reversed())
                .toList();

        // 3. Unimos las listas: los "limpios" primero para que sean cabezas de serie
        List<Pareja> listaOrdenadaParaDraft = new ArrayList<>();
        listaOrdenadaParaDraft.addAll(sinRestricciones);
        listaOrdenadaParaDraft.addAll(conRestricciones);

        // 4. Creamos las zonas vacías (A, B, C...)
        int cantidadZonas = listaOrdenadaParaDraft.size() / 3;
        List<Zona> zonas = crearZonasVacias(cantidadZonas);

        // 5. Aplicamos el Snake Draft (Zig-Zag)
        boolean ida = true;
        int zonaActual = 0;

        for (Pareja p : listaOrdenadaParaDraft) {
            zonas.get(zonaActual).getParejas().add(p);
            
            if (ida) {
                if (zonaActual < cantidadZonas - 1) zonaActual++;
                else ida = false;
            } else {
                if (zonaActual > 0) zonaActual--;
                else ida = true;
            }
        }

        // 6. Generamos los partidos para cada zona
        for (Zona z : zonas) {
            generarPartidosParaZona(z);
        }
        
        // 7. Guardamos todo
        zonaRepository.saveAll(zonas);
    }
    private void generarPartidosParaZona(Zona zona) {
        List<Pareja> p = zona.getParejas();
        if (p.size() < 3) return; // Por las dudas, si la zona no está completa

        // Definimos un horario de inicio por defecto (ej: 14:00)
        // Esto después lo podemos hacer dinámico
        LocalTime horaInicio = LocalTime.of(14, 0);

        List<Partido> partidos = new ArrayList<>();

        // Partido 1: Pareja 1 vs Pareja 2 (14:00)
        partidos.add(crearPartido(zona, p.get(0), p.get(1), horaInicio));

        // Partido 2: Pareja 1 vs Pareja 3 (15:00)
        partidos.add(crearPartido(zona, p.get(0), p.get(2), horaInicio.plusHours(1)));

        // Partido 3: Pareja 2 vs Pareja 3 (16:00)
        partidos.add(crearPartido(zona, p.get(1), p.get(2), horaInicio.plusHours(2)));

        zona.setPartidos(partidos);
    }

    private Partido crearPartido(Zona z, Pareja p1, Pareja p2, LocalTime hora) {
        return Partido.builder()
                .zona(z)
                .pareja1(p1)
                .pareja2(p2)
                .dia(DiaTorneo.SABADO) // Por defecto para la prueba
                .hora(hora)
                .jugado(false)
                .build();
    }
}