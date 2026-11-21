package domain;

/**
 * Poder de Corea del Sur: el balón sale recto y horizontal, a mucha velocidad.
 * Si golpea al rival, lo lanza hacia su propio arco durante 1,5 segundos.
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

    @Override
    protected void efectoSobreRival(Partido partido, Jugador atacante, Jugador rival) {
        rival.aplicarEfecto(Jugador.EFECTO_EMPUJADO, getDuracionTicks());
    }
}
