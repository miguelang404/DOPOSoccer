package domain;

/**
 * Poder de Nigeria: un remolino a ras de suelo que va rebotando rápido.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Tornado extends Poder {

    public Tornado() {
        super("Tornado", "TORNADO",
                "Remolino a ras de suelo que rebota rápido. Deja al rival inconsciente en el aire.", 40, 3);
    }

    @Override
    public void aplicar(Partido partido, Jugador atacante) {
        Balon balon = partido.getBalon();
        // Lo bajamos al suelo para que corra pegado a él.
        balon.setPosicion(balon.getX(), Cancha.SUELO - Balon.RADIO);
        balon.impulsar(atacante.getDireccionAtaque() * 17, 0);
        balon.setEfecto(getCodigo(), 150);
    }
}
