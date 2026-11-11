package domain;

/**
 * Poder de Camerún: un balón cubierto de electricidad que sale recto. Al golpear
 * al rival lo electrocutaría (versión posterior).
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Rayo extends Poder {

    public Rayo() {
        super("Rayo", "RAYO",
                "Recto y cubierto de electricidad. El rival no puede saltar y va a la mitad de velocidad.", 60, 3);
    }

    @Override
    public void aplicar(Partido partido, Jugador atacante) {
        Balon balon = partido.getBalon();
        balon.impulsar(atacante.getDireccionAtaque() * 22, 0);
        balon.setEfecto(getCodigo(), 120);
    }
}
