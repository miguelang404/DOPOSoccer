package domain;

/**
 * Cada uno de los dos arcos. Sabe dónde está, si el balón ya entró (gol) y
 * hace que el balón rebote en el travesaño.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Porteria {

    public static final String IZQUIERDA = "IZQUIERDA";
    public static final String DERECHA = "DERECHA";

    /** Grueso del travesaño (la barra de arriba). */
    public static final int GROSOR = 14;

    private final String lado;
    private final int ancho;
    private final int alto;

    /**
     * Crea el arco de uno de los dos lados.
     *
     * @param lado IZQUIERDA o DERECHA
     */
    public Porteria(String lado) {
        this.lado = lado;
        this.ancho = Cancha.PORTERIA_ANCHO;
        this.alto = Cancha.PORTERIA_ALTO;
    }

    /** @return IZQUIERDA o DERECHA */
    public String getLado() {
        return lado;
    }

    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }

    /** @return x donde empieza el arco */
    public int getXInicio() {
        return IZQUIERDA.equals(lado) ? 0 : Cancha.ANCHO - ancho;
    }

    /** @return x donde termina el arco */
    public int getXFin() {
        return getXInicio() + ancho;
    }

    /** @return y de la parte de arriba del travesaño */
    public int getYTravesaño() {
        return Cancha.SUELO - alto;
    }

    /**
     * Revisa si el balón ya entró completo al arco y por debajo del travesaño.
     *
     * @param balon el balón
     * @return true si es gol
     */
    public boolean esGol(Balon balon) {
        boolean dentro;
        if (IZQUIERDA.equals(lado)) {
            dentro = balon.getX() + Balon.RADIO < getXFin();
        } else {
            dentro = balon.getX() - Balon.RADIO > getXInicio();
        }
        return dentro && balon.getY() > getYTravesaño() + GROSOR;
    }

    /**
     * Hace rebotar el balón contra el travesaño (un rectángulo).
     *
     * @param balon el balón
     * @return true si el balón está apoyado encima del travesaño
     */
    public boolean rebotar(Balon balon) {
        // Recomendación de la IA generativa para el rebote en el travesaño: se busca el punto del
        // rectángulo más cercano al centro del balón y, si está a menos de un radio, se rebota respecto
        // a la dirección que une los dos puntos.
        double x0 = getXInicio();
        double x1 = getXFin();
        double y0 = getYTravesaño();
        double y1 = y0 + GROSOR;

        // Punto del rectángulo más cercano al centro del balón
        double cx = Math.max(x0, Math.min(balon.getX(), x1));
        double cy = Math.max(y0, Math.min(balon.getY(), y1));
        double dx = balon.getX() - cx;
        double dy = balon.getY() - cy;
        double distancia2 = dx * dx + dy * dy;

        if (distancia2 >= (double) Balon.RADIO * Balon.RADIO) {
            return false;
        }
        balon.terminarEfecto();   // pegarle al travesaño acaba el poder
        if (distancia2 == 0) {
            // El centro quedó dentro de la barra: lo sacamos por arriba.
            balon.setPosicion(balon.getX(), y0 - Balon.RADIO);
            balon.setVelocidad(balon.getVx(), -Math.abs(balon.getVy()) * 0.5);
            return true;
        }
        double distancia = Math.sqrt(distancia2);
        double nx = dx / distancia;
        double ny = dy / distancia;
        balon.setPosicion(cx + nx * Balon.RADIO, cy + ny * Balon.RADIO);

        double normal = balon.getVx() * nx + balon.getVy() * ny;
        if (normal < 0) {
            balon.setVelocidad(balon.getVx() - 1.7 * normal * nx,
                               balon.getVy() - 1.7 * normal * ny);
        }
        return ny < -0.7;
    }
}
