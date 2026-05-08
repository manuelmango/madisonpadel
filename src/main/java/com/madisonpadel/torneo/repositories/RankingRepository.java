package com.madisonpadel.torneo.repositories;

import com.madisonpadel.torneo.entities.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {
    
    // Esto lo vamos a usar para preguntar: "¿Este jugador ya tiene puntos en esta categoría?"
    Optional<Ranking> findByJugadorIdAndCategoriaId(Long jugadorId, Long categoriaId);

    // Esto es oro puro: Te trae la tabla de posiciones de una categoría, 
    // y con "OrderByPuntosDesc" ya te la trae ordenada de mayor a menor. ¡Adiós SQL complejo!
    List<Ranking> findByCategoriaIdOrderByPuntosDesc(Long categoriaId);
}