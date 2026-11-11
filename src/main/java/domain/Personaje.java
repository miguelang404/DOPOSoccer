package domain;

/**
 * Un personaje de DOPOSoccer: representa a un país y trae su propio tiro de poder.
 * Las estadísticas (velocidad, salto, patada, vida) no viven aquí sino en el
 * jugador, porque el enunciado dice que aplican a todos los personajes de un jugador.
 *
 * Para sumar un personaje nuevo se crea una subclase y se agrega a la lista de
 * Juego.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public abstract class Personaje {

    private final String pais;
    private final String codigo;
    private final Poder poder;

    /**
     * @param pais   nombre del país (por ejemplo "Camerún")
     * @param codigo clave corta que usa la interfaz para dibujar su camiseta
     * @param poder  su tiro de poder
     */
    protected Personaje(String pais, String codigo, Poder poder) {
        this.pais = pais;
        this.codigo = codigo;
        this.poder = poder;
    }

    public String getPais() {
        return pais;
    }

    public String getCodigo() {
        return codigo;
    }

    public Poder getPoder() {
        return poder;
    }

    @Override
    public String toString() {
        return pais;
    }
}
