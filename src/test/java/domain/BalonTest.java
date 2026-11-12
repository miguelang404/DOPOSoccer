package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del balón: caída, rebotes, paredes y velocidad máxima.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class BalonTest {

    private Balon balon;

    @BeforeEach
    void preparar() {
        balon = new Balon();
    }

    @Test
    void deberiaEmpezarQuietoEnElCentro() {
        assertEquals(Cancha.ANCHO / 2.0, balon.getX(), 0.001);
        assertEquals(0, balon.getVx(), 0.001);
        assertEquals("", balon.getEfecto());
    }

    @Test
    void deberiaCaerPorLaGravedad() {
        double yInicial = balon.getY();
        balon.mover();
        assertTrue(balon.getY() > yInicial);
        assertEquals(Cancha.GRAVEDAD, balon.getVy(), 0.001);
    }

    @Test
    void deberiaRebotarEnElSuelo() {
        balon.setPosicion(500, Cancha.SUELO - Balon.RADIO - 1);
        balon.setVelocidad(0, 10);
        balon.mover();
        assertTrue(balon.getVy() < 0, "después de tocar el piso debe subir");
        assertTrue(balon.getY() + Balon.RADIO <= Cancha.SUELO + 0.001);
    }

    @Test
    void deberiaRebotarEnLasParedes() {
        balon.setPosicion(Balon.RADIO + 1, 200);
        balon.setVelocidad(-15, 0);
        balon.mover();
        assertTrue(balon.getVx() > 0);
        balon.setPosicion(Cancha.ANCHO - Balon.RADIO - 1, 200);
        balon.setVelocidad(15, 0);
        balon.mover();
        assertTrue(balon.getVx() < 0);
    }

    @Test
    void noDeberiaPasarDeLaVelocidadMaxima() {
        balon.setVelocidad(500, 500);
        assertTrue(Math.hypot(balon.getVx(), balon.getVy()) <= 24.0001);
        balon.impulsar(-900, 0);
        assertTrue(Math.hypot(balon.getVx(), balon.getVy()) <= 24.0001);
    }

    @Test
    void elEfectoDeberiaDesaparecerCuandoSeAcabaElTiempo() {
        balon.setEfecto("RAYO", 2);
        assertEquals("RAYO", balon.getEfecto());
        balon.mover();
        assertEquals("RAYO", balon.getEfecto());
        balon.mover();
        assertEquals("", balon.getEfecto());
    }

    @Test
    void conUnTiroDePoderElBalonNoDeberiaCaer() {
        balon.setPosicion(500, 200);
        balon.impulsar(20, 0);
        balon.setEfecto("RAYO", 100);
        for (int i = 0; i < 20; i++) {
            balon.mover();
        }
        assertEquals(200, balon.getY(), 0.001);
        assertEquals(20, balon.getVx(), 0.001);
    }

    @Test
    void cuandoSeAcabaElEfectoElBalonDeberiaVolverACaer() {
        balon.setPosicion(500, 200);
        balon.setEfecto("RAYO", 2);
        balon.mover();
        balon.mover();
        double antes = balon.getY();
        balon.mover();
        balon.mover();
        assertTrue(balon.getY() > antes);
    }

    @Test
    void elPoderDeberiaAcabarseAlChocarConUnaPared() {
        balon.setPosicion(Cancha.ANCHO - Balon.RADIO - 5, 200);
        balon.impulsar(20, 0);
        balon.setEfecto("RAYO", 100);
        balon.mover();
        assertEquals("", balon.getEfecto());
        assertTrue(balon.getVx() < 0);
    }

    @Test
    void elPoderDeberiaAcabarseAlChocarConElPiso() {
        balon.setPosicion(500, Cancha.SUELO - Balon.RADIO - 3);
        balon.impulsar(10, 4);
        balon.setEfecto("AURA_AZUL", 100);
        balon.mover();
        assertEquals("", balon.getEfecto());
    }

    @Test
    void elTornadoNoDeberiaAcabarseAlRodarPorElSuelo() {
        balon.setPosicion(300, Cancha.SUELO - Balon.RADIO);
        balon.impulsar(15, 0);
        balon.setEfecto("TORNADO", 100);
        for (int i = 0; i < 10; i++) {
            balon.mover();
        }
        assertEquals("TORNADO", balon.getEfecto());
    }

    @Test
    void reiniciarDeberiaLimpiarTodo() {
        balon.setPosicion(10, 10);
        balon.impulsar(5, 5);
        balon.setEfecto("TORNADO", 50);
        balon.reiniciar();
        assertEquals(Cancha.ANCHO / 2.0, balon.getX(), 0.001);
        assertEquals(0, balon.getVy(), 0.001);
        assertEquals("", balon.getEfecto());
    }
}
