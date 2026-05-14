package com.madisonpadel.torneo.repositories;

import com.madisonpadel.torneo.entities.Torneo;
import com.madisonpadel.torneo.entities.enums.EstadoTorneo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    List<Torneo> findByEstado(EstadoTorneo estado);
}