package presentation;

import domain.Configuracion;

import java.awt.*;

/**
 * Configuración general del juego (sonido, música, idioma) y un recordatorio de
 * los controles de cada jugador.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
class PantallaConfiguracion extends PantallaBase {

    PantallaConfiguracion(VentanaPrincipal ventana) {
        super(ventana, false);
    }

    @Override
    protected void pintar(Graphics2D g, double t) {
        velo(g, 135);
        botonVolver(g, ventana::irAPortada);
        titulo(g, "CONFIGURACIÓN", 78);
        Configuracion c = ventana.getJuego().getConfiguracion();

        tarjeta(g, 60, 100, 440, 400, Estilo.AZUL_CLARO, false);
        fila(g, "EFECTOS DE SONIDO", 150, c.isSonidoActivo(), ventana::alternarSonido);
        fila(g, "MÚSICA", 240, c.isMusicaActiva(), ventana::alternarMusica);

        Estilo.textoCentrado(g, "IDIOMA", Estilo.fuente(21), 280, 362, Estilo.AZUL_CLARO);
        boolean es = Configuracion.ESPAÑOL.equals(c.getIdioma());
        boton(g, "ESPAÑOL", es ? Estilo.VERDE : Estilo.mezclar(Estilo.AZUL, -0.2), 92, 382, 184, 52, true,
                () -> ventana.cambiarIdioma(Configuracion.ESPAÑOL));
        boton(g, "ENGLISH", !es ? Estilo.VERDE : Estilo.mezclar(Estilo.AZUL, -0.2), 284, 382, 184, 52, true,
                () -> ventana.cambiarIdioma(Configuracion.INGLES));

        tarjeta(g, 520, 100, 420, 400, Estilo.AMARILLO, false);
        Estilo.textoCentrado(g, "CONTROLES", Estilo.fuente(25), 730, 140, Estilo.AMARILLO);
        controles(g, "JUGADOR 1", Estilo.AZUL_CLARO, 540, 158, "A / D", "W", "F", "G");
        controles(g, "JUGADOR 2", Estilo.ROJO, 540, 328, "← / →", "↑", ". (punto)", ", (coma)");
    }

    private void fila(Graphics2D g, String texto, double y, boolean encendido, Runnable accion) {
        // La etiqueta se achica sola si no cabe antes del interruptor, para que nunca quede tapada.
        double maximo = 350 - 28 - 92;
        Font f = Estilo.ajustar(g, Estilo.fuente(20), texto, maximo);
        g.setFont(f);
        g.setColor(Color.WHITE);
        g.drawString(texto, 92f, (float) (y + 34));
        interruptor(g, 360, y, encendido, accion);
    }

    private void controles(Graphics2D g, String nombre, Color color, double x, double y, String mover, String salto,
                           String patada, String poder) {
        Estilo.textoCentrado(g, nombre, Estilo.fuente(21), x + 190, y + 20, color);
        String[][] filas = {{"Moverse", mover}, {"Saltar", salto}, {"Patear", patada}, {"Poder", poder}};
        for (int i = 0; i < filas.length; i++) {
            double yy = y + 56 + i * 32;
            g.setFont(Estilo.fuenteTexto(18));
            g.setColor(new Color(0xD8, 0xE6, 0xFF));
            g.drawString(filas[i][0], (float) (x + 60), (float) yy);
            double w = Math.max(48, Estilo.ancho(g, Estilo.fuente(16), filas[i][1]) + 28);
            Estilo.panel(g, x + 300 - w / 2, yy - 21, w, 28, 10, Color.WHITE, new Color(0x8A, 0x93, 0xA6));
            Estilo.textoCentrado(g, filas[i][1], Estilo.fuente(16), x + 300, yy - 1, Estilo.TEXTO_OSCURO);
        }
    }
}
