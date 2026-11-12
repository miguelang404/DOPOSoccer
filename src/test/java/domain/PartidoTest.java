package domain;

import exceptions.DOPOSoccerExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.RegistroErrores;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del partido: validaciones, cuenta atrás, goles, tipos de partido,
 * muerte súbita, pausa y puntos.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class PartidoTest {

    private Jugador j1;
    private Jugador j2;
    private Cancha cancha;

    @BeforeEach
    void preparar() throws DOPOSoccerExcepcion {
        RegistroErrores.setActivo(false);
        j1 = new Jugador("Jugador 1", new CoreaDelSur(), Porteria.IZQUIERDA);
        j2 = new Jugador("Jugador 2", new Camerun(), Porteria.DERECHA);
        cancha = new Cancha(Cancha.MEDIANA);
    }

    private Partido crear(String tipo, int valor) throws DOPOSoccerExcepcion {
        return new Partido(j1, j2, tipo, valor, cancha);
    }

    /**
     * Avanza la cuenta atrás hasta que el partido arranca.
     */
    private void pasarLaCuenta(Partido p) {
        for (int i = 0; i < Partido.TICKS_CUENTA + 2; i++) {
            p.actualizar();
        }
    }

    // creación

    @Test
    void deberiaCrearUnPartidoPorTiempo() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 3);
        assertEquals(Partido.ESTADO_CUENTA, p.getEstado());
        assertEquals(180, p.getSegundosRestantes());
        assertEquals(0, p.getMarcador().getGolesJugador1());
    }

    @Test
    void deberiaCrearUnPartidoPorGoles() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_GOLES, 5);
        assertEquals(5, p.getGolesParaGanar());
    }

    @Test
    void noDeberiaAceptarMinutosFueraDeRango() {
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class, () -> crear(Partido.TIPO_TIEMPO, 0));
        assertEquals(DOPOSoccerExcepcion.MINUTOS_INVALIDOS, e.getMessage());
        assertThrows(DOPOSoccerExcepcion.class, () -> crear(Partido.TIPO_TIEMPO, 11));
    }

    @Test
    void noDeberiaAceptarGolesFueraDeRango() {
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class, () -> crear(Partido.TIPO_GOLES, 0));
        assertEquals(DOPOSoccerExcepcion.GOLES_INVALIDOS, e.getMessage());
        assertThrows(DOPOSoccerExcepcion.class, () -> crear(Partido.TIPO_GOLES, 21));
    }

    @Test
    void noDeberiaAceptarUnTipoDesconocido() {
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class, () -> crear("RAPIDO", 3));
        assertEquals(DOPOSoccerExcepcion.TIPO_PARTIDO_INVALIDO, e.getMessage());
    }

    @Test
    void noDeberiaCrearseSinJugadoresNiCancha() {
        assertThrows(DOPOSoccerExcepcion.class, () -> new Partido(null, j2, Partido.TIPO_TIEMPO, 3, cancha));
        assertThrows(DOPOSoccerExcepcion.class, () -> new Partido(j1, j2, Partido.TIPO_TIEMPO, 3, null));
    }

    // cuenta atrás

    @Test
    void deberiaContarTresDosUnoYEmpezar() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 1);
        assertEquals(3, p.getNumeroCuenta());
        for (int i = 0; i < Partido.TICKS_POR_SEGUNDO + 1; i++) {
            p.actualizar();
        }
        assertEquals(2, p.getNumeroCuenta());
        pasarLaCuenta(p);
        assertEquals(Partido.ESTADO_EN_CURSO, p.getEstado());
    }

    @Test
    void duranteLaCuentaLosJugadoresNoDeberianMoverse() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 1);
        double x = j1.getX();
        j1.mover(1);
        p.actualizar();
        assertEquals(x, j1.getX(), 0.0001);
    }

    // goles

    @Test
    void unGolDeberiaSumarAlAnotadorYCargarElPoderDelRival() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 3);
        pasarLaCuenta(p);
        p.registrarGol(p.getPorteriaDerecha());   // entra en el arco del jugador 2
        assertEquals(1, p.getMarcador().getGoles(j1));
        assertEquals(0, p.getMarcador().getGoles(j2));
        assertEquals(Jugador.CARGA_POR_GOL, j2.getPoder(), 1.0);
        assertSame(j1, p.getUltimoAnotador());
        assertEquals(Partido.ESTADO_GOL, p.getEstado());
    }

    @Test
    void despuesDeLaCelebracionDeberiaVolverACentrarTodo() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 3);
        pasarLaCuenta(p);
        p.getBalon().setPosicion(100, 100);
        p.registrarGol(p.getPorteriaIzquierda());
        for (int i = 0; i < Partido.TICKS_CELEBRACION + 2; i++) {
            p.actualizar();
        }
        assertEquals(Partido.ESTADO_EN_CURSO, p.getEstado());
        assertEquals(Cancha.ANCHO / 2.0, p.getBalon().getX(), 40);
    }

    @Test
    void enPorGolesGanaQuienLlegaPrimeroALaMeta() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_GOLES, 2);
        pasarLaCuenta(p);
        p.registrarGol(p.getPorteriaDerecha());
        for (int i = 0; i < Partido.TICKS_CELEBRACION + 2; i++) {
            p.actualizar();
        }
        assertEquals(Partido.ESTADO_EN_CURSO, p.getEstado());
        p.registrarGol(p.getPorteriaDerecha());
        for (int i = 0; i < Partido.TICKS_CELEBRACION + 2; i++) {
            p.actualizar();
        }
        assertEquals(Partido.ESTADO_TERMINADO, p.getEstado());
        assertSame(j1, p.getGanador());
    }

    @Test
    void elGanadorDeberiaRecibir100PuntosYElPerdedor25() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_GOLES, 1);
        pasarLaCuenta(p);
        p.registrarGol(p.getPorteriaIzquierda());   // gana el jugador 2
        for (int i = 0; i < Partido.TICKS_CELEBRACION + 2; i++) {
            p.actualizar();
        }
        assertSame(j2, p.getGanador());
        assertEquals(Partido.PUNTOS_GANADOR, p.getPuntosGanados(j2));
        assertEquals(Partido.PUNTOS_PERDEDOR, p.getPuntosGanados(j1));
        assertEquals(100, j2.getPuntos());
        assertEquals(25, j1.getPuntos());
    }

    // tiempo

    @Test
    void elRelojDeberiaBajarMientrasSeJuega() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 1);
        pasarLaCuenta(p);
        int antes = p.getSegundosRestantes();
        for (int i = 0; i < 3 * Partido.TICKS_POR_SEGUNDO; i++) {
            p.actualizar();
        }
        assertTrue(p.getSegundosRestantes() < antes);
    }

    @Test
    void alAcabarseElTiempoGanaQuienVayaAdelante() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 1);
        pasarLaCuenta(p);
        p.getMarcador().sumarGol(j2);
        for (int i = 0; i < 61 * Partido.TICKS_POR_SEGUNDO && !Partido.ESTADO_TERMINADO.equals(p.getEstado()); i++) {
            p.actualizar();
        }
        assertEquals(Partido.ESTADO_TERMINADO, p.getEstado());
        assertSame(j2, p.getGanador());
    }

    @Test
    void conEmpateDeberiaHaberMuerteSubitaYGanaElSiguienteGol() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 1);
        pasarLaCuenta(p);
        for (int i = 0; i < 61 * Partido.TICKS_POR_SEGUNDO && !p.isMuerteSubita(); i++) {
            p.actualizar();
        }
        assertTrue(p.isMuerteSubita());
        assertEquals(Partido.ESTADO_EN_CURSO, p.getEstado());
        p.registrarGol(p.getPorteriaDerecha());
        for (int i = 0; i < Partido.TICKS_CELEBRACION + 2; i++) {
            p.actualizar();
        }
        assertEquals(Partido.ESTADO_TERMINADO, p.getEstado());
        assertSame(j1, p.getGanador());
    }

    // pausa

    @Test
    void pausarDeberiaCongelarElJuegoYReanudarDeberiaSeguir() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 2);
        pasarLaCuenta(p);
        p.pausar();
        assertEquals(Partido.ESTADO_PAUSADO, p.getEstado());
        int segundos = p.getSegundosRestantes();
        double y = p.getBalon().getY();
        for (int i = 0; i < 200; i++) {
            p.actualizar();
        }
        assertEquals(segundos, p.getSegundosRestantes());
        assertEquals(y, p.getBalon().getY(), 0.0001);
        p.reanudar();
        assertEquals(Partido.ESTADO_EN_CURSO, p.getEstado());
    }

    @Test
    void abandonarNoDeberiaDarGanadorNiPuntos() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 2);
        pasarLaCuenta(p);
        p.abandonar();
        assertEquals(Partido.ESTADO_ABANDONADO, p.getEstado());
        assertNull(p.getGanador());
        assertEquals(0, j1.getPuntos());
    }

    // poderes

    @Test
    void conElPoderActivoElSiguienteToqueDeberiaLanzarElTiroDePoder() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 2);
        pasarLaCuenta(p);
        j1.cargarPoder(100);
        assertTrue(j1.activarPoder());
        p.getBalon().setPosicion(j1.getX() + 2, j1.getY() - Jugador.RADIO_CABEZA - Balon.RADIO + 3);
        p.getBalon().setVelocidad(0, 3);
        p.actualizar();
        assertSame(j1, p.getJugadorCargando(), "primero el balón se pone delante de la frente");
        assertTrue(j1.isPoderActivo(), "el aura sigue hasta que sale el tiro");
        for (int i = 0; i < Partido.TICKS_CARGA_PODER; i++) {
            p.actualizar();
        }
        assertNull(p.getJugadorCargando());
        assertFalse(j1.isPoderActivo(), "el aura se gasta al lanzar");
        assertEquals("AURA_AZUL", p.getBalon().getEfecto());
        assertTrue(p.getBalon().getVx() > 10);
    }

    @Test
    void ilusionDeberiaCrearSieteBalonesFalsos() throws DOPOSoccerExcepcion {
        Jugador usa = new Jugador("Jugador 1", new EstadosUnidos(), Porteria.IZQUIERDA);
        Partido p = new Partido(usa, j2, Partido.TIPO_TIEMPO, 2, cancha);
        pasarLaCuenta(p);
        usa.cargarPoder(100);
        usa.activarPoder();
        p.getBalon().setPosicion(usa.getX(), usa.getY() - Jugador.RADIO_CABEZA - Balon.RADIO + 3);
        p.getBalon().setVelocidad(0, 3);
        for (int i = 0; i < Partido.TICKS_CARGA_PODER + 2; i++) {
            p.actualizar();
        }
        assertEquals(7, p.getSeñuelos().size());
        assertNotNull(p.getBalon());
    }

    @Test
    void duranteLaCargaElBalonDeberiaEstarDelanteDeLaFrente() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 2);
        pasarLaCuenta(p);
        j1.cargarPoder(100);
        j1.activarPoder();
        p.getBalon().setPosicion(j1.getX() + 30, j1.getY() - 60);
        p.getBalon().setVelocidad(0, 0);
        p.actualizar();
        for (int i = 0; i < 25; i++) {
            p.actualizar();
        }
        assertSame(j1, p.getJugadorCargando());
        assertEquals(j1.getX() + j1.getMirando() * (Jugador.RADIO_CABEZA + Balon.RADIO + 4), p.getBalon().getX(), 3);
        assertEquals(j1.getY() - 8, p.getBalon().getY(), 3);
    }

    @Test
    void unGolDeberiaAcabarElPoderDelBalon() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 2);
        pasarLaCuenta(p);
        p.getBalon().setEfecto("RAYO", 100);
        p.registrarGol(p.getPorteriaDerecha());
        assertEquals("", p.getBalon().getEfecto());
    }

    @Test
    void chocarConElRivalDeberiaAcabarElPoderDelBalon() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 2);
        pasarLaCuenta(p);
        p.getBalon().setEfecto("RAYO", 100);
        p.getBalon().setPosicion(j2.getX(), j2.getY() - Jugador.RADIO_CABEZA - Balon.RADIO + 3);
        p.getBalon().setVelocidad(0, 0);
        p.actualizar();
        assertEquals("", p.getBalon().getEfecto());
    }

    // simulación

    @Test
    void unPartidoLargoSinTocarNadaNoDeberiaRomperse() throws DOPOSoccerExcepcion {
        Partido p = crear(Partido.TIPO_TIEMPO, 1);
        for (int i = 0; i < 70 * Partido.TICKS_POR_SEGUNDO; i++) {
            p.actualizar();
            assertTrue(p.getBalon().getX() >= 0 && p.getBalon().getX() <= Cancha.ANCHO);
        }
        assertTrue(p.isMuerteSubita() || Partido.ESTADO_TERMINADO.equals(p.getEstado()));
    }
}
