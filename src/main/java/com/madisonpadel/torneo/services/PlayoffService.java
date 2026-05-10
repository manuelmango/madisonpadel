package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.entities.enums.EstadoPartido;
import com.madisonpadel.torneo.entities.enums.FasePartido;
import com.madisonpadel.torneo.dtos.PosicionZonaDTO;
import com.madisonpadel.torneo.entities.DiaTorneo;
import com.madisonpadel.torneo.entities.DisponibilidadHoraria;
import com.madisonpadel.torneo.entities.Pareja;
import com.madisonpadel.torneo.entities.Partido;
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
    private final DisponibilidadHorariaService disponibilidadHorariaService;
    private final ParejaRepository parejaRepository;
    private final ZonaRepository zonaRepository;



    @Transactional
    public void iniciarDomingoDePlayoffs() {
        // 1. Traer la logística del club
        DisponibilidadHoraria dispDomingo = disponibilidadHorariaService.obtenerPorDia(DiaTorneo.DOMINGO);
        
        // 2. Buscar los partidos vacíos (Octavos, Dieciseisavos, etc.) que estén PENDIENTES
        // Traemos todos los que no tengan parejas asignadas aún
        List<Partido> partidosAProgramar = partidoRepository.findByEstado(EstadoPartido.PENDIENTE)
                .stream()
                .filter(p -> p.getFase() != FasePartido.ZONA) // Que no sean de grupos
                .filter(p -> p.getPareja1() == null) // Que estén vacíos
                .toList();

        // 3. Contar zonas para el traductor (Necesitás inyectar zonaRepository arriba)
        int cantidadZonas = (int) zonaRepository.count(); 

        LocalTime horaActualTurno = dispDomingo.getHoraInicio();
        int canchaActual = 1;

        for (Partido partido : partidosAProgramar) {
            
            // --- LA CONEXIÓN MAESTRA ---
            // Buscamos quiénes son las parejas reales para los textos "Clasificado 1", "Clasificado 2", etc.
            Pareja p1 = buscarParejaPorOrigen(partido.getOrigenPareja1(), cantidadZonas);
            Pareja p2 = buscarParejaPorOrigen(partido.getOrigenPareja2(), cantidadZonas);
            
            // Solo programamos si encontramos a ambas parejas (o si no es un BYE)
            if (p1 != null && p2 != null) {
                partido.setPareja1(p1);
                partido.setPareja2(p2);
                partido.setNumeroCancha(canchaActual);
                partido.setHora(horaActualTurno);
                partido.setDia(DiaTorneo.DOMINGO);
                partido.setEstado(EstadoPartido.PENDIENTE);

                // Lógica de canchas y turnos
                canchaActual++;
                if (canchaActual > dispDomingo.getCantidadCanchas()) {
                    canchaActual = 1;
                    horaActualTurno = horaActualTurno.plusMinutes(dispDomingo.getDuracionTurnoMinutos());
                }
                partidoRepository.save(partido);
            }
        }
        System.out.println("¡Playoffs del domingo vinculados con éxito!");
    }
    private Pareja buscarParejaPorOrigen(String origen, int cantidadZonas) {
        // Si el origen es nulo o no es un clasificado de zona, no buscamos nada
        if (origen == null || origen.equals("Ganador") || origen.equals("Bye")) return null;

        // 1. Usamos el método de las letras (A, B, C...)
        String origenTraducido = traducirJerarquia(origen, cantidadZonas);

        // 2. Si el traductor nos dice que viene de una zona (ej: "1ro Zona A")
        if (origenTraducido.contains("Zona")) {
            int posicionBuscada = origenTraducido.startsWith("1ro") ? 1 : 2;
            
            // Extraemos la letra de la zona (la última letra del String)
            String nombreZona = origenTraducido.substring(origenTraducido.length() - 1);

            // 3. Llamamos a la calculadora que usa tu PosicionZonaDTO
            return obtenerParejaPorPosicionEnZona(nombreZona, posicionBuscada);
        }
        
        return null;
    }
@Transactional
    public void generarCuadroDinamico(int cantidadParejas) {
        int techo = calcularTechoCuadro(cantidadParejas);
        int byes = calcularPrivilegiados(cantidadParejas, techo);
        int partidosPrimeraRonda = (cantidadParejas - byes) / 2;

        System.out.println("--- MOTOR DINÁMICO MADISON PADEL ---");
        System.out.println("Parejas: " + cantidadParejas + " | Techo: " + techo);
        System.out.println("Privilegiados (Byes): " + byes + " | Partidos Previa: " + partidosPrimeraRonda);

        // 1. La semilla: La Gran Final
        Partido finalTorneo = partidoRepository.save(Partido.builder()
                .fase(FasePartido.FINAL)
                .estado(EstadoPartido.PENDIENTE)
                .build());

        List<Partido> rondaActual = List.of(finalTorneo);
        FasePartido faseActual = FasePartido.FINAL;

        // 2. Construimos el esqueleto principal hasta la "Sala de Espera" (techo / 4 partidos)
        // Si el techo es 16, corta cuando arma los 4 partidos de Cuartos.
        int salaDeEspera = techo / 4; 
        
        while (rondaActual.size() < salaDeEspera) {
            faseActual = faseActual.faseAnterior();
            List<Partido> rondaAnterior = new ArrayList<>();

            for (Partido partidoDestino : rondaActual) {
                Partido p1 = partidoRepository.save(Partido.builder().fase(faseActual).siguientePartido(partidoDestino).estado(EstadoPartido.PENDIENTE).build());
                Partido p2 = partidoRepository.save(Partido.builder().fase(faseActual).siguientePartido(partidoDestino).estado(EstadoPartido.PENDIENTE).build());
                rondaAnterior.add(p1);
                rondaAnterior.add(p2);
            }
            rondaActual = rondaAnterior; 
        }

        // 3. LA TIJERA: Repartir Byes y armar la Primera Ronda
        FasePartido fasePrevia = faseActual.faseAnterior(); // Ej: Si estamos en Cuartos, la previa es Octavos
        int byesAsignados = 0;
        int partidosPreviosCreados = 0;
        int numeroClasificado = 1; // Para etiquetar: "Clasificado 1", "Clasificado 2"

        for (Partido partidoSalaEspera : rondaActual) {
            
            // Evaluamos la "Silla 1" de este partido
            if (byesAsignados < byes) {
                partidoSalaEspera.setOrigenPareja1("Clasificado " + numeroClasificado);
                numeroClasificado++;
                byesAsignados++;
            } else if (partidosPreviosCreados < partidosPrimeraRonda) {
                partidoRepository.save(Partido.builder().fase(fasePrevia).siguientePartido(partidoSalaEspera).estado(EstadoPartido.PENDIENTE).build());
                partidosPreviosCreados++;
            }

            // Evaluamos la "Silla 2" de este partido
            if (byesAsignados < byes) {
                partidoSalaEspera.setOrigenPareja2("Clasificado " + numeroClasificado);
                numeroClasificado++;
                byesAsignados++;
            } else if (partidosPreviosCreados < partidosPrimeraRonda) {
                partidoRepository.save(Partido.builder().fase(fasePrevia).siguientePartido(partidoSalaEspera).estado(EstadoPartido.PENDIENTE).build());
                partidosPreviosCreados++;
            }
            
            // Actualizamos el partido de la sala de espera por si le seteamos un "Clasificado"
            partidoRepository.save(partidoSalaEspera);
        }

        System.out.println("¡Cuadro dinámico generado y balanceado perfectamente!");
    }

        private int calcularTechoCuadro(int cantidadParejas) {
            int techo = 2;
            while (techo < cantidadParejas) {
                techo *= 2; // Sube de 2 en 2, 4, 8, 16, 32...
            }
            return techo;
        }

        private int calcularPrivilegiados(int cantidadParejas, int techo) {
            return techo - cantidadParejas;
        }
        /**
     * Traduce el texto "Clasificado X" a su verdadera posición y zona.
     * Ej: "Clasificado 7" en un torneo de 6 zonas devuelve "2do Zona A".
     */
    private String traducirJerarquia(String origen, int cantidadZonas) {
        if (!origen.startsWith("Clasificado ")) {
            return origen; // Por si es "Ganador Semi 1", lo deja igual
        }

        // Sacamos el número (Ej: de "Clasificado 7" nos quedamos con el 7)
        int numeroClasificado = Integer.parseInt(origen.replace("Clasificado ", ""));

        int posicion = 1;
        int indiceZona = numeroClasificado;

        // Si el número es mayor a la cantidad de zonas, significa que estamos con los "2dos"
        if (numeroClasificado > cantidadZonas) {
            posicion = 2;
            indiceZona = numeroClasificado - cantidadZonas; 
            // Ej: 7 - 6 = 1 (Que corresponde a la Zona A)
        }

        // Convertimos el índice numérico a una letra (1 = A, 2 = B, 3 = C...)
        char letraZona = (char) ('A' + (indiceZona - 1));
        String sufijo = (posicion == 1) ? "ro" : "do";

        return posicion + sufijo + " Zona " + letraZona;
    }
    /**
     * Calcula la tabla de posiciones usando tu PosicionZonaDTO y devuelve la pareja.
     */
    private Pareja obtenerParejaPorPosicionEnZona(String nombreZona, int posicionBuscada) {
        
        List<Partido> partidosZona = partidoRepository.findByZonaNombreAndFaseAndEstado(nombreZona, FasePartido.ZONA, EstadoPartido.FINALIZADO);
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

        // Devolvemos la pareja solicitada
        if (tablaPosiciones.size() >= posicionBuscada) {
            Long idParejaClasificada = tablaPosiciones.get(posicionBuscada - 1).getParejaId();
            // Necesitamos ParejaRepository inyectado arriba para esto:
            return parejaRepository.findById(idParejaClasificada).orElse(null); 
        }

        return null; 
    }
}