package com.madisonpadel.torneo.repositories;

import com.madisonpadel.torneo.entities.Categoria;
import com.madisonpadel.torneo.entities.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
    // Spring traduce esto a: 
    // SELECT * FROM categorias WHERE nivel = ? AND genero = ?
    // Ideal para buscar "La categoría de 7ma Caballeros"
    Optional<Categoria> findByNivelAndGenero(Integer nivel, Genero genero);

    // Por si en algún momento en la app querés mostrar un filtro: "Solo categorías de Damas"
    List<Categoria> findByGenero(Genero genero);
}