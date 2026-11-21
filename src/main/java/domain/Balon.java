package domain;

import java.io.Serializable;

/**
 * El balón. Se mueve con gravedad, rebota en el piso, en el techo y en las
 * paredes, y va perdiendo fuerza poco a poco. No sabe nada de jugadores ni de
 * goles: de eso se encargan Partido y Porteria.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Balon implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Radio del balón (es un círculo). */
    public static final int RADIO = 16;

    private static final double REBOTE_SUELO = 0.74;
    private static final double REBOTE_PARED = 0.80;
    private static final double ROZAMIENTO_AIRE = 0.9985;
    private static final double ROZAMIENTO_SUELO = 0.985;
    private static final double VELOCIDAD_MAXIMA = 24;

    private double x;
    private double y;
    private double vx;
    private double vy;
    private double giro;
    private String efecto;
    private int ticksEfecto;

    /** Crea el balón en el centro de la cancha, listo para caer. */
    public Balon() {
        reiniciar();
    }

    /** Lo deja otra vez en el centro, quieto y sin ningún efecto. */
    public void reiniciar() {
        x = Cancha.ANCHO / 2.0;
        y = 110;
        vx = 0;
        vy = 0;
        giro = 0;
        efecto = "";
        ticksEfecto = 0;
    }

    /** Avanza el balón un instante (un tick). */
    public void mover() {
        // Recomendación de la IA generativa para la física del balón: gravedad, rozamiento y rebotes
        // con piso, techo y paredes. Cuando hay un poder activo se apaga la gravedad y el rozamiento
        // para que el tiro vaya recto.
        // Mientras dura un tiro de poder el balón vuela recto: sin gravedad y sin perder fuerza.
        if (efecto.isEmpty()) {
            vy += Cancha.GRAVEDAD;
            vx *= ROZAMIENTO_AIRE;
        }
        limitarVelocidad();
        x += vx;
        y += vy;

        // Piso: rebota, y si ya casi no sube se queda rodando.
        if (y + RADIO >= Cancha.SUELO) {
            y = Cancha.SUELO - RADIO;
            if ("TORNADO".equals(efecto)) {
                vy = 0;           // el tornado corre pegado al suelo
            } else if (vy > 1.5 || !efecto.isEmpty()) {
                terminarEfecto();  // chocar con el piso acaba el poder
                vy = -vy * REBOTE_SUELO;
            } else {
                vy = 0;
                vx *= ROZAMIENTO_SUELO;
                if (Math.abs(vx) < 0.02) {
                    vx = 0;
                }
            }
        }
        // Techo
        if (y - RADIO < 0) {
            y = RADIO;
            vy = Math.abs(vy) * 0.6;
            terminarEfecto();
        }
        // Paredes
        if (x - RADIO < 0) {
            x = RADIO;
            vx = Math.abs(vx) * REBOTE_PARED;
            terminarEfecto();
        }
        if (x + RADIO > Cancha.ANCHO) {
            x = Cancha.ANCHO - RADIO;
            vx = -Math.abs(vx) * REBOTE_PARED;
            terminarEfecto();
        }

        giro += vx / RADIO;
        if (ticksEfecto > 0) {
            ticksEfecto--;
            if (ticksEfecto == 0) {
                efecto = "";
            }
        }
    }

    private void limitarVelocidad() {
        double rapidez = Math.hypot(vx, vy);
        if (rapidez > VELOCIDAD_MAXIMA) {
            vx = vx / rapidez * VELOCIDAD_MAXIMA;
            vy = vy / rapidez * VELOCIDAD_MAXIMA;
        }
    }

    /** Apaga el efecto de poder: desde ese momento el balón vuelve a ser uno normal. */
    public void terminarEfecto() {
        efecto = "";
        ticksEfecto = 0;
    }

    /**
     * Le da velocidad al balón de golpe (una patada, un tiro de poder...).
     *
     * @param nuevaVx velocidad horizontal
     * @param nuevaVy velocidad vertical (negativa es hacia arriba)
     */
    public void impulsar(double nuevaVx, double nuevaVy) {
        vx = nuevaVx;
        vy = nuevaVy;
        limitarVelocidad();
    }

    /**
     * Pone el balón en un punto exacto.
     *
     * @param nuevaX posición horizontal
     * @param nuevaY posición vertical
     */
    public void setPosicion(double nuevaX, double nuevaY) {
        x = nuevaX;
        y = nuevaY;
    }

    /**
     * Cambia la velocidad (la usan las colisiones); nunca pasa de la velocidad máxima.
     *
     * @param nuevaVx velocidad horizontal
     * @param nuevaVy velocidad vertical
     */
    public void setVelocidad(double nuevaVx, double nuevaVy) {
        vx = nuevaVx;
        vy = nuevaVy;
        limitarVelocidad();
    }

    /**
     * Marca el balón con el efecto visual de un tiro de poder.
     *
     * @param codigo nombre del efecto (por ejemplo "RAYO")
     * @param ticks  cuánto dura
     */
    public void setEfecto(String codigo, int ticks) {
        efecto = codigo;
        ticksEfecto = ticks;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    /** @return cuánto ha girado el balón, en radianes (para dibujarlo rodando) */
    public double getGiro() {
        return giro;
    }

    /** @return el efecto activo ("" si no tiene ninguno) */
    public String getEfecto() {
        return efecto;
    }

    public int getTicksEfecto() {
        return ticksEfecto;
    }
}
