package presentation;

import java.awt.*;
import java.awt.geom.*;

/**
 * Dibuja las banderas de los cuatro países con formas (sin imágenes), para que se
 * vean nítidas en cualquier tamaño.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
final class DibujoBandera {

    private DibujoBandera() {
        // Clase de utilidad.
    }

    /**
     * Dibuja la bandera del país dentro de un rectángulo con las esquinas redondeadas.
     *
     * @param g      el pincel
     * @param codigo COREA, CAMERUN, NIGERIA o EEUU
     * @param x      esquina izquierda
     * @param y      esquina de arriba
     * @param w      ancho
     * @param h      alto
     */
    static void dibujar(Graphics2D g, String codigo, double x, double y, double w, double h) {
        Shape clipAnterior = g.getClip();
        RoundRectangle2D marco = new RoundRectangle2D.Double(x, y, w, h, h * 0.22, h * 0.22);
        g.clip(marco);

        if ("COREA".equals(codigo)) {
            corea(g, x, y, w, h);
        } else if ("CAMERUN".equals(codigo)) {
            camerun(g, x, y, w, h);
        } else if ("NIGERIA".equals(codigo)) {
            nigeria(g, x, y, w, h);
        } else {
            estadosUnidos(g, x, y, w, h);
        }

        // Brillo suave encima
        g.setColor(new Color(255, 255, 255, 40));
        g.fill(new Rectangle2D.Double(x, y, w, h * 0.45));
        g.setClip(clipAnterior);
        g.setColor(new Color(0, 0, 0, 120));
        g.setStroke(new BasicStroke((float) Math.max(1.2, h * 0.04)));
        g.draw(marco);
    }

    private static void corea(Graphics2D g, double x, double y, double w, double h) {
        g.setColor(new Color(0xF8, 0xF8, 0xF8));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        double cx = x + w / 2;
        double cy = y + h / 2;
        double r = h * 0.24;
        Color rojo = new Color(0xCD, 0x2E, 0x3A);
        Color azul = new Color(0x00, 0x47, 0xA0);
        g.setColor(azul);
        g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        g.setColor(rojo);
        g.fill(new Arc2D.Double(cx - r, cy - r, 2 * r, 2 * r, 0, 180, Arc2D.PIE));
        g.fill(new Ellipse2D.Double(cx - r, cy - r / 2, r, r));
        g.setColor(azul);
        g.fill(new Ellipse2D.Double(cx, cy - r / 2, r, r));
        // Trigramas en las esquinas
        g.setColor(Color.BLACK);
        double[][] pos = {{-1, -1, 1}, {1, -1, -1}, {-1, 1, -1}, {1, 1, 1}};
        for (double[] p : pos) {
            AffineTransform antes = g.getTransform();
            g.translate(cx + p[0] * w * 0.30, cy + p[1] * h * 0.30);
            g.rotate(p[2] * Math.toRadians(33));
            double bw = w * 0.15;
            double bh = h * 0.045;
            for (int i = -1; i <= 1; i++) {
                g.fill(new Rectangle2D.Double(-bw / 2, i * bh * 1.9 - bh / 2, bw, bh));
            }
            g.setTransform(antes);
        }
    }

    private static void camerun(Graphics2D g, double x, double y, double w, double h) {
        double t = w / 3;
        g.setColor(new Color(0x00, 0x7A, 0x5E));
        g.fill(new Rectangle2D.Double(x, y, t + 1, h));
        g.setColor(new Color(0xCE, 0x11, 0x26));
        g.fill(new Rectangle2D.Double(x + t, y, t + 1, h));
        g.setColor(new Color(0xFC, 0xD1, 0x16));
        g.fill(new Rectangle2D.Double(x + 2 * t, y, t, h));
        g.setColor(new Color(0xFC, 0xD1, 0x16));
        g.fill(estrella(x + w / 2, y + h / 2, h * 0.27));
    }

    private static void nigeria(Graphics2D g, double x, double y, double w, double h) {
        double t = w / 3;
        g.setColor(new Color(0x00, 0x87, 0x51));
        g.fill(new Rectangle2D.Double(x, y, t + 1, h));
        g.fill(new Rectangle2D.Double(x + 2 * t, y, t, h));
        g.setColor(Color.WHITE);
        g.fill(new Rectangle2D.Double(x + t, y, t + 1, h));
    }

    private static void estadosUnidos(Graphics2D g, double x, double y, double w, double h) {
        double franja = h / 13;
        for (int i = 0; i < 13; i++) {
            g.setColor(i % 2 == 0 ? new Color(0xB2, 0x22, 0x34) : Color.WHITE);
            g.fill(new Rectangle2D.Double(x, y + i * franja, w, franja + 0.6));
        }
        g.setColor(new Color(0x3C, 0x3B, 0x6E));
        double cw = w * 0.42;
        double ch = franja * 7;
        g.fill(new Rectangle2D.Double(x, y, cw, ch));
        g.setColor(Color.WHITE);
        double d = Math.max(1.2, h * 0.045);
        for (int fila = 0; fila < 4; fila++) {
            for (int col = 0; col < 5; col++) {
                g.fill(new Ellipse2D.Double(x + cw * (0.12 + col * 0.19), y + ch * (0.14 + fila * 0.23), d, d));
            }
        }
    }

    private static Shape estrella(double cx, double cy, double r) {
        Path2D.Double p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double radio = i % 2 == 0 ? r : r * 0.4;
            double ang = -Math.PI / 2 + i * Math.PI / 5;
            double px = cx + Math.cos(ang) * radio;
            double py = cy + Math.sin(ang) * radio;
            if (i == 0) {
                p.moveTo(px, py);
            } else {
                p.lineTo(px, py);
            }
        }
        p.closePath();
        return p;
    }
}
