package com.madisonpadel.torneo.config;

import com.madisonpadel.torneo.entities.Categoria;
import com.madisonpadel.torneo.entities.DisponibilidadHoraria;
import com.madisonpadel.torneo.entities.DiaTorneo;
import com.madisonpadel.torneo.entities.Genero;
import com.madisonpadel.torneo.repositories.CategoriaRepository;
import com.madisonpadel.torneo.repositories.DisponibilidadHorariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.madisonpadel.torneo.entities.Torneo;
import com.madisonpadel.torneo.entities.enums.EstadoTorneo;
import com.madisonpadel.torneo.repositories.TorneoRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Order(1)
public class DataInitializer implements CommandLineRunner {
    private final TorneoRepository torneoRepository;
    private final CategoriaRepository categoriaRepository;
    // 1. Agregamos el repositorio de Disponibilidad
    private final DisponibilidadHorariaRepository disponibilidadHorariaRepository; 

    @Override
    public void run(String... args) {
        
        // --- INICIALIZAR CATEGORÍAS ---
        if (categoriaRepository.count() == 0) {
            List<Categoria> categoriasIniciales = List.of(
                    // CABALLEROS
                    Categoria.builder().nombre("2da Caballeros").nivel(2).genero(Genero.MASCULINO).build(),
                    Categoria.builder().nombre("3ra Caballeros").nivel(3).genero(Genero.MASCULINO).build(),
                    Categoria.builder().nombre("4ta Caballeros").nivel(4).genero(Genero.MASCULINO).build(),
                    Categoria.builder().nombre("5ta Caballeros").nivel(5).genero(Genero.MASCULINO).build(),
                    Categoria.builder().nombre("6ta Caballeros").nivel(6).genero(Genero.MASCULINO).build(),
                    Categoria.builder().nombre("7ma Caballeros").nivel(7).genero(Genero.MASCULINO).build(),
                    Categoria.builder().nombre("8va Caballeros").nivel(8).genero(Genero.MASCULINO).build(),

                    // DAMAS
                    Categoria.builder().nombre("4ta Damas").nivel(4).genero(Genero.FEMENINO).build(),
                    Categoria.builder().nombre("5ta Damas").nivel(5).genero(Genero.FEMENINO).build(),
                    Categoria.builder().nombre("6ta Damas").nivel(6).genero(Genero.FEMENINO).build(),
                    Categoria.builder().nombre("7ma Damas").nivel(7).genero(Genero.FEMENINO).build(),
                    Categoria.builder().nombre("8va Damas").nivel(8).genero(Genero.FEMENINO).build()
                );
            categoriaRepository.saveAll(categoriasIniciales);
            System.out.println(">> Base de datos inicializada: Categorías creadas.");
        }
            // Al final del bloque de categorías, ANTES del bloque de horarios, agregar:
        if (torneoRepository.count() == 0) {
        // Creamos un torneo de prueba
            Torneo torneoDemo = Torneo.builder()
                .nombre("Torneo Mayo 2026")
                .fechaInicio(LocalDate.of(2026, 5, 15))
                .fechaFin(LocalDate.of(2026, 5, 17))
                .estado(EstadoTorneo.EN_JUEGO)
                .build();
            torneoRepository.save(torneoDemo);

            // Buscamos las categorías ya creadas y las vinculamos al torneo
            categoriaRepository.findAll().forEach(cat -> {
                cat.setTorneo(torneoDemo);
                categoriaRepository.save(cat);
        });
        System.out.println(">> Torneo de prueba creado y categorías vinculadas.");
        }
        // --- INICIALIZAR DISPONIBILIDAD HORARIA DEL CLUB ---
        if (disponibilidadHorariaRepository.count() == 0) {
            List<DisponibilidadHoraria> horariosClub = List.of(
                // Viernes: Suelen arrancar a la tarde/noche
                DisponibilidadHoraria.builder()
                    .dia(DiaTorneo.VIERNES)
                    .horaInicio(LocalTime.of(18, 0))
                    .horaFin(LocalTime.of(23, 59))
                    .cantidadCanchas(2) // Modificá esto con las canchas reales de Madison Padel
                    .duracionTurnoMinutos(60) // Turnos de 1 hora
                    .build(),

                // Sábado: Todo el día a full
                DisponibilidadHoraria.builder()
                    .dia(DiaTorneo.SABADO)
                    .horaInicio(LocalTime.of(9, 0))
                    .horaFin(LocalTime.of(23, 59))
                    .cantidadCanchas(2)
                    .duracionTurnoMinutos(60)
                    .build(),

                // Domingo: Playoffs
                DisponibilidadHoraria.builder()
                    .dia(DiaTorneo.DOMINGO)
                    .horaInicio(LocalTime.of(10, 0)) // Domingo arranca un poquito más tarde
                    .horaFin(LocalTime.of(22, 0))
                    .cantidadCanchas(2)
                    .duracionTurnoMinutos(60)
                    .build()
            );

            disponibilidadHorariaRepository.saveAll(horariosClub);
            System.out.println(">> Base de datos inicializada: Horarios del club configurados.");
        }
   
    }
}