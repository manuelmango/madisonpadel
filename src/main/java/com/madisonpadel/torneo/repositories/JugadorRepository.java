package com.madisonpadel.torneo.repositories;

import com.madisonpadel.torneo.entities.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    
    // Método personalizado: Spring lee el nombre "findByDni" y arma el SQL automáticamente.
    Optional<Jugador> findByDni(String dni);
}