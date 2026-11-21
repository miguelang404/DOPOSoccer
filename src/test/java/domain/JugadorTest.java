package domain;

import exceptions.DOPOSoccerExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del jugador: movimiento, salto, patada, barra de poder y estadísticas.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
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

    // vida, daño y aturdimiento

    @Test
    void laVidaInicialDeberiaSerLaMaximaSegunSurvival() throws DOPOSoccerExcepcion {
        assertEquals(100, jugador.getVida(), 0.001);
        jugador.setSurvival(3);
        jugador.prepararPartido(new CoreaDelSur());
        assertEquals(120, jugador.getVida(), 0.001);
        assertEquals(120, jugador.getVidaMaxima(), 0.001);
    }

    @Test
    void recibirDañoDeberiaRestarVidaSinBajarDeCero() {
        jugador.recibirDaño(30);
        assertEquals(70, jugador.getVida(), 0.001);
        jugador.recibirDaño(500);
        assertEquals(0, jugador.getVida(), 0.001);
    }

    @Test
    void conLaVidaEnCeroDeberiaQuedarAturdidoTresSegundosYRecuperarLaVida() {
        jugador.recibirDaño(100);
        assertTrue(jugador.estaAturdido());
        for (int i = 0; i < Jugador.TICKS_ATURDIMIENTO - 1; i++) {
            jugador.actualizar();
        }
        assertTrue(jugador.estaAturdido());
        jugador.actualizar();
        assertFalse(jugador.estaAturdido());
        assertEquals(100, jugador.getVida(), 0.001);
    }

    @Test
    void aturdidoNoDeberiaMoverseSaltarNiPatear() {
        jugador.recibirDaño(100);
        double antes = jugador.getX();
        jugador.mover(1);
        jugador.saltar();
        jugador.patear();
        jugador.actualizar();
        assertEquals(antes, jugador.getX(), 0.001);
        assertTrue(jugador.estaEnElPiso());
        assertFalse(jugador.estaPateando());
    }

    @Test
    void aturdidoNoDeberiaRecibirMasDañoNiEfectos() {
        jugador.recibirDaño(100);
        jugador.recibirDaño(10);
        jugador.aplicarEfecto(Jugador.EFECTO_ELECTROCUTADO, 100);
        assertEquals(0, jugador.getVida(), 0.001);
        assertEquals(Jugador.EFECTO_NINGUNO, jugador.getEfecto());
    }

    @Test
    void electrocutadoNoDeberiaSaltarYCaminaALaMitad() {
        Jugador normal = new Jugador("N", new Camerun(), Porteria.IZQUIERDA);
        jugador.aplicarEfecto(Jugador.EFECTO_ELECTROCUTADO, 100);
        jugador.mover(1);
        jugador.saltar();
        normal.mover(1);
        double inicio = jugador.getX();
        jugador.actualizar();
        normal.actualizar();
        assertEquals((normal.getX() - inicio) / 2, jugador.getX() - inicio, 0.001);
        assertTrue(jugador.estaEnElPiso(), "electrocutado no puede saltar");
    }

    @Test
    void inconscienteNoDeberiaMoverseSaltarNiPatear() {
        jugador.aplicarEfecto(Jugador.EFECTO_INCONSCIENTE, 100);
        double antes = jugador.getX();
        jugador.mover(1);
        jugador.saltar();
        jugador.patear();
        jugador.actualizar();
        assertEquals(antes, jugador.getX(), 0.001);
        assertFalse(jugador.estaPateando());
    }

    @Test
    void empujadoDeberiaIrHaciaSuPropioArco() {
        jugador.empujar(100);   // el jugador izquierdo defiende el arco de la izquierda
        double antes = jugador.getX();
        jugador.aplicarEfecto(Jugador.EFECTO_EMPUJADO, 90);
        jugador.mover(1);       // aunque pida ir a la derecha, lo empujan al otro lado
        jugador.actualizar();
        assertTrue(jugador.getX() < antes);
    }

    @Test
    void unEfectoNuevoDeberiaReemplazarAlAnterior() {
        jugador.aplicarEfecto(Jugador.EFECTO_ELECTROCUTADO, 100);
        jugador.aplicarEfecto(Jugador.EFECTO_CONFUNDIDO, 50);
        assertEquals(Jugador.EFECTO_CONFUNDIDO, jugador.getEfecto());
        assertEquals(50, jugador.getTicksEfecto());
    }

    @Test
    void elEfectoDeberiaTerminarCuandoSeAcabaSuTiempo() {
        jugador.aplicarEfecto(Jugador.EFECTO_CONFUNDIDO, 3);
        for (int i = 0; i < 3; i++) {
            jugador.actualizar();
        }
        assertEquals(Jugador.EFECTO_NINGUNO, jugador.getEfecto());
    }

    @Test
    void unaPatadaQueLeLlegaAlRivalDeberiaHacerleDañoUnaSolaVez() throws DOPOSoccerExcepcion {
        Jugador rival = new Jugador("Rival", new Camerun(), Porteria.DERECHA);
        jugador.setKick(5);
        jugador.empujar(455);   // queda a 45 de distancia del rival
        jugador.patear();
        for (int i = 0; i < Jugador.DURACION_PATADA + 2; i++) {
            jugador.actualizar();
            jugador.golpearConPie(rival);
        }
        assertEquals(100 - 10 * 1.4, rival.getVida(), 0.001);
    }

    @Test
    void unaPatadaQueNoLlegaDeberiaDejarLaVidaIgual() {
        Jugador rival = new Jugador("Rival", new Camerun(), Porteria.DERECHA);
        jugador.patear();
        for (int i = 0; i < Jugador.DURACION_PATADA + 2; i++) {
            jugador.actualizar();
            assertFalse(jugador.golpearConPie(rival));
        }
        assertEquals(100, rival.getVida(), 0.001);
    }

    // mejoras con puntos

    @Test
    void elCostoDeLasMejorasDeberiaDuplicarseDesde500() throws DOPOSoccerExcepcion {
        assertEquals(500, jugador.getCostoMejora(Jugador.KICK));
        jugador.sumarPuntos(3500);
        jugador.mejorar(Jugador.KICK);
        assertEquals(1000, jugador.getCostoMejora(Jugador.KICK));
        jugador.mejorar(Jugador.KICK);
        assertEquals(2000, jugador.getCostoMejora(Jugador.KICK));
        assertEquals(3, jugador.getKick());
        assertEquals(2000, jugador.getPuntos());
        // las otras estadísticas siguen costando 500
        assertEquals(500, jugador.getCostoMejora(Jugador.SPEED));
    }

    @Test
    void mejorarDeberiaSubirLaEstadisticaYGastarLosPuntos() throws DOPOSoccerExcepcion {
        jugador.sumarPuntos(600);
        jugador.mejorar(Jugador.SURVIVAL);
        assertEquals(2, jugador.getSurvival());
        assertEquals(100, jugador.getPuntos());
        assertEquals(110, jugador.getVidaMaxima(), 0.001);
    }

    @Test
    void mejorarSinPuntosSuficientesDeberiaLanzarExcepcion() {
        jugador.sumarPuntos(499);
        assertFalse(jugador.puedeMejorar(Jugador.SPEED));
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class, () -> jugador.mejorar(Jugador.SPEED));
        assertEquals(DOPOSoccerExcepcion.PUNTOS_INSUFICIENTES, e.getMessage());
        assertEquals(1, jugador.getSpeed());
        assertEquals(499, jugador.getPuntos());
    }

    @Test
    void mejorarUnaEstadisticaEnSuMaximoDeberiaLanzarExcepcion() throws DOPOSoccerExcepcion {
        jugador.setJump(10);
        jugador.sumarPuntos(1000000);
        assertEquals(-1, jugador.getCostoMejora(Jugador.JUMP));
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class, () -> jugador.mejorar(Jugador.JUMP));
        assertEquals(DOPOSoccerExcepcion.ESTADISTICA_AL_MAXIMO, e.getMessage());
    }

    @Test
    void unaEstadisticaDesconocidaDeberiaLanzarExcepcion() {
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class, () -> jugador.getNivel("MAGIA"));
        assertEquals(DOPOSoccerExcepcion.ESTADISTICA_DESCONOCIDA, e.getMessage());
        assertFalse(jugador.puedeMejorar("MAGIA"));
    }
}
