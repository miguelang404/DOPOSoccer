package domain;

/**
 * Personaje de Nigeria, con su tiro de poder "Tornado".
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Nigeria extends Personaje {

    public Nigeria() {
        super("Nigeria", "NIGERIA", new Tornado());
    }
}
