package com.madisonpadel.torneo.services;
import java.util.List;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import com.madisonpadel.torneo.dtos.ConfiguracionTorneoDTO;
import com.madisonpadel.torneo.entities.Zona;
import com.madisonpadel.torneo.entities.DiaTorneo;
import com.madisonpadel.torneo.entities.Partido;

@Service
public class PlanificadorHorarioService {

    public void planificarZonas(List<Zona> zonas, ConfiguracionTorneoDTO config) {
        // Punteros para saber en qué hora va cada cancha por día
        LocalTime proximoTurnoViernesC1 = config.getInicioViernes();
        LocalTime proximoTurnoViernesC2 = config.getInicioViernes();
        
        LocalTime proximoTurnoSabadoC1 = config.getInicioSabado();
        LocalTime proximoTurnoSabadoC2 = config.getInicioSabado();

        for (Zona zona : zonas) {
            int horasNecesarias = (zona.getParejas().size() == 4) ? 4 : 3;
            boolean asignada = false;

            // 1. Intentamos en VIERNES
            if (zona.getDiaDefecto() == DiaTorneo.VIERNES) {
                if (hayEspacio(proximoTurnoViernesC1, horasNecesarias, config.getFinViernes())) {
                    asignarZona(zona, DiaTorneo.VIERNES, proximoTurnoViernesC1, 1);
                    proximoTurnoViernesC1 = proximoTurnoViernesC1.plusHours(horasNecesarias);
                    asignada = true;
                } else if (hayEspacio(proximoTurnoViernesC2, horasNecesarias, config.getFinViernes())) {
                    asignarZona(zona, DiaTorneo.VIERNES, proximoTurnoViernesC2, 2);
                    proximoTurnoViernesC2 = proximoTurnoViernesC2.plusHours(horasNecesarias);
                    asignada = true;
                }
            }

            // 2. Intentamos en SÁBADO (si no entró el viernes)
            else if (zona.getDiaDefecto() == DiaTorneo.SABADO) {
                if (hayEspacio(proximoTurnoSabadoC1, horasNecesarias, config.getFinSabado())) {
                    asignarZona(zona, DiaTorneo.SABADO, proximoTurnoSabadoC1, 1);
                    proximoTurnoSabadoC1 = proximoTurnoSabadoC1.plusHours(horasNecesarias);
                    asignada = true;
                } else if (hayEspacio(proximoTurnoSabadoC2, horasNecesarias, config.getFinSabado())) {
                    asignarZona(zona, DiaTorneo.SABADO, proximoTurnoSabadoC2, 2);
                    proximoTurnoSabadoC2 = proximoTurnoSabadoC2.plusHours(horasNecesarias);
                    asignada = true;
                }
            }

            if (!asignada) {
                throw new IllegalStateException("¡Atención! No hay espacio suficiente en el club para la " + zona.getNombre());
            }
        }
    }
    public void planificarDomingo(List<Partido> partidosPlayoff, ConfiguracionTorneoDTO config) {
        LocalTime proximoTurnoC1 = config.getInicioDomingo();
        LocalTime proximoTurnoC2 = config.getInicioDomingo();

        // Ordenamos los partidos para que se jueguen primero los de rondas previas (Octavos, luego Cuartos, etc.)
        // Asumiendo que FasePartido se puede ordenar o vienen en orden de creación.
        for (Partido partido : partidosPlayoff) {
            // Intercalamos: El que tenga el horario más temprano disponible, se lleva el partido
            if (proximoTurnoC1.isBefore(proximoTurnoC2) || proximoTurnoC1.equals(proximoTurnoC2)) {
                partido.setDia(DiaTorneo.DOMINGO);
                partido.setHora(proximoTurnoC1);
                partido.setNumeroCancha(1);
                proximoTurnoC1 = proximoTurnoC1.plusHours(1); // 1 hora de duración por partido
            } else {
                partido.setDia(DiaTorneo.DOMINGO);
                partido.setHora(proximoTurnoC2);
                partido.setNumeroCancha(2);
                proximoTurnoC2 = proximoTurnoC2.plusHours(1);
            }
        }
    }
    private boolean hayEspacio(LocalTime inicio, int horas, LocalTime fin) {
        // Calculamos a qué hora le tocaría arrancar al ÚLTIMO partido de la zona
        // Ej: Si inicio es 21:00 y dura 3 horas -> el último arranca a las 21 + (3 - 1) = 23:00
        LocalTime horaUltimoPartido = inicio.plusHours(horas - 1);
        // Trampa de medianoche: Si el último partido arranca al día siguiente (dio la vuelta al reloj)
        if (horaUltimoPartido.isBefore(inicio)) {
            return false; 
        }
        // Verificamos si la hora de ese último partido es válida (menor o igual al "fin")
        return !horaUltimoPartido.isAfter(fin);
    }
    private void asignarZona(Zona zona, DiaTorneo dia, LocalTime hora, Integer cancha) {
        zona.setDiaDefecto(dia);
        zona.setHoraDefecto(hora);
        // Aquí podrías agregar zona.setCanchaDefecto(cancha) si creaste el campo en Zona
        
        // También actualizamos los partidos de la zona de una vez
        LocalTime relojPartido = hora;
        for (Partido p : zona.getPartidos()) {
            p.setDia(dia);
            p.setHora(relojPartido);
            p.setNumeroCancha(cancha);
            relojPartido = relojPartido.plusHours(1);
        }
    }
}