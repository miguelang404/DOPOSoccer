package domain;

/**
 * Poder de Camerún: un balón cubierto de electricidad que sale recto. Si golpea
 * al rival, lo electrocuta: durante 3 segundos no puede saltar y camina a la mitad
 * de la velocidad.
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

    @Override
    protected void efectoSobreRival(Partido partido, Jugador atacante, Jugador rival) {
        rival.aplicarEfecto(Jugador.EFECTO_ELECTROCUTADO, getDuracionTicks());
    }
}
