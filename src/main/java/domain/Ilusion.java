package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Poder de Estados Unidos: el balón se divide en 8 balones idénticos. Uno es el
 * verdadero y los otros 7 son falsos: se mueven y desaparecen pasados unos segundos.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Ilusion extends Poder {

    private static final int SEÑUELOS = 7;
    private static final int DURACION_TICKS = 150;

    public Ilusion() {
        super("Ilusión", "ILUSION",
                "El balón se divide en 8 iguales (7 falsos). El rival no sabe cuál es el real.", 30, 2.5);
    }

    @Override
    public void aplicar(Partido partido, Jugador atacante) {
        // Recomendación de la IA generativa para el poder Ilusión: los 7 balones falsos salen en
        // abanico (cada uno con una velocidad vertical distinta) y tienen el mismo efecto del balón
        // real, para que ninguno se distinga por caer distinto.
        Balon real = partido.getBalon();
        int d = atacante.getDireccionAtaque();
        List<Balon> falsos = new ArrayList<Balon>();
        for (int i = 0; i < SEÑUELOS; i++) {
            Balon falso = new Balon();
            falso.setPosicion(real.getX(), real.getY());
            falso.setVelocidad(d * (11 + i * 1.3), (i - 3) * 1.2);
            falso.setEfecto(getCodigo(), DURACION_TICKS);
            falsos.add(falso);
        }
        partido.agregarSeñuelos(falsos, DURACION_TICKS);
        real.impulsar(d * 17, 0);
        real.setEfecto(getCodigo(), DURACION_TICKS);
    }
}
