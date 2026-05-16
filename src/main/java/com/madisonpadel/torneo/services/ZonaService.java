package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.dtos.ConfiguracionTorneoDTO;
import com.madisonpadel.torneo.entities.*;
import com.madisonpadel.torneo.entities.enums.EstadoPartido;
import com.madisonpadel.torneo.entities.enums.FasePartido;
import com.madisonpadel.torneo.repositories.*;
import com.madisonpadel.torneo.services.PlanificadorHorarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ZonaService {


    private final ParejaRepository parejaRepository;
    private final PartidoRepository partidoRepository;
    private final PlanificadorHorarioService planificadorHorarioService;
    private final ZonaRepository zonaRepository;
    private final CategoriaRepository categoriaRepository;

    private List<Zona> crearZonasVacias(int cantidad, Categoria categoria) {
        List<Zona> zonas = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            char letra = (char) ('A' + i);
            zonas.add(Zona.builder()
                    .nombre("Zona " + letra)
                    .categoria(categoria)
                    .parejas(new ArrayList<>())
                    .partidos(new ArrayList<>())
                    .build());
        }
        return zonas;
    }    
    @Transactional
    public void generarZonasPorCategoria(Long categoriaId, ConfiguracionTorneoDTO config) {
        // 1. Traemos a todos ordenados por el Ranking real de Madison Padel
        Categoria categoria = categoriaRepository.findById(categoriaId)
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        List<Pareja> rankingParejas = parejaRepository.findByCategoriaId(categoriaId)
                .stream()
                .sorted(Comparator.comparingInt(Pareja::getPuntosTotales).reversed())
                .toList();

        int totalParejas = rankingParejas.size();
        int cantidadZonas = totalParejas / 3;
        List<Zona> zonas = crearZonasVacias(cantidadZonas, categoria);

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

        // 3. REPARTO DINÁMICO (SOPORTA IMPARES)
        List<Pareja> pendientes = new ArrayList<>(rankingParejas);
        
        // Calculamos el resto usando el módulo (%)
        // Ej: 14 parejas en 4 zonas -> 14 % 4 = 2. (Sobran 2 parejas)
        int zonasCon4 = totalParejas % cantidadZonas; 

        for (int i = 0; i < zonas.size(); i++) {
            Zona zona = zonas.get(i);
            
            // Si sobran 2, a las primeras 2 zonas les ponemos un techo de 4. Al resto, de 3.
            int techoParejas = (i < zonasCon4) ? 4 : 3;

            while (zona.getParejas().size() < techoParejas && !pendientes.isEmpty()) {
                Pareja candidata = buscarMejorParejaParaZona(zona, pendientes);
                zona.getParejas().add(candidata);
                pendientes.remove(candidata);
            }
        }
        // 4. GENERAR PARTIDOS Y GUARDAR
        for (Zona z : zonas) {
            generarPartidosParaZona(z);
        }
        planificadorHorarioService.planificarZonas(zonas, config);

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
        if (p.size() < 3) return;

        // --- LO IMPORTANTE: Mantenemos tu lógica de horarios y días ---
        LocalTime horaInicio = zona.getHoraDefecto();
        DiaTorneo diaZona = zona.getDiaDefecto();
        List<Partido> partidos = new ArrayList<>();

        if (p.size() == 3) {
            // --- FORMATO CLÁSICO (Tu código original) ---
            partidos.add(crearPartido(zona, p.get(0), p.get(1), diaZona, horaInicio));
            partidos.add(crearPartido(zona, p.get(0), p.get(2), diaZona, horaInicio.plusHours(1)));
            partidos.add(crearPartido(zona, p.get(1), p.get(2), diaZona, horaInicio.plusHours(2)));

        } else if (p.size() == 4) {
            // --- FORMATO GSL (Mini-Playoff) ---
            
            // 1. Primero creamos los partidos de "FUTURO" (vacíos) para tener sus IDs
            // Partido 3: Ganadores (se juega +2 horas después del inicio)
            Partido p3Ganadores = crearPartido(zona, null, null, diaZona, horaInicio.plusHours(2));
            p3Ganadores.setOrigenPareja1("Ganador P1");
            p3Ganadores.setOrigenPareja2("Ganador P2");
            partidoRepository.save(p3Ganadores);

            // Partido 4: Perdedores (se juega +3 horas después del inicio)
            Partido p4Perdedores = crearPartido(zona, null, null, diaZona, horaInicio.plusHours(3));
            p4Perdedores.setOrigenPareja1("Perdedor P1");
            p4Perdedores.setOrigenPareja2("Perdedor P2");
            partidoRepository.save(p4Perdedores);

            // 2. Ahora creamos los partidos de "PRESENTE" (con parejas) y atamos las flechas
            // Partido 1: Pareja 1 vs Pareja 2 (Hora inicio)
            Partido p1 = crearPartido(zona, p.get(0), p.get(1), diaZona, horaInicio);
            p1.setSiguientePartido(p3Ganadores);
            p1.setSiguientePartidoPerdedor(p4Perdedores);
            partidos.add(p1);

            // Partido 2: Pareja 3 vs Pareja 4 (+1 hora)
            Partido p2 = crearPartido(zona, p.get(2), p.get(3), diaZona, horaInicio.plusHours(1));
            p2.setSiguientePartido(p3Ganadores);
            p2.setSiguientePartidoPerdedor(p4Perdedores);
            partidos.add(p2);

            // Agregamos también los partidos vacíos a la lista de la zona
            partidos.add(p3Ganadores);
            partidos.add(p4Perdedores);
        }

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
                .fase(FasePartido.ZONA)
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