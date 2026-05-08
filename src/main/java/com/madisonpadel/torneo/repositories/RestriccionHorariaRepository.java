package com.madisonpadel.torneo.repositories;

import com.madisonpadel.torneo.entities.RestriccionHoraria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestriccionHorariaRepository extends JpaRepository<RestriccionHoraria, Long> {
    
    // Busca todos los "bloqueos" de horario que pidió una pareja
    List<RestriccionHoraria> findByParejaId(Long parejaId);
}