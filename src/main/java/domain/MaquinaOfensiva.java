package domain;

/**
 * Perfil ofensivo: su prioridad es anotar. Persigue el balón, se pone detrás de él y lo
 * patea hacia el arco rival; usa el poder en cuanto lo tiene.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
public class MaquinaOfensiva extends Maquina {

    private static final long serialVersionUID = 1L;

    public MaquinaOfensiva(long semilla) {
        super(OFENSIVA, "Su prioridad es anotar.", 4, semilla);
    }

    @Override
    protected void decidir(Partido partido, Jugador yo, Jugador rival) {
        atacar(partido, yo, rival);
        yo.activarPoder();
    }
}
