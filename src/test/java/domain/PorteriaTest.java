package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de los arcos: cuándo es gol y cuándo el balón pega en el travesaño.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class PorteriaTest {

    private final Porteria izquierda = new Porteria(Porteria.IZQUIERDA);
    private final Porteria derecha = new Porteria(Porteria.DERECHA);

    private Balon balonEn(double x, double y) {
        Balon b = new Balon();
        b.setPosicion(x, y);
        return b;
    }

    @Test
    void deberiaSerGolCuandoElBalonEntraCompletoPorLaIzquierda() {
        assertTrue(izquierda.esGol(balonEn(20, Cancha.SUELO - 30)));
    }

    @Test
    void deberiaSerGolCuandoElBalonEntraCompletoPorLaDerecha() {
        assertTrue(derecha.esGol(balonEn(Cancha.ANCHO - 20, Cancha.SUELO - 30)));
    }

    @Test
    void noDeberiaSerGolSiElBalonSoloEstaAMedias() {
        assertFalse(izquierda.esGol(balonEn(izquierda.getXFin() - 5, Cancha.SUELO - 30)));
        assertFalse(derecha.esGol(balonEn(derecha.getXInicio() + 5, Cancha.SUELO - 30)));
    }

    @Test
    void noDeberiaSerGolSiVaPorEncimaDelTravesaño() {
        assertFalse(izquierda.esGol(balonEn(20, izquierda.getYTravesaño() - 40)));
    }

    @Test
    void noDeberiaSerGolEnElArcoContrario() {
        assertFalse(derecha.esGol(balonEn(20, Cancha.SUELO - 30)));
        assertFalse(izquierda.esGol(balonEn(Cancha.ANCHO - 20, Cancha.SUELO - 30)));
    }

    @Test
    void elTravesañoDeberiaHacerRebotarElBalon() {
        Balon b = balonEn(50, izquierda.getYTravesaño() - Balon.RADIO + 5);
        b.setVelocidad(0, 8);
        boolean encima = izquierda.rebotar(b);
        assertTrue(b.getVy() < 0, "debe salir hacia arriba");
        assertTrue(encima);
    }

    @Test
    void pegarleAlTravesañoDeberiaAcabarElPoder() {
        Balon b = balonEn(50, izquierda.getYTravesaño() - Balon.RADIO + 5);
        b.setVelocidad(0, 8);
        b.setEfecto("RAYO", 100);
        izquierda.rebotar(b);
        assertEquals("", b.getEfecto());
    }

    @Test
    void sinContactoNoDeberiaCambiarNada() {
        Balon b = balonEn(500, 200);
        b.setVelocidad(3, 3);
        assertFalse(izquierda.rebotar(b));
        assertTrue(b.getVx() == 3 && b.getVy() == 3);
    }
}
