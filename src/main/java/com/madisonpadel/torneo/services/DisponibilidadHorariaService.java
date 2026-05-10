package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.entities.DisponibilidadHoraria;
import com.madisonpadel.torneo.entities.DiaTorneo;
import com.madisonpadel.torneo.repositories.DisponibilidadHorariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadHorariaService {

    private final DisponibilidadHorariaRepository repository;

    // Guardar o actualizar la configuración de un día
    @Transactional
    public DisponibilidadHoraria guardarConfiguracion(DisponibilidadHoraria disponibilidad) {
        // Si ya existe configuración para ese día, la actualizamos en vez de duplicarla
        return repository.findByDia(disponibilidad.getDia())
                .map(existente -> {
                    existente.setHoraInicio(disponibilidad.getHoraInicio());
                    existente.setCantidadCanchas(disponibilidad.getCantidadCanchas());
                    existente.setDuracionTurnoMinutos(disponibilidad.getDuracionTurnoMinutos());
                    existente.setHoraFin(disponibilidad.getHoraFin());
                    return repository.save(existente);
                })
                .orElseGet(() -> repository.save(disponibilidad));
    }

    // Ver todos los horarios cargados (para Postman o el Frontend)
    public List<DisponibilidadHoraria> obtenerTodaLaDisponibilidad() {
        return repository.findAll();
    }

    // El método estrella que usará el "Arquitecto" de los Playoffs
    public DisponibilidadHoraria obtenerPorDia(DiaTorneo dia) {
        return repository.findByDia(dia)
                .orElseThrow(() -> new IllegalArgumentException("No hay horarios cargados para el día: " + dia));
    }
}