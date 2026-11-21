package domain;

import exceptions.DOPOSoccerExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.RegistroErrores;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la puerta de entrada al dominio: Juego y Configuracion.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
class JuegoTest {

    private Juego juego;

    @BeforeEach
    void preparar() {
        RegistroErrores.setActivo(false);
        juego = new Juego();
    }

    @Test
    void deberiaTenerLosCuatroPersonajesDelAnexo() {
        Personaje[] lista = juego.getPersonajes();
        assertEquals(4, lista.length);
        assertEquals("Corea del Sur", lista[0].getPais());
        assertEquals("Camerún", lista[1].getPais());
        assertEquals("Nigeria", lista[2].getPais());
        assertEquals("Estados Unidos", lista[3].getPais());
    }

    @Test
    void cadaPersonajeDeberiaTenerSuPoder() {
        String[] poderes = {"Aura Azul", "Rayo", "Tornado", "Ilusión"};
        Personaje[] lista = juego.getPersonajes();
        for (int i = 0; i < lista.length; i++) {
            assertEquals(poderes[i], lista[i].getPoder().getNombre());
            assertTrue(lista[i].getPoder().getDañoTiro() > 0);
        }
    }

    @Test
    void getPersonajesDeberiaEntregarUnaCopia() {
        Personaje[] copia = juego.getPersonajes();
        copia[0] = null;
        assertNotNull(juego.getPersonajes()[0]);
    }

    @Test
    void deberiaAceptarJugadorVsJugador() throws DOPOSoccerExcepcion {
        juego.seleccionarModalidad(Juego.JUGADOR_VS_JUGADOR);
        assertEquals(Juego.JUGADOR_VS_JUGADOR, juego.getModalidad());
    }

    @Test
    void deberiaRechazarUnaModalidadQueNoExiste() {
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class, () -> juego.seleccionarModalidad("OTRA"));
        assertEquals(DOPOSoccerExcepcion.MODALIDAD_NO_DISPONIBLE, e.getMessage());
    }

    @Test
    void deberiaGuardarLaConfiguracion() throws DOPOSoccerExcepcion {
        juego.configurar(false, true, Configuracion.INGLES);
        Configuracion c = juego.getConfiguracion();
        assertEquals(false, c.isSonidoActivo());
        assertEquals(true, c.isMusicaActiva());
        assertEquals(Configuracion.INGLES, c.getIdioma());
    }

    @Test
    void noDeberiaAceptarUnIdiomaQueNoExiste() {
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class,
                () -> juego.configurar(true, true, "Klingon"));
        assertEquals(DOPOSoccerExcepcion.IDIOMA_INVALIDO, e.getMessage());
    }

    @Test
    void laConfiguracionDeFabricaDeberiaSerEspañolConTodoEncendido() {
        Configuracion c = new Configuracion();
        assertEquals(Configuracion.ESPAÑOL, c.getIdioma());
        assertTrue(c.isSonidoActivo() && c.isMusicaActiva());
        assertEquals(Cancha.MEDIANA, c.getResolucion());
    }

    @Test
    void noDeberiaAceptarUnaResolucionInvalida() {
        assertThrows(DOPOSoccerExcepcion.class, () -> new Configuracion().setResolucion(5));
    }

    @Test
    void deberiaIniciarUnPartidoConLosPersonajesElegidos() throws DOPOSoccerExcepcion {
        Personaje[] l = juego.getPersonajes();
        Partido p = juego.iniciarPartido(l[2], l[3], Partido.TIPO_GOLES, 3, Cancha.GRANDE);
        assertSame(p, juego.getPartido());
        assertSame(l[2], juego.getJugador1().getPersonaje());
        assertSame(l[3], juego.getJugador2().getPersonaje());
        assertEquals(Cancha.GRANDE, juego.getConfiguracion().getResolucion());
        assertEquals(Cancha.GRANDE, p.getCancha().getResolucion());
    }

    @Test
    void noDeberiaIniciarSinPersonajes() {
        Personaje a = juego.getPersonajes()[0];
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class,
                () -> juego.iniciarPartido(a, null, Partido.TIPO_TIEMPO, 3, Cancha.MEDIANA));
        assertEquals(DOPOSoccerExcepcion.PERSONAJE_VACIO, e.getMessage());
    }

    @Test
    void dosPartidosSeguidosDeberianSerObjetosDistintosPeroConservarLosPuntos() throws DOPOSoccerExcepcion {
        Personaje[] l = juego.getPersonajes();
        Partido primero = juego.iniciarPartido(l[0], l[1], Partido.TIPO_GOLES, 1, Cancha.MEDIANA);
        for (int i = 0; i < Partido.TICKS_CUENTA + 2; i++) {
            primero.actualizar();
        }
        primero.registrarGol(primero.getPorteriaDerecha());
        for (int i = 0; i < Partido.TICKS_CELEBRACION + 2; i++) {
            primero.actualizar();
        }
        Partido segundo = juego.iniciarPartido(l[0], l[1], Partido.TIPO_GOLES, 1, Cancha.MEDIANA);
        assertNotSame(primero, segundo);
        assertEquals(100, juego.getJugador1().getPuntos());
        assertEquals(0, juego.getJugador1().getPoder(), 0.001);
        assertEquals(0, segundo.getMarcador().getGolesJugador1());
    }

    @Test
    void elLogDeErroresDeberiaEscribirseCuandoEstaActivo() throws Exception {
        java.nio.file.Path temporal = java.nio.file.Files.createTempFile("doposoccer", ".log");
        RegistroErrores.setArchivo(temporal.toString());
        RegistroErrores.setActivo(true);
        try {
            assertThrows(DOPOSoccerExcepcion.class, () -> juego.configurar(true, true, "Klingon"));
            String contenido = new String(java.nio.file.Files.readAllBytes(temporal), "UTF-8");
            assertTrue(contenido.contains("DOPOSoccerExcepcion"));
        } finally {
            RegistroErrores.setActivo(false);
            java.nio.file.Files.deleteIfExists(temporal);
            RegistroErrores.setArchivo("logs/errores.log");
        }
    }

    @Test
    void lospuntosYLasMejorasDeberianConservarseEntrePartidos() throws DOPOSoccerExcepcion {
        Juego juego = new Juego();
        Personaje[] lista = juego.getPersonajes();
        juego.iniciarPartido(lista[0], lista[1], Partido.TIPO_GOLES, 1, Cancha.MEDIANA);
        juego.getJugador1().sumarPuntos(600);
        juego.getJugador1().mejorar(Jugador.KICK);
        juego.iniciarPartido(lista[2], lista[3], Partido.TIPO_GOLES, 1, Cancha.MEDIANA);
        assertEquals(2, juego.getJugador1().getKick());
        assertEquals(100, juego.getJugador1().getPuntos());
        assertEquals(juego.getJugador1().getVidaMaxima(), juego.getJugador1().getVida(), 0.001);
    }
}
