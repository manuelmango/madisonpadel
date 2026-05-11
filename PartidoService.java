import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartidoService {

    private final PartidoRepository partidoRepository;

    @Transactional
    public Partido cargarResultado(Long partidoId, int setsP1, int setsP2, int gamesP1, int gamesP2) {
        
        // 1. Buscamos el partido que se acaba de jugar
        Partido partidoJugado = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new RuntimeException("Partido no encontrado"));

        // 2. Cargamos los stats
        partidoJugado.setSetsPareja1(setsP1);
        partidoJugado.setSetsPareja2(setsP2);
        partidoJugado.setGamesPareja1(gamesP1);
        partidoJugado.setGamesPareja2(gamesP2);
        partidoJugado.setEstado(EstadoPartido.FINALIZADO);

        // 3. Determinamos quién ganó y quién perdió
        Pareja ganador;
        Pareja perdedor;
        
        if (setsP1 > setsP2) {
            ganador = partidoJugado.getPareja1();
            perdedor = partidoJugado.getPareja2();
        } else {
            ganador = partidoJugado.getPareja2();
            perdedor = partidoJugado.getPareja1();
        }
        
        partidoJugado.setGanador(ganador);
        partidoRepository.save(partidoJugado);

        // 4. AUTOMATISMO GSL: Si estamos en fase de ZONA, intentamos propagar
        if (partidoJugado.getFase() == FasePartido.ZONA) {
            propagarEnZonaGSL(partidoJugado, ganador, perdedor);
        }

        return partidoJugado;
    }

    private void propagarEnZonaGSL(Partido partidoJugado, Pareja ganador, Pareja perdedor) {
        // Traemos todos los partidos de esa misma zona
        List<Partido> partidosDeLaZona = partidoRepository.findByZonaNombreAndFase(
                partidoJugado.getZona().getNombre(), FasePartido.ZONA);

        // Necesitamos saber si el partido que terminó es el "P1" o el "P2" de la zona.
        // Esto depende de cómo los hayas nombrado en tu base de datos al generarlos.
        // Asumamos que tenés una forma de saberlo (por ejemplo, si es el primer partido de la lista)
        String idPartidoEnZona = determinarIdentificadorPartido(partidoJugado, partidosDeLaZona); 
        // idPartidoEnZona nos devolverá "P1" o "P2"
        
        String etiquetaGanadorBuscada = "Ganador " + idPartidoEnZona;
        String etiquetaPerdedorBuscada = "Perdedor " + idPartidoEnZona;

        for (Partido pFuturo : partidosDeLaZona) {
            // Buscamos si algún partido está esperando a este GANADOR
            if (etiquetaGanadorBuscada.equals(pFuturo.getOrigenPareja1())) {
                pFuturo.setPareja1(ganador);
                partidoRepository.save(pFuturo);
            } else if (etiquetaGanadorBuscada.equals(pFuturo.getOrigenPareja2())) {
                pFuturo.setPareja2(ganador);
                partidoRepository.save(pFuturo);
            }

            // Buscamos si algún partido está esperando a este PERDEDOR
            if (etiquetaPerdedorBuscada.equals(pFuturo.getOrigenPareja1())) {
                pFuturo.setPareja1(perdedor);
                partidoRepository.save(pFuturo);
            } else if (etiquetaPerdedorBuscada.equals(pFuturo.getOrigenPareja2())) {
                pFuturo.setPareja2(perdedor);
                partidoRepository.save(pFuturo);
            }
        }
    }
    
    // Método auxiliar para saber si el partido que acaba de terminar era el P1 o el P2 de la zona
    private String determinarIdentificadorPartido(Partido partido, List<Partido> partidosZona) {
        // Acá tenés que poner la lógica de cómo diferencias al Partido 1 del Partido 2
        // Si el Partido 1 tiene un ID menor al Partido 2 en la base de datos, podés ordenarlos.
        // Si ya tenés un campo en tu entidad Partido que dice "nombre" o "numero", usá eso.
        
        // Ejemplo asumiendo orden cronológico por ID:
        partidosZona.sort((p1, p2) -> p1.getId().compareTo(p2.getId()));
        if (partido.getId().equals(partidosZona.get(0).getId())) return "P1";
        if (partido.getId().equals(partidosZona.get(1).getId())) return "P2";
        
        return "Desconocido"; // Si es el partido 3 o 4, no propaga a nadie más dentro de la zona
    }
}