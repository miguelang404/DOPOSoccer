package domain;

/**
 * Poder de Corea del Sur: el balón sale recto y horizontal, a mucha velocidad.
 * (Cuando golpee al rival lo empujará hacia su propio arco; eso llega en una
 * versión posterior.)
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class AuraAzul extends Poder {

    public AuraAzul() {
        super("Aura Azul", "AURA_AZUL",
                "Recto y horizontal, a alta velocidad. Empuja al rival hacia su propio arco.", 40, 1.5);
    }

    @Override
    public void aplicar(Partido partido, Jugador atacante) {
        Balon balon = partido.getBalon();
        balon.impulsar(atacante.getDireccionAtaque() * 24, 0);
        balon.setEfecto(getCodigo(), 120);
    }
}
