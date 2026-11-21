package domain;

import java.io.Serializable;
import java.util.Random;

/**
 * Un jugador controlado por el computador. Cada perfil (torpe, ofensivo, defensivo,
 * experto) decide qué pedirle a su jugador: moverse, saltar, patear o activar el
 * poder. La máquina usa exactamente los mismos pedidos que el teclado, así que no
 * tiene ninguna ventaja especial sobre una persona.
 *
 * Para crear un perfil nuevo basta con extender esta clase, implementar decidir y
 * sumarlo a la lista de Juego.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
public abstract class Maquina implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TORPE = "Torpe";
    public static final String OFENSIVA = "Ofensiva";
    public static final String DEFENSIVA = "Defensiva";
    public static final String EXPERTA = "Experta";

    /** Distancia horizontal desde la que el pie le llega al balón. */
    protected static final double ALCANCE_PIE = 62;

    private final String perfil;
    private final String descripcion;
    private final int reaccion;
    private final Random azar;
    private double objetivoX;
    private double tolerancia;
    private boolean siguiendo;
    private int ticks;

    /**
     * @param perfil      nombre del perfil
     * @param descripcion cómo juega, en una frase
     * @param reaccion    cada cuántos ticks decide algo (más alto = más lenta)
     * @param semilla     semilla del azar (para poder repetir un partido en las pruebas)
     */
    protected Maquina(String perfil, String descripcion, int reaccion, long semilla) {
        this.perfil = perfil;
        this.descripcion = descripcion;
        this.reaccion = Math.max(1, reaccion);
        this.azar = new Random(semilla);
    }

    /**
     * Se llama en cada tick del partido: cada cierto número de ticks (según su reacción)
     * la máquina decide qué hacer.
     *
     * @param partido el partido en juego
     * @param yo      el jugador que controla la máquina
     * @param rival   el otro jugador
     */
    public final void actuar(Partido partido, Jugador yo, Jugador rival) {
        ticks++;
        if (ticks % reaccion == 0) {
            siguiendo = false;
            decidir(partido, yo, rival);
        }
        // Hacia dónde ir se decide cada cierto tiempo, pero al caminar se corrige en cada tick para
        // no pasarse del objetivo ni quedarse temblando de un lado a otro.
        if (siguiendo) {
            caminarHacia(yo);
        }
    }

    /**
     * Cada perfil decide aquí qué pedirle a su jugador.
     *
     * @param partido el partido en juego
     * @param yo      el jugador que controla la máquina
     * @param rival   el otro jugador
     */
    protected abstract void decidir(Partido partido, Jugador yo, Jugador rival);

    // herramientas comunes

    /** @return el azar propio de esta máquina */
    protected Random getAzar() {
        return azar;
    }

    /** @return x del arco que defiende el jugador */
    protected double xDeSuArco(Jugador yo) {
        return yo.getDireccionAtaque() == 1 ? 0 : Cancha.ANCHO;
    }

    /**
     * Calcula en qué x va a caer el balón (sin contar el choque con jugadores).
     *
     * @param balon el balón
     * @return la x donde tocará el piso
     */
    protected double dondeCae(Balon balon) {
        double x = balon.getX();
        double y = balon.getY();
        double vx = balon.getVx();
        double vy = balon.getVy();
        boolean recto = !balon.getEfecto().isEmpty();
        for (int i = 0; i < 90; i++) {
            if (!recto) {
                vy += Cancha.GRAVEDAD;
                vx *= 0.9985;
            }
            x += vx;
            y += vy;
            if (x < Balon.RADIO || x > Cancha.ANCHO - Balon.RADIO) {
                vx = -vx * 0.9;
                x = Math.max(Balon.RADIO, Math.min(Cancha.ANCHO - Balon.RADIO, x));
            }
            if (y + Balon.RADIO >= Cancha.SUELO) {
                break;
            }
        }
        return x;
    }

    /** Camina hacia una x y se queda quieto cuando está cerca. */
    protected void irA(Jugador yo, double objetivo, double tolerancia) {
        objetivoX = objetivo;
        this.tolerancia = tolerancia;
        siguiendo = true;
        caminarHacia(yo);
    }

    private void caminarHacia(Jugador yo) {
        double dx = objetivoX - yo.getX();
        if (Math.abs(dx) <= tolerancia) {
            yo.mover(0);
        } else {
            yo.mover(dx > 0 ? 1 : -1);
        }
    }

    /** @return true si el balón está delante del pie, bajo, y se puede patear */
    protected boolean alAlcanceDelPie(Jugador yo, Balon balon) {
        double delante = (balon.getX() - yo.getX()) * yo.getDireccionAtaque();
        return delante > 12 && delante < ALCANCE_PIE && balon.getY() > yo.getY() - 6;
    }

    /** @return true si el balón viene por encima de la cabeza y conviene saltar a cabecearlo */
    protected boolean paraCabecear(Jugador yo, Balon balon) {
        return yo.estaEnElPiso() && Math.abs(balon.getX() - yo.getX()) < 52
                && balon.getY() < yo.getY() - 12 && balon.getY() > yo.getY() - 150;
    }

    /** @return true si el rival está pegado y se le puede dar una patada */
    protected boolean rivalAlAlcance(Jugador yo, Jugador rival) {
        double delante = (rival.getX() - yo.getX()) * yo.getDireccionAtaque();
        return !rival.estaAturdido() && delante > 10 && delante < 62 && Math.abs(rival.getY() - yo.getY()) < 60;
    }

    /** Intenta quedar detrás del balón y, si ya lo tiene al frente, lo patea o lo cabecea. */
    protected void atacar(Partido partido, Jugador yo, Jugador rival) {
        Balon balon = partido.getBalon();
        irA(yo, dondeCae(balon) - yo.getDireccionAtaque() * 34, 8);
        if (alAlcanceDelPie(yo, balon)) {
            yo.patear();
        } else if (paraCabecear(yo, balon)) {
            yo.saltar();
        } else if (rivalAlAlcance(yo, rival)) {
            yo.patear();
        }
    }

    // consultas

    public String getPerfil() {
        return perfil;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return perfil;
    }
}
