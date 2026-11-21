package domain;

/**
 * Perfil defensivo: su prioridad es defender el arco. Se queda cerca de su arco y solo sale
 * a buscar el balón cuando se acerca; entonces lo despeja.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
public class MaquinaDefensiva extends Maquina {

    private static final long serialVersionUID = 1L;
    private static final double DISTANCIA_PELIGRO = 380;
    private static final double MARGEN_CASA = 190;

    public MaquinaDefensiva(long semilla) {
        super(DEFENSIVA, "Su prioridad es defender el arco.", 4, semilla);
    }

    @Override
    protected void decidir(Partido partido, Jugador yo, Jugador rival) {
        Balon balon = partido.getBalon();
        double suArco = xDeSuArco(yo);
        double casa = yo.getDireccionAtaque() == 1 ? MARGEN_CASA : Cancha.ANCHO - MARGEN_CASA;
        boolean peligro = Math.abs(dondeCae(balon) - suArco) < DISTANCIA_PELIGRO
                || Math.abs(balon.getX() - suArco) < DISTANCIA_PELIGRO;
        if (peligro) {
            atacar(partido, yo, rival);   // lo despeja hacia el campo rival
            yo.activarPoder();
        } else {
            irA(yo, casa, 12);
        }
    }
}
