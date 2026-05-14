package com.madisonpadel.torneo.repositories;

import com.madisonpadel.torneo.entities.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ZonaRepository extends JpaRepository<Zona, Long> {
    List<Zona> findByCategoriaId(Long categoriaId);
}