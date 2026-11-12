package presentation;

import domain.Jugador;

import java.awt.*;
import java.awt.geom.*;

/**
 * Dibuja a los personajes: cabeza grande con ojos que siguen el balón, camiseta
 * del país, un pie que patea y el aura del poder.
 * Todo con formas, sin imágenes, así que se ve nítido en cualquier resolución.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
final class DibujoPersonaje {

    static final int RADIO = 46;
    static final int ALTURA_CENTRO = 88;


    private DibujoPersonaje() {
        // Clase de utilidad.
    }

    /**
     * Colores de un personaje: camiseta, adorno, pantaloneta, medias, piel y pelo.
     *
     * @param codigo  COREA, CAMERUN, NIGERIA o EEUU
     * @param alterno si usa la camiseta de visitante
     * @return seis colores en ese orden
     */
    static Color[] colores(String codigo, boolean alterno) {
        Color jersey;
        Color adorno;
        Color short1;
        Color medias;
        Color piel;
        Color pelo;
        if ("COREA".equals(codigo)) {
            jersey = alterno ? new Color(0xF4, 0xF4, 0xF4) : new Color(0xD6, 0x23, 0x2A);
            adorno = alterno ? new Color(0xD6, 0x23, 0x2A) : new Color(0x1D, 0x3F, 0x8F);
            short1 = alterno ? new Color(0xD6, 0x23, 0x2A) : new Color(0x1D, 0x3F, 0x8F);
            medias = alterno ? new Color(0xF4, 0xF4, 0xF4) : new Color(0xD6, 0x23, 0x2A);
            piel = new Color(0xF0, 0xC8, 0xA0);
            pelo = new Color(0x18, 0x18, 0x22);
        } else if ("CAMERUN".equals(codigo)) {
            jersey = alterno ? new Color(0xFC, 0xD1, 0x16) : new Color(0x0C, 0x8F, 0x3C);
            adorno = alterno ? new Color(0x0C, 0x8F, 0x3C) : new Color(0xFC, 0xD1, 0x16);
            short1 = alterno ? new Color(0x0C, 0x8F, 0x3C) : new Color(0xCE, 0x11, 0x26);
            medias = alterno ? new Color(0xFC, 0xD1, 0x16) : new Color(0xFC, 0xD1, 0x16);
            piel = new Color(0x8A, 0x5A, 0x3A);
            pelo = new Color(0x14, 0x10, 0x10);
        } else if ("NIGERIA".equals(codigo)) {
            jersey = alterno ? new Color(0xF4, 0xF4, 0xF4) : new Color(0x13, 0x9A, 0x4A);
            adorno = alterno ? new Color(0x13, 0x9A, 0x4A) : new Color(0xF4, 0xF4, 0xF4);
            short1 = alterno ? new Color(0x13, 0x9A, 0x4A) : new Color(0xF4, 0xF4, 0xF4);
            medias = new Color(0x13, 0x9A, 0x4A);
            piel = new Color(0xA9, 0x6F, 0x45);
            pelo = new Color(0x12, 0x10, 0x10);
        } else {
            jersey = alterno ? new Color(0x1F, 0x3A, 0x93) : new Color(0xF4, 0xF4, 0xF4);
            adorno = alterno ? new Color(0xC8, 0x10, 0x2E) : new Color(0x1F, 0x3A, 0x93);
            short1 = alterno ? new Color(0xF4, 0xF4, 0xF4) : new Color(0x1F, 0x3A, 0x93);
            medias = new Color(0xC8, 0x10, 0x2E);
            piel = new Color(0xF4, 0xCF, 0xA8);
            pelo = new Color(0x8A, 0x55, 0x22);
        }
        return new Color[]{jersey, adorno, short1, medias, piel, pelo};
    }

    /**
     * Dibuja un personaje completo.
     *
     * @param g       el pincel
     * @param x       centro de la cabeza
     * @param y       centro de la cabeza
     * @param suelo   y del piso
     * @param codigo  código del personaje
     * @param alterno si usa camiseta de visitante
     * @param mirando 1 derecha, -1 izquierda
     * @param patada  progreso de la patada (0 a 1)
     * @param aura    código del aura activa ("" si no tiene)
     * @param t       tiempo en cuadros, para animar
     */
    static void dibujarGrande(Graphics2D g, double x, double y, double suelo, String codigo, boolean alterno,
                              int mirando, double patada,
                              String aura, double t) {
        dibujarBase(g, x, y, codigo, alterno, mirando, patada, aura, t);
    }

    /**
     * Dibuja al personaje a su tamaño de juego: mide justo lo que mide en el dominio
     * (la mitad de la altura del arco). Se usa dentro de la cancha.
     *
     * @param g       el pincel
     * @param x       centro de la cabeza
     * @param y       centro de la cabeza
     * @param suelo   y del piso
     * @param codigo  código del personaje
     * @param alterno si usa camiseta de visitante
     * @param mirando 1 derecha, -1 izquierda
     * @param patada  progreso de la patada (0 a 1)
     * @param aura    código del aura activa ("" si no tiene)
     * @param t       tiempo en cuadros, para animar
     */
    static void dibujar(Graphics2D g, double x, double y, double suelo, String codigo, boolean alterno,
                        int mirando, double patada,
                        String aura, double t) {
        double k = Jugador.RADIO_CABEZA / (double) RADIO;
        java.awt.geom.AffineTransform original = g.getTransform();
        g.translate(x, y);
        g.scale(k, k);
        g.translate(-x, -y);
        dibujarBase(g, x, y, codigo, alterno, mirando, patada, aura, t);
        g.setTransform(original);
    }

    private static void dibujarBase(Graphics2D g, double x, double y, String codigo, boolean alterno,
                                    int mirando, double patada,
                                    String aura, double t) {
        Color[] c = colores(codigo, alterno);
        Color jersey = c[0];
        Color adorno = c[1];
        Color pantaloneta = c[2];
        Color medias = c[3];
        Color piel = c[4];
        Color pelo = c[5];
        int m = mirando;

        if (!aura.isEmpty()) {
            dibujarAura(g, x, y, aura, t);
        }

        // Piernas y zapatos
        double cadera = y + 58;
        double atrasX = x - m * 16;
        double atrasY = y + 74;
        double frenteX = x + m * (14 + 46 * patada);
        double frenteY = y + 74 - 26 * patada;
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(medias);
        g.draw(new Line2D.Double(x - m * 7, cadera, atrasX, atrasY - 4));
        g.draw(new Line2D.Double(x + m * 7, cadera, frenteX, frenteY - 4));
        zapato(g, atrasX, atrasY, m, jersey, adorno, true);

        // Cuerpo
        RoundRectangle2D torso = new RoundRectangle2D.Double(x - 25, y + 34, 50, 32, 14, 14);
        g.setColor(jersey);
        g.fill(torso);
        g.setColor(adorno);
        if ("CAMERUN".equals(codigo) || "COREA".equals(codigo)) {
            g.fill(new RoundRectangle2D.Double(x - 25, y + 48, 50, 7, 3, 3));
        } else if ("EEUU".equals(codigo)) {
            g.setColor(alterno ? Color.WHITE : new Color(0xC8, 0x10, 0x2E));
            g.fill(new RoundRectangle2D.Double(x - 25, y + 48, 50, 6, 3, 3));
        } else {
            g.setColor(Estilo.conAlfa(adorno, 220));
            for (int i = -2; i <= 2; i++) {
                g.fill(new RoundRectangle2D.Double(x + i * 10 - 2.5, y + 38, 5, 26, 3, 3));
            }
        }
        g.setColor(new Color(0, 0, 0, 55));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(torso);
        g.setColor(pantaloneta);
        g.fill(new RoundRectangle2D.Double(x - 24, y + 60, 48, 15, 8, 8));

        // Pie que patea (delante del cuerpo)
        zapato(g, frenteX, frenteY, m, jersey, adorno, false);

        // Cabeza
        double rad = RADIO;
        Shape cabeza = new Ellipse2D.Double(x - rad, y - rad, rad * 2, rad * 2);
        // Oreja
        g.setColor(Estilo.mezclar(piel, -0.12));
        g.fill(new Ellipse2D.Double(x - m * 41 - 7, y - 4, 14, 22));
        g.setColor(Estilo.mezclar(piel, -0.35));
        g.fill(new Ellipse2D.Double(x - m * 41 - 3.5, y + 1, 7, 12));
        g.setColor(piel);
        g.fill(cabeza);

        // Pelo (recortado dentro de la cabeza)
        Shape clip = g.getClip();
        g.clip(cabeza);
        pelo(g, x, y, m, codigo, pelo);
        g.setClip(clip);

        cara(g, x, y, m, piel);

        // Contorno de la cabeza
        g.setColor(Estilo.mezclar(piel, -0.62));
        g.setStroke(new BasicStroke(3.6f));
        g.draw(cabeza);
    }

    private static void zapato(Graphics2D g, double x, double y, int m, Color jersey, Color adorno, boolean atras) {
        // El zapato mira hacia donde mira el jugador
        double ancho = 38;
        double alto = 19;
        double cx = x + m * 6;
        Shape suela = new RoundRectangle2D.Double(cx - ancho / 2, y + 2, ancho, 7, 6, 6);
        Shape bota = new Ellipse2D.Double(cx - ancho / 2, y - alto / 2 - 2, ancho, alto);
        g.setColor(new Color(0x22, 0x26, 0x33));
        g.fill(suela);
        Color base = atras ? new Color(0xE8, 0xEA, 0xF0) : Color.WHITE;
        g.setColor(base);
        g.fill(bota);
        g.setColor(Estilo.mezclar(adorno, 0));
        g.fill(new Arc2D.Double(cx - ancho / 2 + (m > 0 ? 4 : ancho / 2 - 2), y - alto / 2 + 1, ancho / 2 - 2, alto - 3, m > 0 ? 100 : -80, 160, Arc2D.CHORD));
        g.setColor(new Color(0x23, 0x2B, 0x46));
        g.setStroke(new BasicStroke(2f));
        g.draw(bota);
    }

    private static void pelo(Graphics2D g, double x, double y, int m, String codigo, Color pelo) {
        g.setColor(pelo);
        Path2D.Double casco = new Path2D.Double();
        double lineaPelo;
        if ("CAMERUN".equals(codigo)) {
            lineaPelo = y - 30;
        } else if ("NIGERIA".equals(codigo)) {
            lineaPelo = y - 22;
        } else {
            lineaPelo = y - 17;
        }
        casco.moveTo(x - 60, y - 70);
        casco.lineTo(x + 60, y - 70);
        casco.lineTo(x + 60, lineaPelo);
        // Flequillo ondulado
        int mechas = 7;
        for (int i = 0; i <= mechas; i++) {
            double px = x + 60 - i * (120.0 / mechas);
            double desvio = (i % 2 == 0) ? 0 : 10;
            double barrido = "EEUU".equals(codigo) ? (m * (i - mechas / 2.0) * -1.8) : 0;
            casco.lineTo(px, lineaPelo + desvio + barrido);
        }
        casco.closePath();
        g.fill(casco);
        if ("NIGERIA".equals(codigo)) {
            g.fill(new Ellipse2D.Double(x - 44, y - 62, 88, 44));   // pelo voluminoso
        }
        // Patilla del lado de atrás
        g.fill(new Ellipse2D.Double(x - m * 46 - 8, y - 20, 16, 30));
    }

    /**
     * Cara sencilla y siempre igual: ojos fijos mirando al frente y una rayita de boca.
     */
    private static void cara(Graphics2D g, double x, double y, int m, Color piel) {
        double px = m * 3.2;
        double py = 0;

        double[] ojosX = {x + m * 6, x + m * 29};
        double ojoY = y - 3;
        for (int i = 0; i < 2; i++) {
            double ex = ojosX[i];
            double ancho = i == 0 ? 17 : 19;
            double alto = 23;
            Ellipse2D.Double ojo = new Ellipse2D.Double(ex - ancho / 2, ojoY - alto / 2, ancho, alto);
            g.setColor(Color.WHITE);
            g.fill(ojo);
            g.setColor(new Color(0x20, 0x20, 0x30));
            g.fill(new Ellipse2D.Double(ex - 5.6 + px, ojoY - 5.6 + py + 1, 11.2, 11.2));
            g.setColor(new Color(0x30, 0x20, 0x20));
            g.setStroke(new BasicStroke(2f));
            g.draw(ojo);
        }

        // Boca: una rayita
        g.setColor(new Color(0x3A, 0x14, 0x14));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double bx = x + m * 24;
        g.draw(new Line2D.Double(bx - 8, y + 28, bx + 8, y + 28));
    }

    private static void dibujarAura(Graphics2D g, double x, double y, String aura, double t) {
        Color color = DibujoBalon.colorEfecto(aura);
        double cy = y + 20;
        double pulso = 1 + 0.08 * Math.sin(t * 0.35);
        float radio = (float) (92 * pulso);
        g.setPaint(new RadialGradientPaint((float) x, (float) cy, radio, new float[]{0f, 0.55f, 1f},
                new Color[]{Estilo.conAlfa(color, 30), Estilo.conAlfa(color, 120), Estilo.conAlfa(color, 0)}));
        g.fill(new Ellipse2D.Double(x - radio, cy - radio, radio * 2, radio * 2));
        // Chispas que giran alrededor
        for (int i = 0; i < 8; i++) {
            double a = t * 0.08 + i * Math.PI / 4;
            double rr = 62 + 10 * Math.sin(t * 0.2 + i);
            double sx = x + Math.cos(a) * rr;
            double sy = cy + Math.sin(a) * rr * 0.95;
            g.setColor(Estilo.conAlfa(Estilo.mezclar(color, 0.5), 200));
            g.fill(new Ellipse2D.Double(sx - 3, sy - 3, 6, 6));
        }
        // Llamas / aro
        g.setColor(Estilo.conAlfa(color, 150));
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Double(x - 60 * pulso, cy - 64 * pulso, 120 * pulso, 128 * pulso));
    }
}
