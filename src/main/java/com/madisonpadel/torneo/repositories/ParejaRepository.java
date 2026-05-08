package com.madisonpadel.torneo.repositories;

import com.madisonpadel.torneo.entities.Pareja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParejaRepository extends JpaRepository<Pareja, Long> {
    
    // Trae la lista completa de inscriptos para una categoría (ej: Todos los de 7ma)
    List<Pareja> findByCategoriaId(Long categoriaId);
}