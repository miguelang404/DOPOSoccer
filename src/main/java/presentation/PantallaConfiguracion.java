package presentation;

import domain.Configuracion;

import java.awt.*;

/**
 * Configuración general del juego (sonido, música, idioma) y un recordatorio de
 * los controles de cada jugador.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class PantallaConfiguracion extends PantallaBase {

    PantallaConfiguracion(VentanaPrincipal ventana) {
        super(ventana, false);
    }

    @Override
    protected void pintar(Graphics2D g, double t) {
        velo(g, 135);
        titulo(g, "CONFIGURACIÓN", 78);
        Configuracion c = ventana.getJuego().getConfiguracion();

        tarjeta(g, 60, 100, 440, 366, Estilo.AZUL_CLARO, false);
        fila(g, "EFECTOS DE SONIDO", 150, c.isSonidoActivo(), ventana::alternarSonido);
        fila(g, "MÚSICA", 232, c.isMusicaActiva(), ventana::alternarMusica);

        Estilo.textoCentrado(g, "IDIOMA", Estilo.fuente(22), 280, 330, Color.WHITE);
        boolean es = Configuracion.ESPAÑOL.equals(c.getIdioma());
        boton(g, "ESPAÑOL", es ? Estilo.VERDE : Estilo.mezclar(Estilo.AZUL, -0.2), 100, 346, 180, 50, true,
                () -> ventana.cambiarIdioma(Configuracion.ESPAÑOL));
        boton(g, "ENGLISH", !es ? Estilo.VERDE : Estilo.mezclar(Estilo.AZUL, -0.2), 290, 346, 180, 50, true,
                () -> ventana.cambiarIdioma(Configuracion.INGLES));
        texto(g, "La música y el idioma se guardan ahora,", 14, 280, 428, new Color(0xBF, 0xD6, 0xFF));
        texto(g, "pero se activan en versiones siguientes.", 14, 280, 448, new Color(0xBF, 0xD6, 0xFF));

        tarjeta(g, 520, 100, 420, 366, Estilo.AMARILLO, false);
        Estilo.textoCentrado(g, "CONTROLES", Estilo.fuente(26), 730, 142, Estilo.AMARILLO);
        controles(g, "JUGADOR 1", Estilo.AZUL_CLARO, 540, 172, "A / D", "W", "F", "G");
        controles(g, "JUGADOR 2", Estilo.ROJO, 540, 318, "← / →", "↑", ". (punto)", ", (coma)");

        boton(g, "VOLVER", Estilo.ROJO, 40, 482, 200, 56, true, ventana::irAPortada);
    }

    private void fila(Graphics2D g, String texto, double y, boolean encendido, Runnable accion) {
        g.setFont(Estilo.fuente(22));
        double ancho = Estilo.ancho(g, Estilo.fuente(22), texto);
        Estilo.textoCentrado(g, texto, Estilo.fuente(22), 90 + ancho / 2, y + 34, Color.WHITE);
        interruptor(g, 330, y, encendido, accion);
    }

    private void controles(Graphics2D g, String nombre, Color color, double x, double y, String mover, String salto,
                           String patada, String poder) {
        Estilo.textoCentrado(g, nombre, Estilo.fuente(20), x + 190, y + 18, color);
        String[][] filas = {{"Moverse", mover}, {"Saltar", salto}, {"Patear", patada}, {"Poder", poder}};
        for (int i = 0; i < filas.length; i++) {
            double yy = y + 44 + i * 30;
            g.setFont(Estilo.fuenteTexto(17));
            g.setColor(new Color(0xD8, 0xE6, 0xFF));
            g.drawString(filas[i][0], (float) (x + 60), (float) yy);
            double w = Math.max(44, Estilo.ancho(g, Estilo.fuente(15), filas[i][1]) + 26);
            Estilo.panel(g, x + 300 - w / 2, yy - 20, w, 26, 10, Color.WHITE, new Color(0x8A, 0x93, 0xA6));
            Estilo.textoCentrado(g, filas[i][1], Estilo.fuente(15), x + 300, yy, Estilo.TEXTO_OSCURO);
        }
    }
}
