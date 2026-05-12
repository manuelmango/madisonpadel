package com.madisonpadel.torneo.config;

import com.madisonpadel.torneo.entities.*;
import com.madisonpadel.torneo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Order(2) // Corre después del DataInitializer para que ya existan las categorías
public class TournamentSeeder implements CommandLineRunner {

    private final JugadorRepository jugadorRepository;
    private final ParejaRepository parejaRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    public void run(String... args) {
        
        // Solo creamos datos si la base está vacía (evita duplicados al reiniciar)
        if (parejaRepository.count() == 0) {
            System.out.println("🌱 [TournamentSeeder] Generando 15 parejas para probar el Motor...");

            // Buscamos la 7ma Caballeros que creó el DataInitializer
            Categoria cat7ma = categoriaRepository.findAll().stream()
                    .filter(c -> c.getNombre().contains("7ma"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró la categoría de prueba"));

            for (int i = 1; i <= 15; i++) {
                // 1. Creamos dos jugadores por pareja
                Jugador j1 = Jugador.builder()
                        .nombre("Jugador_" + i + "A")
                        .apellido("Test")
                        .telefono("111" + i)
                        .build();
                
                Jugador j2 = Jugador.builder()
                        .nombre("Jugador_" + i + "B")
                        .apellido("Test")
                        .telefono("222" + i)
                        .build();

                jugadorRepository.save(j1);
                jugadorRepository.save(j2);

                // 2. Creamos la pareja y le asignamos ranking descendente
                // Así la Pareja 1 tendrá 1200 puntos (Top 1) y la Pareja 12 tendrá 100 puntos.
                Pareja pareja = Pareja.builder()
                        .jugador1(j1)
                        .jugador2(j2)
                        .categoria(cat7ma)
                        .puntosTotales(100 * (13 - i)) 
                        .restricciones(new ArrayList<>())
                        .build();

                parejaRepository.save(pareja);
            }

            System.out.println("✅ [TournamentSeeder] 12 parejas creadas con éxito. ¡Todo listo para Postman!");
        }
    }
}