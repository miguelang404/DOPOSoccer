package domain;

/**
 * Personaje de Corea del Sur, con su tiro de poder "AuraAzul".
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class CoreaDelSur extends Personaje {

    public CoreaDelSur() {
        super("Corea del Sur", "COREA", new AuraAzul());
    }
}
