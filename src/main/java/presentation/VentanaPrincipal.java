package presentation;

import domain.Cancha;
import domain.Configuracion;
import domain.Juego;
import domain.Jugador;
import domain.Partido;
import exceptions.DOPOSoccerExcepcion;
import util.RegistroErrores;

import javax.swing.*;
import java.awt.*;

/**
 * La ventana del juego. Guarda las elecciones que el jugador va haciendo en las
 * pantallas (personajes, tipo de partido, tamaño) y cambia de una pantalla a
 * otra. Todo lo que tiene que ver con las reglas se lo pide a {@link Juego}.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class VentanaPrincipal extends JFrame {

    private static final String PORTADA = "portada";
    private static final String MODALIDAD = "modalidad";
    private static final String PERSONAJES = "personajes";
    private static final String CONFIG_PARTIDO = "configPartido";
    private static final String CONFIGURACION = "configuracion";
    private static final String MEJORAS = "mejoras";
    private static final String PARTIDO = "partido";

    private final Juego juego;
    private final CardLayout cartas;
    private final JPanel contenedor;
    private final PantallaBase portada;
    private final PantallaBase modalidad;
    private final PantallaBase personajes;
    private final PantallaBase configPartido;
    private final PantallaBase configuracion;
    private final PantallaBase mejoras;
    private final PantallaPartido pantallaPartido;
    private PantallaBase actual;

    private final int[] personajeElegido = {0, 1};
    private String tipo = Partido.TIPO_TIEMPO;
    private int minutos = 3;
    private int goles = 5;
    private int resolucion = Cancha.MEDIANA;

    /**
     * Arma la ventana con todas sus pantallas y muestra la portada.
     */
    public VentanaPrincipal() {
        super("DOPO SOCCER");
        juego = new Juego();
        cartas = new CardLayout();
        contenedor = new JPanel(cartas);

        portada = new PantallaPortada(this);
        modalidad = new PantallaModalidad(this);
        personajes = new PantallaPersonajes(this);
        configPartido = new PantallaConfigPartido(this);
        configuracion = new PantallaConfiguracion(this);
        mejoras = new PantallaMejoras(this);
        pantallaPartido = new PantallaPartido(this);

        contenedor.add(portada, PORTADA);
        contenedor.add(modalidad, MODALIDAD);
        contenedor.add(personajes, PERSONAJES);
        contenedor.add(configPartido, CONFIG_PARTIDO);
        contenedor.add(configuracion, CONFIGURACION);
        contenedor.add(mejoras, MEJORAS);
        contenedor.add(pantallaPartido, PARTIDO);
        setContentPane(contenedor);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        aplicarResolucion();
        setLocationRelativeTo(null);
        mostrar(portada, PORTADA);
    }

    private void mostrar(PantallaBase pantalla, String nombre) {
        if (actual != null) {
            actual.desactivar();
        }
        actual = pantalla;
        cartas.show(contenedor, nombre);
        pantalla.activar();
    }

    private void aplicarResolucion() {
        contenedor.setPreferredSize(new Dimension(Cancha.anchoPantalla(resolucion), Cancha.altoPantalla(resolucion)));
        pack();
        setLocationRelativeTo(null);
    }

    // navegación

    void irAPortada() {
        mostrar(portada, PORTADA);
    }

    void irAModalidad() {
        mostrar(modalidad, MODALIDAD);
    }

    void irAPersonajes() {
        try {
            juego.seleccionarModalidad(Juego.JUGADOR_VS_JUGADOR);
            mostrar(personajes, PERSONAJES);
        } catch (DOPOSoccerExcepcion e) {
            avisar(e.getMessage());
        }
    }

    void irAConfigPartido() {
        mostrar(configPartido, CONFIG_PARTIDO);
    }

    void irAConfiguracion() {
        mostrar(configuracion, CONFIGURACION);
    }

    void irAMejoras() {
        mostrar(mejoras, MEJORAS);
    }

    /**
     * Compra una mejora de estadística para un jugador (gasta sus puntos).
     */
    void mejorarEstadistica(Jugador jugador, String estadistica) {
        try {
            jugador.mejorar(estadistica);
        } catch (DOPOSoccerExcepcion e) {
            avisar(e.getMessage());
        }
    }

    void salir() {
        dispose();
        System.exit(0);
    }

    /**
     * Crea un partido nuevo con lo que se eligió y pasa a la cancha.
     */
    void iniciarPartido() {
        try {
            int valor = Partido.TIPO_TIEMPO.equals(tipo) ? minutos : goles;
            Partido partido = juego.iniciarPartido(juego.getPersonajes()[personajeElegido[0]],
                    juego.getPersonajes()[personajeElegido[1]], tipo, valor, resolucion);
            aplicarResolucion();
            pantallaPartido.mostrarPartido(partido);
            mostrar(pantallaPartido, PARTIDO);
        } catch (DOPOSoccerExcepcion e) {
            avisar(e.getMessage());
        }
    }

    private void avisar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Ups", JOptionPane.WARNING_MESSAGE);
    }

    // elecciones

    Juego getJuego() {
        return juego;
    }

    int getPersonajeElegido(int jugador) {
        return personajeElegido[jugador];
    }

    void elegirPersonaje(int jugador, int indice) {
        personajeElegido[jugador] = indice;
        repaint();
    }

    String getTipo() {
        return tipo;
    }

    void setTipo(String nuevo) {
        tipo = nuevo;
    }

    int getMinutos() {
        return minutos;
    }

    int getGoles() {
        return goles;
    }

    /**
     * Sube o baja los minutos o los goles según el tipo elegido, dentro de los límites.
     */
    void cambiarValor(int paso) {
        if (Partido.TIPO_TIEMPO.equals(tipo)) {
            minutos = Math.max(1, Math.min(10, minutos + paso));
        } else {
            goles = Math.max(1, Math.min(20, goles + paso));
        }
    }

    int getResolucion() {
        return resolucion;
    }

    void setResolucion(int indice) {
        resolucion = indice;
        aplicarResolucion();
    }

    void alternarSonido() {
        Configuracion c = juego.getConfiguracion();
        guardarConfiguracion(!c.isSonidoActivo(), c.isMusicaActiva(), c.getIdioma());
        Sonidos.setActivo(c.isSonidoActivo());
    }

    void alternarMusica() {
        Configuracion c = juego.getConfiguracion();
        guardarConfiguracion(c.isSonidoActivo(), !c.isMusicaActiva(), c.getIdioma());
    }

    void cambiarIdioma(String idioma) {
        Configuracion c = juego.getConfiguracion();
        guardarConfiguracion(c.isSonidoActivo(), c.isMusicaActiva(), idioma);
    }

    private void guardarConfiguracion(boolean sonido, boolean musica, String idioma) {
        try {
            juego.configurar(sonido, musica, idioma);
        } catch (DOPOSoccerExcepcion e) {
            avisar(e.getMessage());
        }
    }

    /**
     * Punto de apoyo para registrar un error inesperado sin que el juego se caiga.
     */
    static void registrar(String donde, Throwable error) {
        RegistroErrores.registrar(donde, error);
    }
}
