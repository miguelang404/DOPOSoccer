package domain;

/**
 * Personaje de Camerún, con su tiro de poder "Rayo".
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Camerun extends Personaje {

    public Camerun() {
        super("Camerún", "CAMERUN", new Rayo());
    }
}
