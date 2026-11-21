package domain;

/**
 * Perfil experto: decide según el estado del partido. Si el balón está cerca de su arco
 * defiende; si no, ataca. Además aprovecha las patadas al rival cuando lo tiene al lado y
 * guarda el poder para cuando el balón está en terreno rival. A veces se equivoca, como
 * cualquiera.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
public class MaquinaExperta extends Maquina {

    private static final long serialVersionUID = 1L;
    private static final double ZONA_PROPIA = 330;
    private static final double PROBABILIDAD_ERROR = 0.08;

    public MaquinaExperta(long semilla) {
        super(EXPERTA, "Decide la acción según el estado del partido.", 3, semilla);
    }

    @Override
    protected void decidir(Partido partido, Jugador yo, Jugador rival) {
        if (getAzar().nextDouble() < PROBABILIDAD_ERROR) {
            return;   // un descuido
        }
        Balon balon = partido.getBalon();
        double suArco = xDeSuArco(yo);
        boolean enSuZona = Math.abs(balon.getX() - suArco) < ZONA_PROPIA;
        boolean vieneAlArco = Math.abs(dondeCae(balon) - suArco) < ZONA_PROPIA;

        if (rival.estaAturdido() || !(enSuZona || vieneAlArco)) {
            atacar(partido, yo, rival);
        } else {
            // En su zona se pone entre el balón y su arco y despeja.
            double detras = dondeCae(balon) - yo.getDireccionAtaque() * 34;
            irA(yo, detras, 8);
            if (alAlcanceDelPie(yo, balon)) {
                yo.patear();
            } else if (paraCabecear(yo, balon)) {
                yo.saltar();
            }
        }
        if (rivalAlAlcance(yo, rival) && !alAlcanceDelPie(yo, balon)) {
            yo.patear();   // daño al rival
        }
        boolean enTerrenoRival = Math.abs(balon.getX() - suArco) > Cancha.ANCHO / 2.0;
        if (enTerrenoRival || yo.getPoder() >= Jugador.PODER_MAXIMO && enSuZona) {
            yo.activarPoder();
        }
    }
}
