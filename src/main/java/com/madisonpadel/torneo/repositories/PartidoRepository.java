package com.madisonpadel.torneo.repositories;

import com.madisonpadel.torneo.entities.Partido;
import com.madisonpadel.torneo.entities.enums.EstadoPartido;
import com.madisonpadel.torneo.entities.enums.FasePartido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {
    // Spring Boot lee el nombre del método y hace el "SELECT * FROM partidos WHERE zona_id = ?" solo
    List<Partido> findByZonaId(Long zonaId);
    List<Partido> findByFaseAndEstado(FasePartido fase, EstadoPartido estado);
    // AGREGÁ ESTA LÍNEA NUEVA para la Calculadora:
    List<Partido> findByZonaNombreAndFaseAndEstado(String nombreZona, FasePartido fase, EstadoPartido estado);
    List<Partido> findByEstado(EstadoPartido estado);

}