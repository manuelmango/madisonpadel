package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.entities.*;
import com.madisonpadel.torneo.entities.enums.EstadoPartido;
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
    // 1. Traemos a todos ordenados por el Ranking real de Madison Padel
    List<Pareja> rankingParejas = parejaRepository.findByCategoriaId(categoriaId)
            .stream()
            .sorted(Comparator.comparingInt(Pareja::getPuntosTotales).reversed())
            .toList();

    int totalParejas = rankingParejas.size();
    int cantidadZonas = totalParejas / 3;
    List<Zona> zonas = crearZonasVacias(cantidadZonas);

    // 2. ASIGNACIÓN DE "SLOTS" A LAS ZONAS
    // Zonas Altas (A, B, C...) -> Sábado
    // Zonas Bajas (X, Y, Z...) -> Viernes
    for (int i = 0; i < zonas.size(); i++) {
        if (i < zonas.size() / 2) {
            zonas.get(i).setDiaDefecto(DiaTorneo.SABADO);
            zonas.get(i).setHoraDefecto(LocalTime.of(16, 0)); // Hora tentativa sábado
        } else {
            zonas.get(i).setDiaDefecto(DiaTorneo.VIERNES);
            zonas.get(i).setHoraDefecto(LocalTime.of(19, 0)); // Hora tentativa viernes
        }
    }

    // 3. REPARTO CON VALIDACIÓN DE RESTRICCIONES
    // Usamos una lista de parejas que todavía no tienen zona
    List<Pareja> pendientes = new ArrayList<>(rankingParejas);
    
    // El objetivo es llenar las zonas de la A a la Z respetando el nivel
    for (Zona zona : zonas) {
        while (zona.getParejas().size() < 3 && !pendientes.isEmpty()) {
            Pareja candidata = buscarMejorParejaParaZona(zona, pendientes);
            zona.getParejas().add(candidata);
            pendientes.remove(candidata);
        }
    }

    // 4. GENERAR PARTIDOS Y GUARDAR
    for (Zona z : zonas) {
        generarPartidosParaZona(z);
    }
    zonaRepository.saveAll(zonas);
}

/**
 * Busca la pareja con más ranking que PUEDA jugar en el horario de esta zona.
 */
    private Pareja buscarMejorParejaParaZona(Zona zona, List<Pareja> candidatos) {
        for (Pareja p : candidatos) {
            // Usamos el "Patovica" que definimos antes
            if (!tieneRestriccionParaJugar(p, zona.getDiaDefecto(), zona.getHoraDefecto())) {
                return p; // Encontramos al mejor que puede jugar
            }
        }
        // Si ninguno puede (caso extremo), devolvemos el primero de la lista por defecto
        return candidatos.get(0);
    }
    private void generarPartidosParaZona(Zona zona) {
        List<Pareja> p = zona.getParejas();
        if (p.size() < 3) return; // Por las dudas, si la zona no está completa

        // Usamos el día y la hora que el algoritmo inteligente le asignó a esta zona 
        // (Ej: Si es la Zona A, traerá Sábado 16:00. Si es la Zona X, traerá Viernes 19:00)
        LocalTime horaInicio = zona.getHoraDefecto();
        DiaTorneo diaZona = zona.getDiaDefecto();

        List<Partido> partidos = new ArrayList<>();

        // Partido 1: Pareja 1 vs Pareja 2
        partidos.add(crearPartido(zona, p.get(0), p.get(1), diaZona, horaInicio));

        // Partido 2: Pareja 1 vs Pareja 3 (+1 hora)
        partidos.add(crearPartido(zona, p.get(0), p.get(2), diaZona, horaInicio.plusHours(1)));

        // Partido 3: Pareja 2 vs Pareja 3 (+2 horas)
        partidos.add(crearPartido(zona, p.get(1), p.get(2), diaZona, horaInicio.plusHours(2)));

        zona.setPartidos(partidos);
    }

    // Le agregamos el parámetro DiaTorneo para que ya no diga SABADO de forma fija
    private Partido crearPartido(Zona z, Pareja p1, Pareja p2, DiaTorneo dia, LocalTime hora) {
        return Partido.builder()
                .zona(z)
                .pareja1(p1)
                .pareja2(p2)
                .dia(dia) 
                .hora(hora)
                .estado(EstadoPartido.PENDIENTE)
                .build();
    }
    /**
     * El "Patovica": Devuelve TRUE si la pareja NO PUEDE jugar en ese día y horario.
     * Devuelve FALSE si están libres y pueden pasar a la zona.
     */
    private boolean tieneRestriccionParaJugar(Pareja pareja, DiaTorneo diaZona, LocalTime horaZona) {
        
        List<RestriccionHoraria> restricciones = pareja.getRestricciones(); 
        if (restricciones == null || restricciones.isEmpty()) {
            return false; // No pidieron nada, pueden jugar siempre
        }

        for (RestriccionHoraria rest : restricciones) {
            // Primero chequeamos que la restricción sea para el mismo día de la zona
            if (rest.getDia() == diaZona) {
                // Evaluamos si la hora de la zona cae ADENTRO del bloque que no pueden jugar
                // isBefore = antes | isAfter = después
                if (!horaZona.isBefore(rest.getHoraDesde()) && !horaZona.isAfter(rest.getHoraHasta())) {
                    return true; // Tienen restricción, NO pueden jugar a esta hora
                }
            }
        }
        return false; // Si pasó todos los filtros, está libre
    }
}