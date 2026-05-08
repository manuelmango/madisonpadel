package com.madisonpadel.torneo.config;

import com.madisonpadel.torneo.entities.Categoria;
import com.madisonpadel.torneo.entities.Genero;
import com.madisonpadel.torneo.repositories.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;

    @Override
    public void run(String... args) {
        // Si no hay categorías, las creamos para que el sistema no esté vacío
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
    }
}