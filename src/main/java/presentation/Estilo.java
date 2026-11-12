package presentation;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Paleta de colores, tipografías y pequeños trucos de dibujo que comparten todas
 * las pantallas, para que el juego se vea como una sola cosa.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
final class Estilo {

    static final Color AZUL_NOCHE = new Color(0x0B, 0x1F, 0x4D);
    static final Color AZUL = new Color(0x1E, 0x6F, 0xE0);
    static final Color AZUL_CLARO = new Color(0x5C, 0xB8, 0xFF);
    static final Color VERDE = new Color(0x2E, 0xB8, 0x4B);
    static final Color VERDE_OSCURO = new Color(0x13, 0x7A, 0x2E);
    static final Color NARANJA = new Color(0xFF, 0x8A, 0x1F);
    static final Color AMARILLO = new Color(0xFF, 0xD3, 0x2E);
    static final Color ROJO = new Color(0xE5, 0x3B, 0x3B);
    static final Color ROJO_OSCURO = new Color(0x9E, 0x1B, 0x25);
    static final Color MORADO = new Color(0x7A, 0x4D, 0xE8);
    static final Color GRIS = new Color(0x8A, 0x93, 0xA6);
    static final Color BLANCO = Color.WHITE;
    static final Color TEXTO_OSCURO = new Color(0x14, 0x20, 0x3C);

    private static String familiaCache;

    private Estilo() {
        // Clase de utilidad.
    }

    /**
     * @return el nombre de la tipografía más "gamer" que haya en este computador
     */
    static String familia() {
        if (familiaCache == null) {
            String[] preferidas = {"Arial Black", "Segoe UI Black", "Impact", "Verdana", "Dialog"};
            String[] disponibles = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            familiaCache = "Dialog";
            for (String p : preferidas) {
                for (String d : disponibles) {
                    if (d.equalsIgnoreCase(p)) {
                        familiaCache = d;
                        return familiaCache;
                    }
                }
            }
        }
        return familiaCache;
    }

    /**
     * @param tamaño tamaño en puntos
     * @return fuente gruesa para títulos y botones
     */
    static Font fuente(float tamaño) {
        return new Font(familia(), Font.BOLD, Math.round(tamaño)).deriveFont(tamaño);
    }

    /**
     * @param tamaño tamaño en puntos
     * @return fuente normal para textos largos
     */
    static Font fuenteTexto(float tamaño) {
        return new Font("SansSerif", Font.BOLD, Math.round(tamaño)).deriveFont(tamaño);
    }

    /**
     * Enciende el suavizado para que todo se vea liso.
     *
     * @param g el pincel
     */
    static void calidad(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    /**
     * Mezcla un color con blanco (positivo) o negro (negativo).
     *
     * @param c      color original
     * @param factor entre -1 y 1
     * @return el color aclarado u oscurecido
     */
    static Color mezclar(Color c, double factor) {
        int r = c.getRed();
        int gr = c.getGreen();
        int b = c.getBlue();
        if (factor >= 0) {
            r += (255 - r) * factor;
            gr += (255 - gr) * factor;
            b += (255 - b) * factor;
        } else {
            r += r * factor;
            gr += gr * factor;
            b += b * factor;
        }
        return new Color(clamp(r), clamp(gr), clamp(b), c.getAlpha());
    }

    static Color conAlfa(Color c, int alfa) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alfa)));
    }

    private static int clamp(double v) {
        return (int) Math.max(0, Math.min(255, Math.round(v)));
    }

    /**
     * Ancho que ocupa un texto con una fuente.
     *
     * @param g     el pincel
     * @param f     la fuente
     * @param texto el texto
     * @return el ancho en píxeles
     */
    static double ancho(Graphics2D g, Font f, String texto) {
        return f.getStringBounds(texto, g.getFontRenderContext()).getWidth();
    }

    /**
     * Texto grande con contorno grueso y degradado (estilo videojuego).
     *
     * @param g        el pincel
     * @param texto    el texto
     * @param f        la fuente
     * @param cx       x del centro del texto
     * @param baseline y de la línea base
     * @param arriba   color de arriba del degradado
     * @param abajo    color de abajo del degradado
     * @param contorno color del borde
     * @param grosor   grosor del borde
     */
    static void textoContorno(Graphics2D g, String texto, Font f, double cx, double baseline,
                              Color arriba, Color abajo, Color contorno, float grosor) {
        GlyphVector gv = f.createGlyphVector(g.getFontRenderContext(), texto);
        Rectangle2D caja = gv.getVisualBounds();
        double x = cx - caja.getWidth() / 2 - caja.getX();
        Shape forma = gv.getOutline((float) x, (float) baseline);
        Rectangle2D b = forma.getBounds2D();

        // Borde
        g.setColor(contorno);
        g.draw(forma);
        // Relleno con degradado
        g.setPaint(new GradientPaint(0, (float) b.getMinY(), arriba, 0, (float) b.getMaxY(), abajo));
        g.fill(forma);
        // Brillo
        g.setClip(forma);
        g.setColor(new Color(255, 255, 255, 70));
        g.fill(new Rectangle2D.Double(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight() * 0.38));
        g.setClip(null);
    }

    /**
     * Texto normal centrado con una sombrita suave.
     *
     * @param g        el pincel
     * @param texto    el texto
     * @param f        la fuente
     * @param cx       x del centro
     * @param baseline y de la línea base
     * @param color    color del texto
     */
    static void textoCentrado(Graphics2D g, String texto, Font f, double cx, double baseline, Color color) {
        g.setFont(f);
        double w = ancho(g, f, texto);
        g.setColor(color);
        g.drawString(texto, (float) (cx - w / 2), (float) baseline);
    }

    /**
     * Rectángulo redondeado con degradado vertical y brillo, la base de botones y tarjetas.
     *
     * @param g     el pincel
     * @param x     esquina izquierda
     * @param y     esquina de arriba
     * @param w     ancho
     * @param h     alto
     * @param radio redondeo
     * @param color color base
     * @param borde color del borde (puede ser null)
     */
    static void panel(Graphics2D g, double x, double y, double w, double h, double radio, Color color, Color borde) {
        RoundRectangle2D forma = new RoundRectangle2D.Double(x, y, w, h, radio, radio);
        g.setPaint(new GradientPaint(0, (float) y, mezclar(color, 0.22), 0, (float) (y + h), mezclar(color, -0.18)));
        g.fill(forma);
        g.setPaint(new GradientPaint(0, (float) y, new Color(255, 255, 255, 80), 0, (float) (y + h * 0.5), new Color(255, 255, 255, 0)));
        g.fill(new RoundRectangle2D.Double(x + 2, y + 2, w - 4, h * 0.5, radio, radio));
        if (borde != null) {
            g.setColor(borde);
            g.setStroke(new BasicStroke(3f));
            g.draw(forma);
        }
    }
}
