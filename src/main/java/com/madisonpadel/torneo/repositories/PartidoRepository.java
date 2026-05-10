package com.madisonpadel.torneo.repositories;

import com.madisonpadel.torneo.entities.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {
    // Spring Boot lee el nombre del método y hace el "SELECT * FROM partidos WHERE zona_id = ?" solo
    List<Partido> findByZonaId(Long zonaId);

}