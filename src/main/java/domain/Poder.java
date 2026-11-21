package domain;

/**
 * El tiro de poder de un personaje. Cada país tiene el suyo: lo único que cambia
 * es cómo sale el balón (método aplicar) y los datos de daño y duración que
 * vienen en la tabla del enunciado.
 *
 * Para agregar un poder nuevo basta con crear una subclase de esta clase y
 * dársela a un personaje; el resto del juego no se toca.
 *
 * El tiro tiene dos momentos: aplicar (sale el balón) y golpear (el balón alcanza
 * al rival, que recibe el daño y el efecto propio de cada poder).
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
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
     * Qué le pasa al rival cuando el tiro lo golpea (aparte del daño): cada poder
     * le deja un efecto distinto.
     *
     * @param partido  el partido en juego
     * @param atacante quien disparó
     * @param rival    el jugador golpeado
     */
    protected abstract void efectoSobreRival(Partido partido, Jugador atacante, Jugador rival);

    /**
     * El tiro golpea al rival: le hace el daño del poder y, si no quedó aturdido,
     * le deja el efecto. Si el tiro entra al arco sin tocar al rival, este método
     * nunca se llama y por eso no hay daño ni efecto.
     *
     * @param partido  el partido en juego
     * @param atacante quien disparó
     * @param rival    el jugador golpeado
     */
    public void golpear(Partido partido, Jugador atacante, Jugador rival) {
        rival.recibirDaño(calcularDaño(atacante));
        if (!rival.estaAturdido()) {
            efectoSobreRival(partido, atacante, rival);
        }
    }

    /**
     * @return true si el balón de este poder se carga delante del pie en vez de delante de la frente
     */
    public boolean saleDelPie() {
        return false;
    }

    /**
     * Daño que hace el tiro cuando golpea al rival: DañoTiro x multiplicadorKick.
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

    /**
     * @return lo que dura el efecto sobre el rival, en ticks (60 por segundo)
     */
    public int getDuracionTicks() {
        return (int) Math.round(duracionEfecto * Partido.TICKS_POR_SEGUNDO);
    }
}
