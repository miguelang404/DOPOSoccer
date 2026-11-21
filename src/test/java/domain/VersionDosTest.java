package domain;

import exceptions.DOPOSoccerExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.RegistroErrores;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la versión 2: jugador contra máquina, guardar y abrir partidas y progreso.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
class VersionDosTest {

    private Juego juego;

    @BeforeEach
    void preparar() {
        RegistroErrores.setActivo(false);
        juego = new Juego();
    }

    private Partido partidoContraMaquina(String perfil) throws DOPOSoccerExcepcion {
        juego.seleccionarModalidad(Juego.JUGADOR_VS_MAQUINA);
        juego.seleccionarPerfil(perfil);
        Personaje[] p = juego.getPersonajes();
        Partido partido = juego.iniciarPartido(p[0], p[1], Partido.TIPO_TIEMPO, 3, Cancha.MEDIANA);
        return partido;
    }

    private void correr(Partido partido, int ticks) {
        for (int i = 0; i < ticks; i++) {
            partido.actualizar();
        }
    }

    @Test
    void deberiaOfrecerLosCuatroPerfiles() {
        assertEquals(4, juego.getPerfiles().length);
    }

    @Test
    void deberiaCrearElPartidoConLaMaquinaDelPerfilElegido() throws DOPOSoccerExcepcion {
        for (String perfil : juego.getPerfiles()) {
            Partido partido = partidoContraMaquina(perfil);
            assertNotNull(partido.getMaquina());
            assertEquals(perfil, partido.getMaquina().getPerfil());
        }
    }

    @Test
    void noDeberiaCrearMaquinaEnJugadorContraJugador() throws DOPOSoccerExcepcion {
        Personaje[] p = juego.getPersonajes();
        Partido partido = juego.iniciarPartido(p[0], p[1], Partido.TIPO_TIEMPO, 3, Cancha.MEDIANA);
        assertNull(partido.getMaquina());
    }

    @Test
    void deberiaRechazarUnPerfilQueNoExiste() {
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class, () -> juego.seleccionarPerfil("Loca"));
        assertEquals(DOPOSoccerExcepcion.PERFIL_INVALIDO, e.getMessage());
    }

    @Test
    void deberiaExigirPerfilAntesDeJugarContraLaMaquina() throws DOPOSoccerExcepcion {
        juego.seleccionarModalidad(Juego.JUGADOR_VS_MAQUINA);
        Personaje[] p = juego.getPersonajes();
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class,
                () -> juego.iniciarPartido(p[0], p[1], Partido.TIPO_TIEMPO, 3, Cancha.MEDIANA));
        assertEquals(DOPOSoccerExcepcion.SIN_PERFIL, e.getMessage());
    }

    @Test
    void cadaPerfilDeberiaJugarUnPartidoCompletoSinErrores() throws DOPOSoccerExcepcion {
        for (String perfil : juego.getPerfiles()) {
            Partido partido = partidoContraMaquina(perfil);
            correr(partido, 60 * 60);
            Jugador m = partido.getJugador2();
            assertTrue(m.getX() >= 0 && m.getX() <= Cancha.ANCHO);
            assertTrue(m.getVida() >= 0 && m.getVida() <= m.getVidaMaxima());
        }
    }

    @Test
    void laMaquinaDeberiaMoverseSola() throws DOPOSoccerExcepcion {
        Partido partido = partidoContraMaquina(Maquina.OFENSIVA);
        double inicio = partido.getJugador2().getX();
        correr(partido, 60 * 20);
        assertTrue(Math.abs(inicio - partido.getJugador2().getX()) > 0.001);
    }

    @Test
    void laMaquinaDefensivaDeberiaEstarMasCercaDeSuArcoQueLaOfensiva() throws DOPOSoccerExcepcion {
        double defensiva = promedioDistanciaAlArco(Maquina.DEFENSIVA);
        double ofensiva = promedioDistanciaAlArco(Maquina.OFENSIVA);
        assertTrue(defensiva < ofensiva, "defensiva=" + defensiva + " ofensiva=" + ofensiva);
    }

    private double promedioDistanciaAlArco(String perfil) throws DOPOSoccerExcepcion {
        Juego otro = new Juego();
        otro.seleccionarModalidad(Juego.JUGADOR_VS_MAQUINA);
        otro.seleccionarPerfil(perfil);
        Personaje[] p = otro.getPersonajes();
        Partido partido = otro.iniciarPartido(p[0], p[1], Partido.TIPO_TIEMPO, 3, Cancha.MEDIANA);
        double suma = 0;
        int n = 0;
        for (int i = 0; i < 60 * 40; i++) {
            partido.actualizar();
            if (Partido.ESTADO_EN_CURSO.equals(partido.getEstado())) {
                suma += Math.abs(Cancha.ANCHO - partido.getJugador2().getX());
                n++;
            }
        }
        return suma / Math.max(1, n);
    }

    @Test
    void deberiaGuardarYAbrirUnaPartida() throws DOPOSoccerExcepcion, IOException {
        Partido partido = partidoContraMaquina(Maquina.EXPERTA);
        correr(partido, 60 * 8);
        partido.pausar();
        Path temporal = Files.createTempFile("partida", ".dop");
        try {
            juego.guardarPartida(temporal.toFile());
            Juego otro = new Juego();
            Partido abierto = otro.abrirPartida(temporal.toFile());
            assertEquals(partido.getEstado(), abierto.getEstado());
            assertEquals(partido.getJugador1().getX(), abierto.getJugador1().getX(), 0.0001);
            assertEquals(partido.getBalon().getX(), abierto.getBalon().getX(), 0.0001);
            assertEquals(partido.getMarcador().getGoles(partido.getJugador1()),
                    abierto.getMarcador().getGoles(abierto.getJugador1()));
            assertNotNull(abierto.getMaquina());
            assertEquals(Maquina.EXPERTA, abierto.getMaquina().getPerfil());
            assertEquals(Juego.JUGADOR_VS_MAQUINA, otro.getModalidad());
            abierto.reanudar();
            correr(abierto, 60 * 5);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    @Test
    void noDeberiaGuardarSiNoHayPartido() {
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class,
                () -> juego.guardarPartida(new File("no_importa.dop")));
        assertEquals(DOPOSoccerExcepcion.SIN_PARTIDO_PARA_GUARDAR, e.getMessage());
    }

    @Test
    void noDeberiaAbrirUnArchivoQueNoExiste() {
        DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class,
                () -> juego.abrirPartida(new File("no_existe_para_nada.dop")));
        assertEquals(DOPOSoccerExcepcion.NO_SE_PUDO_ABRIR, e.getMessage());
    }

    @Test
    void noDeberiaAbrirUnArchivoQueNoEsUnaPartida() throws IOException {
        Path temporal = Files.createTempFile("falsa", ".dop");
        try {
            Files.write(temporal, "esto no es una partida".getBytes("UTF-8"));
            DOPOSoccerExcepcion e = assertThrows(DOPOSoccerExcepcion.class,
                    () -> juego.abrirPartida(temporal.toFile()));
            assertEquals(DOPOSoccerExcepcion.NO_SE_PUDO_ABRIR, e.getMessage());
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    @Test
    void deberiaConservarElProgresoDeLosJugadores() throws DOPOSoccerExcepcion, IOException {
        juego.getJugador1().sumarPuntos(1500);
        juego.getJugador1().mejorar(Jugador.SPEED);
        juego.configurar(false, false, Configuracion.INGLES);
        Path carpeta = Files.createTempDirectory("progreso");
        Path archivo = carpeta.resolve("datos").resolve("progreso.properties");
        try {
            juego.guardarProgreso(archivo);
            Juego otro = new Juego();
            assertTrue(otro.cargarProgreso(archivo));
            assertEquals(1000, otro.getJugador1().getPuntos());
            assertEquals(2, otro.getJugador1().getSpeed());
            assertFalse(otro.getConfiguracion().isSonidoActivo());
            assertEquals(Configuracion.INGLES, otro.getConfiguracion().getIdioma());
        } finally {
            Files.deleteIfExists(archivo);
            Files.deleteIfExists(archivo.getParent());
            Files.deleteIfExists(carpeta);
        }
    }

    @Test
    void noDeberiaFallarSiNoHayProgresoGuardado() {
        assertFalse(juego.cargarProgreso(new File("no_hay_progreso_aqui.properties").toPath()));
    }

    @Test
    void deberiaIgnorarValoresDanadosDelProgreso() throws IOException {
        Path temporal = Files.createTempFile("progreso", ".properties");
        try {
            Files.write(temporal, "j1.puntos=abc\nj1.speed=99\n".getBytes("UTF-8"));
            assertTrue(juego.cargarProgreso(temporal));
            assertEquals(1, juego.getJugador1().getSpeed());
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
