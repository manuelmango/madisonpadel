package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.entities.enums.EstadoPartido;
import com.madisonpadel.torneo.entities.enums.FasePartido;
import com.madisonpadel.torneo.dtos.ConfiguracionTorneoDTO;
import com.madisonpadel.torneo.dtos.PosicionZonaDTO;
import com.madisonpadel.torneo.entities.DiaTorneo;
import com.madisonpadel.torneo.entities.DisponibilidadHoraria;
import com.madisonpadel.torneo.entities.Pareja;
import com.madisonpadel.torneo.entities.Partido;
import com.madisonpadel.torneo.entities.Zona;
import com.madisonpadel.torneo.repositories.ParejaRepository;
import com.madisonpadel.torneo.repositories.PartidoRepository;
import com.madisonpadel.torneo.repositories.ZonaRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
@Service
@RequiredArgsConstructor
public class PlayoffService {
    private final PartidoRepository partidoRepository;
    private final PlanificadorHorarioService planificadorHorarioService;
    private final ParejaRepository parejaRepository;
    private final ZonaRepository zonaRepository;

    private void acomodarParejasEnCuadro(List<Partido> primeraRonda, List<Pareja> clasificados, int byes) {
        int totalPartidos = primeraRonda.size();
    
        // 1. Generamos el orden correcto según el tamaño de la llave (Evita que se pisen)
        int[] ordenSiembra;
        if (totalPartidos == 8) {
            ordenSiembra = new int[]{0, 7, 4, 3, 2, 5, 6, 1}; // Distribución para Octavos
        } else if (totalPartidos == 4) {
            ordenSiembra = new int[]{0, 3, 1, 2}; // Distribución para Cuartos
        } else if (totalPartidos == 2) {
            ordenSiembra = new int[]{0, 1}; // Distribución para Semis
        } else {
            ordenSiembra = new int[]{0}; // Final
        }
        int indiceClasificados = 0;
       
        // 1. Sentamos a los privilegiados (Byes) directamente en la SEGUNDA ronda
        for (int i = 0; i < byes; i++) {
            Pareja privilegiada = clasificados.get(indiceClasificados++);
            // Elegimos de qué lado del árbol sacar el partido
            int indicePartido = ordenSiembra[i];
            
            Partido partidoBye = primeraRonda.get(indicePartido);
            Partido siguiente = partidoBye.getSiguientePartido();

            if (siguiente.getPareja1() == null){
                siguiente.setPareja1(privilegiada);
            } 
            else{
                siguiente.setPareja2(privilegiada);
            } 
            partidoBye.setEstado(EstadoPartido.FINALIZADO); // El Bye no se juega
            partidoRepository.save(siguiente);
            partidoRepository.save(partidoBye);
        }

        // 2. El resto juega la primera ronda
        int peorClasificado = clasificados.size() - 1;
        for (int i = byes; i < totalPartidos; i++) {
            int indicePartido = ordenSiembra[i];
            Partido partido = primeraRonda.get(indicePartido);
            if(partido.getEstado() == EstadoPartido.FINALIZADO){
                continue;
            }
            if (indiceClasificados <= peorClasificado){
                partido.setPareja1(clasificados.get(indiceClasificados++));
            } 
            if(indiceClasificados <= peorClasificado){
                partido.setPareja2(clasificados.get(peorClasificado--));
            }
            partidoRepository.save(partido);
        }
    }
    private int calcularSiguientePotenciaDeDos(int n) {
        int p = 1;
        while (p < n) p *= 2;
        return p;
    }

    private FasePartido determinarFase(int n) {
        return switch (n) {
            case 1 -> FasePartido.FINAL;
            case 2 -> FasePartido.SEMIFINAL;
            case 4 -> FasePartido.CUARTOS;
            case 8 -> FasePartido.OCTAVOS;
            default -> FasePartido.ZONA;
        };
    }
    private List<Partido> construirEsqueletoPlayoffs(int tamañoCuadro) {
        // Creamos la Final
        Partido finalTorneo = partidoRepository.save(Partido.builder()
                .fase(FasePartido.FINAL).estado(EstadoPartido.PENDIENTE).build());

        List<Partido> rondaActual = List.of(finalTorneo);
        int partidosEnRonda = 1;

        // Vamos creando hacia atrás: Final -> Semis -> Cuartos...
        while (partidosEnRonda < tamañoCuadro / 2) {
            partidosEnRonda *= 2;
            FasePartido fase = determinarFase(partidosEnRonda);
            List<Partido> rondaAnterior = new ArrayList<>();

            for (Partido destino : rondaActual) {
                Partido p1 = partidoRepository.save(Partido.builder().fase(fase).siguientePartido(destino).estado(EstadoPartido.PENDIENTE).build());
                Partido p2 = partidoRepository.save(Partido.builder().fase(fase).siguientePartido(destino).estado(EstadoPartido.PENDIENTE).build());
                rondaAnterior.add(p1);
                rondaAnterior.add(p2);
            }
            rondaActual = rondaAnterior;
        }
        return rondaActual; // Devuelve los partidos de la primera fase (ej: Octavos)
    }
    @Transactional
    public void generarPlayoffsDomingo(ConfiguracionTorneoDTO config) {
        // 1. RECOLECCIÓN: Traemos a todos los que clasificaron (1ros, 2dos y 3ros si hay GSL)
        List<Zona> zonas = zonaRepository.findAll();
        List<Pareja> clasificados = obtenerClasificadosOrdenados(zonas);

        int totalClasificados = clasificados.size();
        if (totalClasificados < 2) throw new IllegalStateException("No hay suficientes parejas.");

        // 2. MATEMÁTICA: ¿De qué tamaño es el cuadro? (8, 16, 32...)
        int tamañoCuadro = calcularSiguientePotenciaDeDos(totalClasificados);
        int cantidadByes = tamañoCuadro - totalClasificados;

        // 3. ESQUELETO: Creamos los partidos vacíos enlazados
        List<Partido> primeraRonda = construirEsqueletoPlayoffs(tamañoCuadro);

        // 4. SIEMBRA: Acomodamos a los clasificados
        acomodarParejasEnCuadro(primeraRonda, clasificados, cantidadByes);

        // 5. HORARIOS: Usamos el planificador que ya tenés para poner orden al domingo
        // (Podemos pasarle solo los partidos de playoff para que los acomode en las 2 canchas)
        List<Partido> todosLosPlayoffs = partidoRepository.findByFaseNot(FasePartido.ZONA);
        planificadorHorarioService.planificarDomingo(todosLosPlayoffs, config);
        System.out.println("¡Cuadro de " + tamañoCuadro + " generado con " + cantidadByes + " Byes!");
    }

    private List<Pareja> obtenerClasificadosOrdenados(List<Zona> zonas) {
        List<Pareja> primeros = new ArrayList<>();
        List<Pareja> segundos = new ArrayList<>();
        List<Pareja> terceros = new ArrayList<>();

        for (Zona zona : zonas) {
            // Tu método obtenerParejaPorPosicionEnZona ya es perfecto, lo seguimos usando
            Pareja p1 = obtenerParejaPorPosicionEnZona(zona.getNombre(), 1);
            Pareja p2 = obtenerParejaPorPosicionEnZona(zona.getNombre(), 2);
            Pareja p3 = obtenerParejaPorPosicionEnZona(zona.getNombre(), 3);

            if (p1 != null) primeros.add(p1);
            if (p2 != null) segundos.add(p2);
            if (p3 != null) terceros.add(p3);
        }

        List<Pareja> merito = new ArrayList<>();
        merito.addAll(primeros);
        merito.addAll(segundos);
        merito.addAll(terceros);
        return merito;
    }
    private Pareja obtenerParejaPorPosicionEnZona(String nombreZona, int posicionBuscada) {
        
        List<Partido> partidosZona = partidoRepository.findByZonaNombreAndFaseAndEstado(nombreZona, FasePartido.ZONA, EstadoPartido.FINALIZADO);
        if(partidosZona.isEmpty()) return null;

        Partido partidoGanadores = partidosZona.stream()
                .filter(p -> "Ganador P1".equals(p.getOrigenPareja1()))
                .findFirst()
                .orElse(null);

        // Si existe el partido de ganadores, es una zona de 4 y resolvemos por acá:
        if (partidoGanadores != null) {
            if(partidoGanadores.getGanador() == null|| partidoGanadores.getPareja1() == null || partidoGanadores.getPareja2() == null){
                return null;
            }
            
            if (posicionBuscada == 1) {
                // El 1ro es directamente el ganador del Partido 3
                return partidoGanadores.getGanador();
            } 
            
            if (posicionBuscada == 2) {
                // El 2do es el que jugó el Partido 3, pero NO ganó (el perdedor)
                return partidoGanadores.getGanador().getId().equals(partidoGanadores.getPareja1().getId()) 
                        ? partidoGanadores.getPareja2() 
                        : partidoGanadores.getPareja1();
            }

            if (posicionBuscada == 3) {
                // El 3ro es el ganador del Partido 4
                Partido partidoPerdedores = partidosZona.stream()
                        .filter(p -> "Perdedor P1".equals(p.getOrigenPareja1()))
                        .findFirst()
                        .orElse(null);
                return partidoPerdedores != null ? partidoPerdedores.getGanador() : null;
            }

            // Si piden el 4to (eliminado) o una posición inválida
            return null; 
        }
        
        Map<Long, PosicionZonaDTO> statsMap = new HashMap<>();

        for (Partido p : partidosZona) {
            // Inicializamos P1
            statsMap.putIfAbsent(p.getPareja1().getId(), new PosicionZonaDTO());
            PosicionZonaDTO dto1 = statsMap.get(p.getPareja1().getId());
            dto1.setParejaId(p.getPareja1().getId());

            // Inicializamos P2
            statsMap.putIfAbsent(p.getPareja2().getId(), new PosicionZonaDTO());
            PosicionZonaDTO dto2 = statsMap.get(p.getPareja2().getId());
            dto2.setParejaId(p.getPareja2().getId());

            // Sumamos Partidos Jugados y Ganados
            dto1.setPartidosJugados(dto1.getPartidosJugados() + 1);
            dto2.setPartidosJugados(dto2.getPartidosJugados() + 1);

            if (p.getGanador() != null) {
                if (p.getGanador().getId().equals(p.getPareja1().getId())) dto1.setPartidosGanados(dto1.getPartidosGanados() + 1);
                if (p.getGanador().getId().equals(p.getPareja2().getId())) dto2.setPartidosGanados(dto2.getPartidosGanados() + 1);
            }

            // Sumamos Sets
            dto1.setSetsAFavor(dto1.getSetsAFavor() + p.getSetsPareja1());
            dto1.setSetsEnContra(dto1.getSetsEnContra() + p.getSetsPareja2());
            dto2.setSetsAFavor(dto2.getSetsAFavor() + p.getSetsPareja2());
            dto2.setSetsEnContra(dto2.getSetsEnContra() + p.getSetsPareja1());

            // Sumamos Games
            dto1.setGamesAFavor(dto1.getGamesAFavor() + p.getGamesPareja1());
            dto1.setGamesEnContra(dto1.getGamesEnContra() + p.getGamesPareja2());
            dto2.setGamesAFavor(dto2.getGamesAFavor() + p.getGamesPareja2());
            dto2.setGamesEnContra(dto2.getGamesEnContra() + p.getGamesPareja1());
        }

        // Convertimos a lista y dejamos que tu DTO haga la magia del ordenamiento
        List<PosicionZonaDTO> tablaPosiciones = new ArrayList<>(statsMap.values());
        Collections.sort(tablaPosiciones); // ¡Usa tu compareTo!

        if(tablaPosiciones.size() == 3 && posicionBuscada == 3){
            return null;
        }
        // Devolvemos la pareja solicitada
        if (tablaPosiciones.size() >= posicionBuscada) {
            Long idParejaClasificada = tablaPosiciones.get(posicionBuscada - 1).getParejaId();
            // Necesitamos ParejaRepository inyectado arriba para esto:
            return parejaRepository.findById(idParejaClasificada).orElse(null); 
        }

        return null; 
    }
}