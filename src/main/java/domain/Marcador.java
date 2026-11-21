package domain;

import java.io.Serializable;

/**
 * El marcador del partido: lleva la cuenta de los goles de cada jugador y sabe
 * quién va ganando.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Marcador implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Jugador jugador1;
    private final Jugador jugador2;
    private int golesJugador1;
    private int golesJugador2;

    /**
     * Crea un marcador 0 - 0 para dos jugadores.
     *
     * @param jugador1 el que defiende el arco izquierdo
     * @param jugador2 el que defiende el arco derecho
     */
    public Marcador(Jugador jugador1, Jugador jugador2) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
    }

    /**
     * Suma un gol a quien lo anotó.
     *
     * @param jugador el goleador (tiene que ser uno de los dos del partido)
     */
    public void sumarGol(Jugador jugador) {
        if (jugador == jugador1) {
            golesJugador1++;
        } else if (jugador == jugador2) {
            golesJugador2++;
        } else {
            throw new IllegalArgumentException("Ese jugador no pertenece a este marcador");
        }
    }

    /**
     * @param jugador uno de los dos jugadores
     * @return sus goles
     */
    public int getGoles(Jugador jugador) {
        return jugador == jugador1 ? golesJugador1 : golesJugador2;
    }

    public int getGolesJugador1() {
        return golesJugador1;
    }

    public int getGolesJugador2() {
        return golesJugador2;
    }

    /** @return true si van empatados */
    public boolean hayEmpate() {
        return golesJugador1 == golesJugador2;
    }

    /** @return el que va ganando, o null si hay empate */
    public Jugador getGanador() {
        if (golesJugador1 > golesJugador2) {
            return jugador1;
        }
        if (golesJugador2 > golesJugador1) {
            return jugador2;
        }
        return null;
    }

    /** Vuelve a 0 - 0. */
    public void reiniciar() {
        golesJugador1 = 0;
        golesJugador2 = 0;
    }
}
