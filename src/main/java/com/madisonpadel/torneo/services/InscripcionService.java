package com.madisonpadel.torneo.services;

import com.madisonpadel.torneo.entities.Jugador;
import com.madisonpadel.torneo.entities.Categoria;
import com.madisonpadel.torneo.entities.Genero;
import com.madisonpadel.torneo.repositories.JugadorRepository;
import com.madisonpadel.torneo.repositories.CategoriaRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // Magia de Lombok: Inyecta los repositorios automáticamente
public class InscripcionService {

    private static final int OFFSET_NIVEL_DAMAS_EN_CABALLEROS = 2;

    // Traemos a los "empleados" que buscan en la base de datos
    private final JugadorRepository jugadorRepository;
    private final CategoriaRepository categoriaRepository;

    /**
     * Este es el método que va a llamar el Controller.
     * Recibe los datos crudos, busca en la BD y luego valida.
     */
    public String procesarInscripcion(String dni1, String dni2, Long categoriaId) {
        
        // 1. Buscamos la categoría una sola vez
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("La categoría no existe."));

        // 2. Buscamos y validamos al Jugador 1
        Jugador j1 = jugadorRepository.findByTelefono(dni1)
                .orElseThrow(() -> new IllegalArgumentException("El jugador con DNI " + dni1 + " no existe."));
        validarNivelParaInscripcion(j1, categoria);

        // 3. Buscamos y validamos al Jugador 2
        Jugador j2 = jugadorRepository.findByTelefono(dni2)
                .orElseThrow(() -> new IllegalArgumentException("El jugador con DNI " + dni2 + " no existe."));
        validarNivelParaInscripcion(j2, categoria);

        // 4. (Opcional) Podrías validar que no sea la misma persona
        if (dni1.equals(dni2)) {
            throw new IllegalArgumentException("Error: Una pareja no puede estar formada por el mismo jugador.");
        }

        return "¡Inscripción válida! " + j1.getNombre() + " y " + j2.getNombre() + 
            " están habilitados para jugar en " + categoria.getNombre();
    }

    // --- Acá abajo queda intacto el método matemático que ya habías hecho ---

    public void validarNivelParaInscripcion(Jugador jugador, Categoria torneoCategoria) {
        // 1. Filtro de Género: Caballeros no entran en Damas.
        if (torneoCategoria.getGenero() == Genero.FEMENINO && jugador.getGenero() == Genero.MASCULINO) {
            throw new IllegalArgumentException("Error: El jugador " + jugador.getNombre() + 
                " (Masc) no puede participar en categorías de Damas.");
        }

        int nivelEvaluado = jugador.getCategoriaBase();
        
        // 2. Aplicar OFFSET: Si una Dama juega en Caballeros, "sube" su número de nivel (baja nivel técnico)
        if (jugador.getGenero() == Genero.FEMENINO && torneoCategoria.getGenero() == Genero.MASCULINO) {
            nivelEvaluado += OFFSET_NIVEL_DAMAS_EN_CABALLEROS;
        }

        // 3. Filtro de Nivel: El número de nivel evaluado no puede ser MENOR al del torneo 
        // (Recordá: 3 es más nivel que 4).
        if (nivelEvaluado < torneoCategoria.getNivel()) {
            throw new IllegalArgumentException(
                "Error: Nivel excedido. " + jugador.getNombre() + " tiene nivel " + jugador.getCategoriaBase() +
                "ta y el mínimo para esta categoría es " + torneoCategoria.getNivel() + "ta."
            );
        }
    }
}