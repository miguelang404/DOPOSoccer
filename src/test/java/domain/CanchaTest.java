package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de la cancha y sus tres resoluciones.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class CanchaTest {

    @Test
    void deberiaCrearLasTresResoluciones() throws DOPOSoccerExcepcion {
        assertEquals(3, Cancha.cantidadResoluciones());
        assertEquals("Pequeña", new Cancha(Cancha.PEQUEÑA).getNombreResolucion());
        assertEquals("Mediana", new Cancha(Cancha.MEDIANA).getNombreResolucion());
        assertEquals("Grande", new Cancha(Cancha.GRANDE).getNombreResolucion());
    }

    @Test
    void deberiaDarElTamañoDePantallaDeCadaResolucion() throws DOPOSoccerExcepcion {
        assertEquals(800, new Cancha(Cancha.PEQUEÑA).getAnchoPantalla());
        assertEquals(450, new Cancha(Cancha.PEQUEÑA).getAltoPantalla());
        assertEquals(1280, new Cancha(Cancha.GRANDE).getAnchoPantalla());
        assertEquals(720, new Cancha(Cancha.GRANDE).getAltoPantalla());
    }

    @Test
    void noDeberiaCrearUnaResolucionQueNoExiste() {
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class, () -> new Cancha(7));
        assertEquals(DOPOSoccerExcepcion.RESOLUCION_INVALIDA, e.getMessage());
        assertThrows(DOPOSoccerExcepcion.class, () -> new Cancha(-1));
    }
}
