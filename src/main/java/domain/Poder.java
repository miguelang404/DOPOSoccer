package domain;

/**
 * El tiro de poder de un personaje. Cada país tiene el suyo: lo único que cambia
 * es cómo sale el balón (método aplicar) y los datos de daño y duración que
 * vienen en la tabla del enunciado.
 *
 * Para agregar un poder nuevo basta con crear una subclase de esta clase y
 * dársela a un personaje; el resto del juego no se toca.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public abstract class Poder {

    private final String nombre;
    private final String codigo;
    private final String descripcion;
    private final int dañoTiro;
    private final double duracionEfecto;

    /**
     * @param nombre         cómo se llama el poder (por ejemplo "Rayo")
     * @param codigo         clave corta que usa la interfaz para dibujar el efecto
     * @param descripcion    qué le hace al rival cuando lo golpea
     * @param dañoTiro       daño base del tiro (ya multiplicado por 5, como dice el anexo)
     * @param duracionEfecto segundos que dura el efecto sobre el rival
     */
    protected Poder(String nombre, String codigo, String descripcion, int dañoTiro, double duracionEfecto) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.dañoTiro = dañoTiro;
        this.duracionEfecto = duracionEfecto;
    }

    /**
     * Lanza el tiro de poder: cambia la velocidad del balón (y lo que haga falta)
     * según el poder.
     *
     * @param partido  el partido en juego
     * @param atacante quien tiene el poder activo
     */
    public abstract void aplicar(Partido partido, Jugador atacante);

    /**
     * Daño que haría el tiro si golpeara al rival: DañoTiro x multiplicadorKick.
     * (El daño todavía no se aplica en esta versión; queda calculado para la siguiente.)
     *
     * @param atacante quien dispara
     * @return el daño del tiro
     */
    public double calcularDaño(Jugador atacante) {
        return dañoTiro * atacante.getMultiplicadorKick();
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDañoTiro() {
        return dañoTiro;
    }

    public double getDuracionEfecto() {
        return duracionEfecto;
    }
}
