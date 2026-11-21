package domain;

import exceptions.DOPOSoccerExcepcion;
import util.RegistroErrores;
import java.io.Serializable;
/**
 * La cancha donde se juega. Toda la física del juego ocurre en un campo "lógico"
 * de 1000 x 560 unidades; la resolución (pequeña, mediana o grande) solo cambia
 * el tamaño de la ventana, así el partido se juega igual en cualquiera de las tres.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Cancha implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Ancho del campo lógico. */
    public static final int ANCHO = 1000;
    /** Alto del campo lógico. */
    public static final int ALTO = 560;
    /** Altura (y) del piso. */
    public static final int SUELO = 480;
    /** Gravedad que afecta al balón en cada tick. */
    public static final double GRAVEDAD = 0.40;
    /** Gravedad que afecta a los jugadores en cada tick. */
    public static final double GRAVEDAD_JUGADOR = 0.85;
    /** Ancho de cada arco. */
    public static final int PORTERIA_ANCHO = 96;
    /** Alto de cada arco. */
    public static final int PORTERIA_ALTO = 170;

    public static final int PEQUEÑA = 0;
    public static final int MEDIANA = 1;
    public static final int GRANDE = 2;

    private static final String[] NOMBRES = {"Pequeña", "Mediana", "Grande"};
    private static final int[] ANCHOS = {800, 1000, 1280};
    private static final int[] ALTOS = {450, 563, 720};

    private final int resolucion;

    /**
     * Crea la cancha con una de las tres resoluciones.
     *
     * @param resolucion PEQUEÑA, MEDIANA o GRANDE
     * @throws DOPOSoccerExcepcion si la resolución no existe
     */
    public Cancha(int resolucion) throws DOPOSoccerExcepcion {
        if (resolucion < 0 || resolucion >= NOMBRES.length) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.RESOLUCION_INVALIDA));
        }
        this.resolucion = resolucion;
    }

    /** @return cuántas resoluciones tiene el juego */
    public static int cantidadResoluciones() {
        return NOMBRES.length;
    }

    /**
     * @param indice número de la resolución
     * @return su nombre (Pequeña, Mediana o Grande)
     */
    public static String nombreResolucion(int indice) {
        return NOMBRES[indice];
    }

    /**
     * @param indice número de la resolución
     * @return el ancho de la ventana en píxeles
     */
    public static int anchoPantalla(int indice) {
        return ANCHOS[indice];
    }

    /**
     * @param indice número de la resolución
     * @return el alto de la ventana en píxeles
     */
    public static int altoPantalla(int indice) {
        return ALTOS[indice];
    }

    /** @return el índice de la resolución de esta cancha */
    public int getResolucion() {
        return resolucion;
    }

    /** @return nombre de la resolución de esta cancha */
    public String getNombreResolucion() {
        return NOMBRES[resolucion];
    }

    /** @return ancho en píxeles de la ventana de esta cancha */
    public int getAnchoPantalla() {
        return ANCHOS[resolucion];
    }

    /** @return alto en píxeles de la ventana de esta cancha */
    public int getAltoPantalla() {
        return ALTOS[resolucion];
    }
}
