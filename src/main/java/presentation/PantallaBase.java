package presentation;

import domain.Cancha;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Base de todas las pantallas. Dibuja el estadio de fondo, trabaja siempre con
 * las coordenadas lógicas de la cancha (1000 x 560) y se ajusta sola al tamaño
 * de la ventana. Los botones no son componentes de Swing: se dibujan y se
 * registran como "zonas" de clic cada vez que se pinta la pantalla.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
abstract class PantallaBase extends JPanel {

    /**
     * Una zona donde se puede hacer clic.
     */
    private static final class Zona {
        final Rectangle2D area;
        final Runnable accion;

        Zona(Rectangle2D area, Runnable accion) {
            this.area = area;
            this.accion = accion;
        }
    }

    protected final VentanaPrincipal ventana;
    protected double t;

    private final List<Zona> zonas = new ArrayList<Zona>();
    private final List<Zona> zonasNuevas = new ArrayList<Zona>();
    private final Timer reloj;
    private BufferedImage fondo;
    private int fondoAncho;
    private int fondoAlto;
    private boolean fondoConArcos;
    private Zona encima;
    private Zona presionada;

    /**
     * @param ventana  la ventana principal, para poder cambiar de pantalla
     * @param conArcos si el fondo lleva los arcos de fútbol
     */
    PantallaBase(VentanaPrincipal ventana, boolean conArcos) {
        this.ventana = ventana;
        this.fondoConArcos = conArcos;
        setOpaque(true);
        setBackground(Estilo.AZUL_NOCHE);
        reloj = new Timer(33, e -> {
            t += 2;
            repaint();
        });
        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Zona z = buscar(e);
                if ((z == null) != (encima == null) || (z != null && !z.area.equals(encima.area))) {
                    encima = z;
                    setCursor(Cursor.getPredefinedCursor(z == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                presionada = buscar(e);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Zona z = buscar(e);
                Zona antes = presionada;
                presionada = null;
                repaint();
                if (z != null && antes != null && z.area.equals(antes.area)) {
                    Sonidos.reproducir("clic");
                    z.accion.run();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                encima = null;
                presionada = null;
                repaint();
            }
        };
        addMouseListener(raton);
        addMouseMotionListener(raton);
    }

    /**
     * Empieza a animar la pantalla (se llama cuando se muestra).
     */
    void activar() {
        reloj.start();
        requestFocusInWindow();
    }

    /**
     * Detiene la animación (se llama cuando se oculta).
     */
    void desactivar() {
        reloj.stop();
    }

    private Zona buscar(MouseEvent e) {
        // Recomendación de la IA generativa para los botones dibujados: como la pantalla se redibuja
        // todo el tiempo, las zonas de clic se guardan en cada pintada y se convierte la posición del
        // mouse a las coordenadas lógicas de la cancha (1000 x 560).
        double lx = e.getX() * Cancha.ANCHO / (double) getWidth();
        double ly = e.getY() * Cancha.ALTO / (double) getHeight();
        for (int i = zonas.size() - 1; i >= 0; i--) {
            if (zonas.get(i).area.contains(lx, ly)) {
                return zonas.get(i);
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        if (fondo == null || fondoAncho != w || fondoAlto != h) {
            fondo = DibujoEstadio.crearFondo(w, h, Cancha.ANCHO, Cancha.ALTO, fondoConArcos);
            fondoAncho = w;
            fondoAlto = h;
        }
        Graphics2D g = (Graphics2D) graficos.create();
        Estilo.calidad(g);
        g.drawImage(fondo, 0, 0, null);
        g.scale(w / (double) Cancha.ANCHO, h / (double) Cancha.ALTO);
        zonasNuevas.clear();
        pintar(g, t);
        zonas.clear();
        zonas.addAll(zonasNuevas);
        g.dispose();
    }

    /**
     * Cada pantalla dibuja aquí su contenido, en coordenadas lógicas.
     *
     * @param g pincel ya escalado a 1000 x 560
     * @param t tiempo en cuadros
     */
    protected abstract void pintar(Graphics2D g, double t);

    // piezas de interfaz

    /**
     * Velo oscuro semitransparente sobre todo el fondo, para que resalten los menús.
     */
    protected void velo(Graphics2D g, int alfa) {
        g.setColor(new Color(5, 12, 35, alfa));
        g.fillRect(0, 0, Cancha.ANCHO, Cancha.ALTO);
    }

    /**
     * Título grande de la pantalla, con borde y degradado.
     */
    protected void titulo(Graphics2D g, String texto, double y) {
        Estilo.textoContorno(g, texto, Estilo.ajustar(g, Estilo.fuente(54), texto, 640), Cancha.ANCHO / 2.0, y,
                Estilo.AMARILLO, Estilo.NARANJA, new Color(0x5A, 0x2A, 0x00), 5f);
    }

    /**
     * Texto pequeño centrado, blanco.
     */
    protected void texto(Graphics2D g, String texto, double tam, double cx, double y, Color color) {
        Estilo.textoCentrado(g, texto, Estilo.fuenteTexto((float) tam), cx, y, color);
    }

    /**
     * Botón redondeado. Se dibuja y queda registrado como zona de clic.
     */
    protected void boton(Graphics2D g, String texto, Color color, double x, double y, double w, double h,
                         boolean activo, Runnable accion) {
        Rectangle2D area = new Rectangle2D.Double(x, y, w, h);
        Zona z = null;
        if (activo) {
            z = new Zona(area, accion);
            zonasNuevas.add(z);
        }
        boolean sobre = activo && z != null && esZona(encima, area);
        boolean hundido = activo && esZona(presionada, area);
        Color base = activo ? color : Estilo.GRIS;
        if (sobre) {
            base = Estilo.mezclar(base, 0.16);
        }
        double hundir = hundido ? 3 : 0;
        double cara = h - 7;
        g.setColor(Estilo.mezclar(base, -0.55));
        g.fill(new RoundRectangle2D.Double(x, y + 5, w, cara + 2, h * 0.5, h * 0.5));
        Estilo.panel(g, x, y + hundir, w, cara, h * 0.5, base, Estilo.mezclar(base, -0.5));
        // El texto se achica solo si no cabe, dejando un margen a cada lado dentro del botón.
        Font f = Estilo.ajustar(g, Estilo.fuente((float) (h * 0.42)), texto, w - 32);
        g.setFont(f);
        double tw = Estilo.ancho(g, f, texto);
        double ty = y + hundir + cara / 2 + f.getSize2D() * 0.36;
        g.setColor(activo ? Color.WHITE : new Color(0xDD, 0xE1, 0xEA));
        g.drawString(texto, (float) (x + (w - tw) / 2), (float) ty);
    }

    /**
     * Botón de volver: pequeño y discreto, en la esquina de arriba a la izquierda, para que no le
     * quite protagonismo a las acciones principales de cada pantalla.
     */
    protected void botonVolver(Graphics2D g, Runnable accion) {
        double x = 24;
        double y = 16;
        double w = 118;
        double h = 36;
        Rectangle2D area = new Rectangle2D.Double(x, y, w, h);
        zonasNuevas.add(new Zona(area, accion));
        boolean sobre = esZona(encima, area);
        boolean hundido = esZona(presionada, area);
        g.setColor(sobre ? new Color(255, 255, 255, 60) : new Color(6, 14, 38, 150));
        g.fill(new RoundRectangle2D.Double(x, y + (hundido ? 1 : 0), w, h, h, h));
        g.setColor(new Color(255, 255, 255, sobre ? 210 : 120));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new RoundRectangle2D.Double(x, y + (hundido ? 1 : 0), w, h, h, h));
        // flechita hacia la izquierda
        double ay = y + h / 2 + (hundido ? 1 : 0);
        java.awt.geom.Path2D flecha = new java.awt.geom.Path2D.Double();
        flecha.moveTo(x + 27, ay - 7);
        flecha.lineTo(x + 19, ay);
        flecha.lineTo(x + 27, ay + 7);
        g.draw(flecha);
        Font f = Estilo.fuenteTexto(15);
        g.setFont(f);
        g.setColor(Color.WHITE);
        g.drawString("VOLVER", (float) (x + 38), (float) (ay + 5.5));
    }

    /**
     * Ventana de confirmación dibujada con el estilo del juego. Mientras se muestra, la pantalla de
     * abajo no debe registrar botones (para que no se pueda hacer clic detrás).
     *
     * @param pregunta lo que se pregunta
     * @param detalle  una línea de explicación (puede ser vacía)
     * @param textoSi  texto del botón que confirma
     * @param textoNo  texto del botón que cancela
     * @param si       qué pasa si confirma
     * @param no       qué pasa si cancela
     */
    protected void confirmacion(Graphics2D g, String pregunta, String detalle, String textoSi, String textoNo,
                                Runnable si, Runnable no) {
        velo(g, 175);
        tarjeta(g, 270, 150, 460, 250, Estilo.AMARILLO, true);
        Font f = Estilo.ajustar(g, Estilo.fuente(30), pregunta, 410);
        Estilo.textoCentrado(g, pregunta, f, 500, 220, Estilo.AMARILLO);
        if (!detalle.isEmpty()) {
            texto(g, detalle, 16, 500, 256, new Color(0xD8, 0xE6, 0xFF));
        }
        boton(g, textoSi, Estilo.ROJO, 300, 300, 195, 58, true, si);
        boton(g, textoNo, Estilo.VERDE, 505, 300, 195, 58, true, no);
    }

    private boolean esZona(Zona z, Rectangle2D area) {
        return z != null && z.area.equals(area);
    }

    /**
     * Registra una zona de clic sin dibujar nada (para tarjetas que se pintan aparte).
     */
    protected boolean zona(double x, double y, double w, double h, Runnable accion) {
        Rectangle2D area = new Rectangle2D.Double(x, y, w, h);
        zonasNuevas.add(new Zona(area, accion));
        return esZona(encima, area);
    }

    /**
     * @return true si el mouse está sobre la zona dada
     */
    protected boolean sobre(double x, double y, double w, double h) {
        return esZona(encima, new Rectangle2D.Double(x, y, w, h));
    }

    /**
     * Tarjeta con fondo oscuro translúcido y borde de color.
     */
    protected void tarjeta(Graphics2D g, double x, double y, double w, double h, Color borde, boolean brillo) {
        RoundRectangle2D forma = new RoundRectangle2D.Double(x, y, w, h, 34, 34);
        g.setPaint(new java.awt.GradientPaint(0, (float) y, new Color(0x16, 0x2F, 0x6E, 235),
                0, (float) (y + h), new Color(0x08, 0x16, 0x3F, 240)));
        g.fill(forma);
        g.setPaint(new java.awt.GradientPaint(0, (float) y, new Color(255, 255, 255, 60),
                0, (float) (y + h * 0.35), new Color(255, 255, 255, 0)));
        g.fill(new RoundRectangle2D.Double(x + 3, y + 3, w - 6, h * 0.4, 30, 30));
        if (brillo) {
            g.setColor(Estilo.conAlfa(borde, 90));
            g.setStroke(new BasicStroke(9f));
            g.draw(forma);
        }
        g.setColor(borde);
        g.setStroke(new BasicStroke(4f));
        g.draw(forma);
    }

    /**
     * Interruptor tipo "encendido / apagado".
     */
    protected void interruptor(Graphics2D g, double x, double y, boolean encendido, Runnable accion) {
        double w = 120;
        double h = 52;
        boolean hover = zona(x, y, w, h, accion);
        Color fondoColor = encendido ? Estilo.VERDE : Estilo.GRIS;
        if (hover) {
            fondoColor = Estilo.mezclar(fondoColor, 0.12);
        }
        Estilo.panel(g, x, y, w, h, h, fondoColor, Estilo.mezclar(fondoColor, -0.45));
        double kx = encendido ? x + w - h + 5 : x + 5;
        g.setPaint(new java.awt.GradientPaint(0, (float) y, Color.WHITE, 0, (float) (y + h), new Color(0xC9, 0xD2, 0xE6)));
        g.fill(new Ellipse2D.Double(kx, y + 5, h - 10, h - 10));
        g.setColor(new Color(0x7B, 0x86, 0xA3));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Ellipse2D.Double(kx, y + 5, h - 10, h - 10));
        Font f = Estilo.fuente(18);
        g.setFont(f);
        String s = encendido ? "SÍ" : "NO";
        double tw = Estilo.ancho(g, f, s);
        g.setColor(Color.WHITE);
        g.drawString(s, (float) (encendido ? x + 18 : x + w - 18 - tw), (float) (y + h / 2 + 6));
    }

    /**
     * Parte un texto largo en renglones que caben en el ancho dado.
     */
    protected List<String> partir(Graphics2D g, String texto, Font f, double ancho) {
        List<String> lineas = new ArrayList<String>();
        FontMetrics fm = g.getFontMetrics(f);
        String actual = "";
        for (String palabra : texto.split(" ")) {
            String prueba = actual.isEmpty() ? palabra : actual + " " + palabra;
            if (fm.stringWidth(prueba) > ancho && !actual.isEmpty()) {
                lineas.add(actual);
                actual = palabra;
            } else {
                actual = prueba;
            }
        }
        if (!actual.isEmpty()) {
            lineas.add(actual);
        }
        return lineas;
    }

    /**
     * @return el rectángulo entero de la pantalla (útil para pruebas visuales)
     */
    Rectangle limites() {
        return new Rectangle(0, 0, getWidth(), getHeight());
    }
}
