package domain;

/**
 * Personaje de Estados Unidos, con su tiro de poder "Ilusion".
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class EstadosUnidos extends Personaje {

    public EstadosUnidos() {
        super("Estados Unidos", "EEUU", new Ilusion());
    }
}
