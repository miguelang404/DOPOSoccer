package presentation;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * Dibuja el balón con sus parches rodando, y los efectos de los tiros de poder
 * (estela azul, rayos, remolino e ilusión).
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
final class DibujoBalon {

    private DibujoBalon() {
        // Clase de utilidad.
    }

    /**
     * Dibuja un balón.
     *
     * @param g      el pincel
     * @param x      centro x
     * @param y      centro y
     * @param radio  radio
     * @param giro   cuánto ha girado, en radianes
     * @param efecto código del efecto ("" si no tiene)
     * @param vx     velocidad horizontal (para la estela)
     * @param vy     velocidad vertical (para la estela)
     * @param t      tiempo en cuadros, para animar
     */
    static void dibujar(Graphics2D g, double x, double y, double radio, double giro, String efecto,
                        double vx, double vy, double t) {
        if (!efecto.isEmpty()) {
            efectoAtras(g, x, y, radio, efecto, vx, vy, t);
        }

        // Cuerpo del balón
        Shape circulo = new Ellipse2D.Double(x - radio, y - radio, radio * 2, radio * 2);
        g.setColor(Color.WHITE);
        g.fill(circulo);

        Shape anterior = g.getClip();
        g.clip(circulo);
        g.setColor(new Color(0x1C, 0x1F, 0x2A));
        // Parches: 5 en el ecuador y 2 en cada polo, proyectados como en una esfera
        for (int k = 0; k < 5; k++) {
            parche(g, x, y, radio, giro + k * 2 * Math.PI / 5, 0);
        }
        for (int k = 0; k < 5; k++) {
            parche(g, x, y, radio, giro + (k + 0.5) * 2 * Math.PI / 5, Math.toRadians(62));
            parche(g, x, y, radio, giro + (k + 0.5) * 2 * Math.PI / 5, Math.toRadians(-62));
        }
        g.setClip(anterior);

        g.setColor(new Color(0x23, 0x2B, 0x46));
        g.setStroke(new BasicStroke((float) Math.max(1.6, radio * 0.09)));
        g.draw(circulo);

        if (!efecto.isEmpty()) {
            efectoAdelante(g, x, y, radio, efecto, t);
        }
    }

    private static void parche(Graphics2D g, double x, double y, double radio, double longitud, double latitud) {
        // Recomendación de la IA generativa para dibujar los parches del balón como en una esfera: cada
        // parche se proyecta con longitud y latitud, y se achata en la dirección del borde para dar
        // sensación de giro.
        double cosLong = Math.cos(longitud);
        double visible = cosLong * Math.cos(latitud);
        if (visible <= 0.05) {
            return;
        }
        double px = x + radio * Math.cos(latitud) * Math.sin(longitud) * 0.92;
        double py = y + radio * Math.sin(latitud) * 0.92;
        double ancho = radio * 0.46 * Math.max(0.22, visible);
        double alto = radio * 0.46 * Math.max(0.35, Math.cos(latitud) * 0.6 + 0.4);
        Path2D.Double pentagono = new Path2D.Double();
        for (int i = 0; i < 5; i++) {
            double a = -Math.PI / 2 + i * 2 * Math.PI / 5;
            double ex = px + Math.cos(a) * ancho;
            double ey = py + Math.sin(a) * alto;
            if (i == 0) {
                pentagono.moveTo(ex, ey);
            } else {
                pentagono.lineTo(ex, ey);
            }
        }
        pentagono.closePath();
        g.fill(pentagono);
    }

    private static void efectoAtras(Graphics2D g, double x, double y, double radio, String efecto,
                                    double vx, double vy, double t) {
        Color color = colorEfecto(efecto);
        double rapidez = Math.max(0.1, Math.hypot(vx, vy));
        double ux = vx / rapidez;
        double uy = vy / rapidez;
        // Estela: copias cada vez más pálidas hacia atrás
        for (int i = 8; i >= 1; i--) {
            double d = i * radio * 0.5;
            g.setColor(Estilo.conAlfa(color, 120 - i * 12));
            double r = radio * (1.05 - i * 0.05);
            g.fill(new Ellipse2D.Double(x - ux * d - r, y - uy * d - r, 2 * r, 2 * r));
        }
        // Resplandor
        float brillo = (float) (radio * (2.2 + 0.25 * Math.sin(t * 0.4)));
        g.setPaint(new RadialGradientPaint((float) x, (float) y, brillo, new float[]{0f, 1f},
                new Color[]{Estilo.conAlfa(color, 170), Estilo.conAlfa(color, 0)}));
        g.fill(new Ellipse2D.Double(x - brillo, y - brillo, brillo * 2, brillo * 2));
    }

    private static void efectoAdelante(Graphics2D g, double x, double y, double radio, String efecto, double t) {
        Color color = colorEfecto(efecto);
        if ("RAYO".equals(efecto)) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 4; i++) {
                double a = t * 0.7 + i * Math.PI / 2;
                Path2D.Double rayo = new Path2D.Double();
                double px = x + Math.cos(a) * radio;
                double py = y + Math.sin(a) * radio;
                rayo.moveTo(px, py);
                for (int s = 1; s <= 3; s++) {
                    double dist = radio + s * radio * 0.45;
                    double desvio = ((s + i) % 2 == 0 ? 1 : -1) * radio * 0.28;
                    rayo.lineTo(x + Math.cos(a) * dist - Math.sin(a) * desvio, y + Math.sin(a) * dist + Math.cos(a) * desvio);
                }
                g.setColor(Color.WHITE);
                g.draw(rayo);
                g.setColor(Estilo.conAlfa(color, 160));
                g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(rayo);
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
        } else if ("TORNADO".equals(efecto)) {
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 4; i++) {
                double k = 1.2 + i * 0.25;
                double fase = t * 0.5 + i * 1.3;
                g.setColor(Estilo.conAlfa(i % 2 == 0 ? Color.WHITE : color, 190 - i * 30));
                g.draw(new java.awt.geom.Arc2D.Double(x - radio * k, y - radio * (0.55 + i * 0.05), radio * 2 * k,
                        radio * (1.1 + i * 0.1), Math.toDegrees(fase), 230, java.awt.geom.Arc2D.OPEN));
            }
        } else if ("ILUSION".equals(efecto)) {
            g.setStroke(new BasicStroke(2.5f));
            double pulso = 1.25 + 0.12 * Math.sin(t * 0.3);
            g.setColor(Estilo.conAlfa(color, 160));
            g.draw(new Ellipse2D.Double(x - radio * pulso, y - radio * pulso, radio * 2 * pulso, radio * 2 * pulso));
            for (int i = 0; i < 3; i++) {
                double a = t * 0.12 + i * 2.1;
                AffineTransform antes = g.getTransform();
                g.translate(x + Math.cos(a) * radio * 1.5, y + Math.sin(a) * radio * 1.5);
                DibujoEstadio.destello(g, 0, 0, 1);
                g.setTransform(antes);
            }
        }
    }

    /**
     * @param efecto código del efecto
     * @return el color con el que se identifica
     */
    static Color colorEfecto(String efecto) {
        if ("AURA_AZUL".equals(efecto)) {
            return new Color(0x2E, 0xA8, 0xFF);
        }
        if ("RAYO".equals(efecto)) {
            return new Color(0xFF, 0xE0, 0x3D);
        }
        if ("TORNADO".equals(efecto)) {
            return new Color(0xB9, 0x8A, 0x5A);
        }
        if ("ILUSION".equals(efecto)) {
            return new Color(0xB0, 0x5B, 0xFF);
        }
        return Color.WHITE;
    }
}
