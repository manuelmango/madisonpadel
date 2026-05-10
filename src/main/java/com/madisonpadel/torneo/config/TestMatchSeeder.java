package com.madisonpadel.torneo.config;

import com.madisonpadel.torneo.entities.*;
import com.madisonpadel.torneo.entities.enums.EstadoPartido;
import com.madisonpadel.torneo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Order(2)
public class TestMatchSeeder implements CommandLineRunner {

    private final ParejaRepository parejaRepository;
    private final ZonaRepository zonaRepository;
    private final PartidoRepository partidoRepository;
    private final CategoriaRepository categoriaRepository;
    private final JugadorRepository jugadorRepository; // ¡Agregamos el repo de Jugadores!

    @Override
    public void run(String... args) {
        
        if (partidoRepository.count() == 0) {
            System.out.println("🌱 [TestSeeder] Creando partido de prueba...");

            List<Categoria> categorias = categoriaRepository.findAll();
            if (categorias.isEmpty()) {
                System.out.println("⚠️ [TestSeeder] No hay categorías. Primero debe correr el DataInitializer.");
                return;
            }
            Categoria categoriaPrueba = categorias.get(0); 

            // 1. CREAMOS 4 JUGADORES
            Jugador j1 = new Jugador();
            j1.setNombre("Alejandro"); // Agregá el set del nombre
            j1.setApellido("Galán");   // Agregá el set del apellido
            j1.setTelefono("1");
            // j1.set... (Cualquier otro dato que no pueda quedar vacío en tu base)
            jugadorRepository.save(j1);

            Jugador j2 = new Jugador();
            j2.setNombre("Agustin");
            j2.setApellido("Tapia");   // Agregá el set del apellido
            j2.setTelefono("2");
            jugadorRepository.save(j2);

            Jugador j3 = new Jugador();
            j3.setNombre("Arturo");
            j3.setApellido("Coello");   // Agregá el set del apellido
            j3.setTelefono("3");
            jugadorRepository.save(j3);
            
            Jugador j4 = new Jugador();
            j4.setNombre("Federico");
            j4.setApellido("Chingotto");   // Agregá el set del apellido
            j4.setTelefono("4");
            jugadorRepository.save(j4);

            // 2. ARMAMOS LAS PAREJAS CON SUS JUGADORES Y CATEGORÍA
            Pareja pareja1 = new Pareja();
            pareja1.setCategoria(categoriaPrueba);
            pareja1.setJugador1(j1); // Asignamos Jugador 1
            pareja1.setJugador2(j2); // Asignamos Jugador 2 (asumiendo que tenés este campo)
            parejaRepository.save(pareja1);

            Pareja pareja2 = new Pareja();
            pareja2.setCategoria(categoriaPrueba);
            pareja2.setJugador1(j3); // Asignamos Jugador 1
            pareja2.setJugador2(j4); // Asignamos Jugador 2
            parejaRepository.save(pareja2);

            // 3. CREAMOS LA ZONA
            Zona zonaA = new Zona();
            zonaA.setNombre("Zona A");
            zonaRepository.save(zonaA);

            // 4. CREAMOS EL PARTIDO
            Partido partidoDePrueba = Partido.builder()
                    .zona(zonaA)
                    .pareja1(pareja1)
                    .pareja2(pareja2)
                    .dia(DiaTorneo.SABADO)
                    .hora(LocalTime.of(18, 0))
                    .estado(EstadoPartido.PENDIENTE)
                    .build();

            partidoRepository.save(partidoDePrueba);

            System.out.println("✅ [TestSeeder] Partido de prueba creado con ID: " + partidoDePrueba.getId());
        }
    }
}