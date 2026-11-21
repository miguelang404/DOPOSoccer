package domain;

/**
 * Perfil torpe: escoge al azar sus movimientos y reacciona lento. Es el más fácil de vencer.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
public class MaquinaTorpe extends Maquina {

    private static final long serialVersionUID = 1L;

    public MaquinaTorpe(long semilla) {
        super(TORPE, "Escoge al azar sus movimientos.", 24, semilla);
    }

    @Override
    protected void decidir(Partido partido, Jugador yo, Jugador rival) {
        yo.mover(getAzar().nextInt(3) - 1);
        if (getAzar().nextInt(4) == 0) {
            yo.saltar();
        }
        if (getAzar().nextInt(4) == 0) {
            yo.patear();
        }
        if (getAzar().nextInt(8) == 0) {
            yo.activarPoder();
        }
    }
}
