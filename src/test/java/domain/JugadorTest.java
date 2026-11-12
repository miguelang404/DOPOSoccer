package domain;

import exceptions.DOPOSoccerExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del jugador: movimiento, salto, patada, barra de poder y estadísticas.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class JugadorTest {

    private Jugador jugador;

    @BeforeEach
    void preparar() {
        jugador = new Jugador("Jugador 1", new CoreaDelSur(), Porteria.IZQUIERDA);
    }

    @Test
    void deberiaEmpezarMirandoAlCentroYEnElPiso() {
        assertEquals(1, jugador.getMirando());
        assertTrue(jugador.estaEnElPiso());
        assertEquals(0, jugador.getPoder(), 0.001);
        assertFalse(jugador.isPoderActivo());
    }

    @Test
    void deberiaCaminarHaciaLaDerecha() {
        double antes = jugador.getX();
        jugador.mover(1);
        jugador.actualizar();
        assertTrue(jugador.getX() > antes);
    }

    @Test
    void noDeberiaSalirseDeLaCancha() {
        jugador.mover(-1);
        for (int i = 0; i < 400; i++) {
            jugador.actualizar();
        }
        assertTrue(jugador.getX() >= Cancha.PORTERIA_ANCHO);
    }

    @Test
    void deberiaSaltarSoloSiEstaEnElPiso() {
        jugador.saltar();
        jugador.actualizar();
        assertFalse(jugador.estaEnElPiso());
        double alturaEnElAire = jugador.getY();
        jugador.saltar();            // en el aire no debe volver a impulsarse
        jugador.actualizar();
        assertTrue(jugador.getVy() > -17 || jugador.getY() <= alturaEnElAire);
    }

    @Test
    void deberiaVolverAlPisoDespuesDeSaltar() {
        jugador.saltar();
        for (int i = 0; i < 120; i++) {
            jugador.actualizar();
        }
        assertTrue(jugador.estaEnElPiso());
    }

    @Test
    void laPatadaDeberiaDurarUnRatoYTerminar() {
        jugador.patear();
        jugador.actualizar();
        assertTrue(jugador.estaPateando());
        for (int i = 0; i < Jugador.DURACION_PATADA + 2; i++) {
            jugador.actualizar();
        }
        assertFalse(jugador.estaPateando());
        assertEquals(0, jugador.getProgresoPatada(), 0.001);
    }

    @Test
    void laBarraDePoderDeberiaLlenarseEnDiezSegundos() {
        for (int i = 0; i < Jugador.TICKS_CARGA; i++) {
            jugador.actualizar();
        }
        assertEquals(Jugador.PODER_MAXIMO, jugador.getPoder(), 0.5);
    }

    @Test
    void recibirUnGolDeberiaSubirUnCuartoDeBarra() {
        jugador.recibirGol();
        assertEquals(25, jugador.getPoder(), 0.001);
        for (int i = 0; i < 10; i++) {
            jugador.recibirGol();
        }
        assertEquals(Jugador.PODER_MAXIMO, jugador.getPoder(), 0.001);
    }

    @Test
    void noDeberiaActivarElPoderConLaBarraIncompleta() {
        jugador.cargarPoder(50);
        assertFalse(jugador.activarPoder());
        assertFalse(jugador.isPoderActivo());
    }

    @Test
    void deberiaActivarElPoderConLaBarraLlenaYVaciarla() {
        jugador.cargarPoder(100);
        assertTrue(jugador.activarPoder());
        assertTrue(jugador.isPoderActivo());
        assertEquals(0, jugador.getPoder(), 0.001);
        jugador.cargarPoder(100);
        assertFalse(jugador.activarPoder(), "no puede activarlo dos veces seguidas");
    }

    @Test
    void lasEstadisticasDeberianCalcularSusMultiplicadores() throws DOPOSoccerExcepcion {
        jugador.setKick(6);
        jugador.setSpeed(3);
        jugador.setJump(10);
        jugador.setSurvival(2);
        assertEquals(1.5, jugador.getMultiplicadorKick(), 0.0001);
        assertEquals(1.2, jugador.getMultiplicadorVelocidad(), 0.0001);
        assertEquals(1.9, jugador.getMultiplicadorSalto(), 0.0001);
        assertEquals(110, jugador.getVidaMaxima(), 0.0001);
    }

    @Test
    void noDeberiaAceptarEstadisticasFueraDeRango() {
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class, () -> jugador.setKick(0));
        assertEquals(DOPOSoccerExcepcion.ESTADISTICA_INVALIDA, e.getMessage());
        assertThrows(DOPOSoccerExcepcion.class, () -> jugador.setSpeed(11));
        assertThrows(DOPOSoccerExcepcion.class, () -> jugador.setJump(-3));
        assertThrows(DOPOSoccerExcepcion.class, () -> jugador.setSurvival(100));
    }

    @Test
    void prepararPartidoDeberiaVaciarLaBarraPeroConservarLosPuntos() {
        jugador.sumarPuntos(100);
        jugador.cargarPoder(80);
        jugador.prepararPartido(new Nigeria());
        assertEquals(0, jugador.getPoder(), 0.001);
        assertEquals(100, jugador.getPuntos());
        assertEquals("NIGERIA", jugador.getPersonaje().getCodigo());
    }

    @Test
    void laCabezaDeberiaHacerRebotarElBalon() {
        Balon b = new Balon();
        b.setPosicion(jugador.getX(), jugador.getY() - Jugador.RADIO_CABEZA - Balon.RADIO + 5);
        b.setVelocidad(0, 6);
        assertTrue(jugador.resolverContacto(b));
        assertTrue(b.getVy() < 0);
    }

    @Test
    void laPatadaDeberiaMandarElBalonHaciaElArcoRival() {
        Balon b = new Balon();
        jugador.patear();
        for (int i = 0; i < 6; i++) {
            jugador.actualizar();
        }
        b.setPosicion(jugador.getPieX() + 18, jugador.getPieY());
        b.setVelocidad(0, 0);
        assertTrue(jugador.resolverContacto(b));
        assertTrue(b.getVx() > 3, "el jugador 1 patea hacia la derecha");
    }

    @Test
    void sinContactoNoDeberiaTocarElBalon() {
        Balon b = new Balon();
        b.setPosicion(900, 100);
        assertFalse(jugador.resolverContacto(b));
    }
}
