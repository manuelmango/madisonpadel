package com.madisonpadel.torneo.repositories;

import com.madisonpadel.torneo.entities.DisponibilidadHoraria;
import com.madisonpadel.torneo.entities.DiaTorneo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DisponibilidadHorariaRepository extends JpaRepository<DisponibilidadHoraria, Long> {
    // Buscamos la configuración de un día específico (ej: "SABADO")
    Optional<DisponibilidadHoraria> findByDia(DiaTorneo dia);
}