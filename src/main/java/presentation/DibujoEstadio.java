package presentation;

import domain.Cancha;
import domain.Porteria;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Dibuja el estadio: cielo, gradas con público, vallas de publicidad, pasto y
 * los dos arcos. El fondo se pinta una sola vez en una imagen y luego solo se
 * copia en cada cuadro, así el juego va fluido.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
final class DibujoEstadio {

    private static final Color[] CAMISETAS = {
            new Color(0xE5, 0x3B, 0x3B), new Color(0xF5, 0xF5, 0xF5), new Color(0xFF, 0xD3, 0x2E),
            new Color(0x2F, 0x7B, 0xE8), new Color(0xFF, 0x8A, 0x1F), new Color(0x2E, 0xB8, 0x4B),
            new Color(0xE9, 0x5B, 0xA6), new Color(0x7A, 0x4D, 0xE8), new Color(0x1B, 0x2A, 0x57)
    };
    private static final Color[] PIELES = {
            new Color(0xF2, 0xC9, 0xA5), new Color(0xE0, 0xA9, 0x7C), new Color(0xB9, 0x79, 0x4C), new Color(0x8A, 0x5A, 0x3A)
    };

    private DibujoEstadio() {
        // Clase de utilidad.
    }

    /**
     * Crea la imagen de fondo.
     *
     * @param pxAncho  ancho de la imagen en píxeles
     * @param pxAlto   alto de la imagen en píxeles
     * @param logAncho ancho "lógico" que se quiere dibujar
     * @param logAlto  alto "lógico" que se quiere dibujar
     * @param conArcos si dibuja también la parte de atrás de los arcos
     * @return la imagen lista
     */
    static BufferedImage crearFondo(int pxAncho, int pxAlto, double logAncho, double logAlto, boolean conArcos) {
        BufferedImage img = new BufferedImage(Math.max(1, pxAncho), Math.max(1, pxAlto), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        Estilo.calidad(g);
        g.scale(pxAncho / logAncho, pxAlto / logAlto);
        double w = logAncho;
        double h = logAlto;
        double yVallas = h * 0.775;
        double yGradas = 66;

        cielo(g, w, yGradas + 20);
        torresDeLuz(g, w);
        gradas(g, w, yGradas, yVallas - 30);
        vallas(g, w, yVallas - 30, 30);
        pasto(g, w, h, yVallas);
        if (conArcos) {
            dibujarArco(g, new Porteria(Porteria.IZQUIERDA), false);
            dibujarArco(g, new Porteria(Porteria.DERECHA), false);
        }
        g.dispose();
        return img;
    }

    private static void cielo(Graphics2D g, double w, double alto) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x1F, 0x7F, 0xEA), 0, (float) alto, new Color(0xB5, 0xE3, 0xFF)));
        g.fill(new Rectangle2D.Double(0, 0, w, alto));
        // Nubes suaves
        g.setColor(new Color(255, 255, 255, 120));
        double[][] nubes = {{110, 28, 120, 22}, {160, 20, 90, 20}, {620, 24, 140, 20}, {700, 30, 110, 18}, {880, 18, 100, 18}};
        for (double[] n : nubes) {
            g.fill(new Ellipse2D.Double(n[0], n[1], n[2], n[3]));
        }
    }

    private static void torresDeLuz(Graphics2D g, double w) {
        double[] xs = {w * 0.13, w * 0.87};
        for (double x : xs) {
            // Brillo
            g.setPaint(new RadialGradientPaint((float) x, 26f, 70f, new float[]{0f, 1f},
                    new Color[]{new Color(255, 255, 220, 200), new Color(255, 255, 220, 0)}));
            g.fill(new Ellipse2D.Double(x - 70, -44, 140, 140));
            // Poste y panel de focos
            g.setColor(new Color(0x2A, 0x33, 0x4F));
            g.fill(new Rectangle2D.Double(x - 4, 30, 8, 60));
            g.fill(new Rectangle2D.Double(x - 30, 6, 60, 26));
            g.setColor(new Color(255, 250, 210));
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 2; j++) {
                    g.fill(new Ellipse2D.Double(x - 27 + i * 11.5, 9 + j * 11.5, 8, 8));
                }
            }
        }
    }

    private static void gradas(Graphics2D g, double w, double y0, double y1) {
        // Fondo oscuro de las gradas
        g.setPaint(new GradientPaint(0, (float) y0, new Color(0x1B, 0x2A, 0x57), 0, (float) y1, new Color(0x3A, 0x2F, 0x6B)));
        g.fill(new Rectangle2D.Double(0, y0, w, y1 - y0));

        Random azar = new Random(42);
        double paso = 11;
        int fila = 0;
        for (double y = y0 + 10; y < y1 - 4; y += paso, fila++) {
            double desfase = (fila % 2) * 4.5;
            // Escalón de la grada
            for (double x = -4 + desfase; x < w + 4; x += 9) {
                if (azar.nextInt(14) == 0) {
                    continue; // un asiento vacío de vez en cuando
                }
                Color camiseta = CAMISETAS[azar.nextInt(CAMISETAS.length)];
                Color piel = PIELES[azar.nextInt(PIELES.length)];
                double tono = 1 - 0.28 * (1 - (y - y0) / (y1 - y0));
                g.setColor(Estilo.mezclar(camiseta, tono - 1));
                g.fill(new Rectangle2D.Double(x - 3, y + 1.5, 6.5, 6));
                g.setColor(Estilo.mezclar(piel, tono - 1));
                g.fill(new Ellipse2D.Double(x - 2.2, y - 3.2, 4.8, 4.8));
            }
        }
        // Pasillos verticales
        g.setColor(new Color(10, 15, 40, 120));
        for (double x = 160; x < w; x += 170) {
            g.fill(new Rectangle2D.Double(x, y0, 6, y1 - y0));
        }
        // Borde del techo
        g.setColor(new Color(0x14, 0x1B, 0x38));
        g.fill(new Rectangle2D.Double(0, y0 - 8, w, 10));
        g.setColor(new Color(255, 255, 255, 60));
        g.fill(new Rectangle2D.Double(0, y0 - 8, w, 2));
    }

    private static void vallas(Graphics2D g, double w, double y, double alto) {
        String[] textos = {"DOPO SOCCER", "HEAD CUP", "ECI 2026", "GOOOL", "FAIR PLAY"};
        Color[] colores = {Estilo.ROJO, new Color(0xF4, 0xF4, 0xF4), Estilo.AZUL, Estilo.AMARILLO, Estilo.VERDE};
        double ancho = 160;
        int i = 0;
        g.setFont(Estilo.fuente(15f));
        for (double x = 0; x < w; x += ancho, i++) {
            Color c = colores[i % colores.length];
            g.setPaint(new GradientPaint(0, (float) y, Estilo.mezclar(c, 0.15), 0, (float) (y + alto), Estilo.mezclar(c, -0.25)));
            g.fill(new Rectangle2D.Double(x, y, ancho, alto));
            g.setColor(new Color(0, 0, 0, 90));
            g.fill(new Rectangle2D.Double(x, y, 2, alto));
            Color texto = (c.equals(Estilo.AMARILLO) || c.getRed() > 230 && c.getGreen() > 230) ? Estilo.AZUL_NOCHE : Color.WHITE;
            g.setColor(texto);
            String t = textos[i % textos.length];
            double tw = Estilo.ancho(g, g.getFont(), t);
            g.drawString(t, (float) (x + (ancho - tw) / 2), (float) (y + alto * 0.68));
        }
        g.setColor(new Color(255, 255, 255, 120));
        g.fill(new Rectangle2D.Double(0, y, w, 2));
    }

    private static void pasto(Graphics2D g, double w, double h, double y0) {
        g.setPaint(new GradientPaint(0, (float) y0, new Color(0x5F, 0xC8, 0x55), 0, (float) h, new Color(0x2F, 0x9A, 0x3C)));
        g.fill(new Rectangle2D.Double(0, y0, w, h - y0));
        // Franjas de corte del pasto
        double ancho = 62;
        for (int i = 0; i * ancho < w; i++) {
            if (i % 2 == 0) {
                g.setColor(new Color(0, 60, 0, 34));
                g.fill(new Rectangle2D.Double(i * ancho, y0, ancho, h - y0));
            }
        }
        // Brillo cerca de las vallas
        g.setPaint(new GradientPaint(0, (float) y0, new Color(255, 255, 255, 60), 0, (float) (y0 + 26), new Color(255, 255, 255, 0)));
        g.fill(new Rectangle2D.Double(0, y0, w, 26));
        // Líneas de la cancha
        g.setColor(new Color(255, 255, 255, 150));
        g.setStroke(new BasicStroke(3f));
        double yc = Cancha.SUELO + 8;
        g.setColor(new Color(255, 255, 255, 120));
        g.draw(new Line2D.Double(0, y0 + 14, w, y0 + 14));
    }

    /**
     * Dibuja un arco: red, travesaño y postes.
     *
     * @param g      el pincel (en coordenadas lógicas)
     * @param arco   el arco del dominio
     * @param frente false dibuja la red del fondo; true, el marco por delante
     */
    static void dibujarArco(Graphics2D g, Porteria arco, boolean frente) {
        boolean izquierda = Porteria.IZQUIERDA.equals(arco.getLado());
        double x0 = arco.getXInicio();
        double x1 = arco.getXFin();
        double yTop = arco.getYTravesaño();
        double yPiso = Cancha.SUELO;
        double fondo = izquierda ? x0 : x1;      // el lado de la pared (por donde la red se aleja)
        double borde = izquierda ? x1 : x0;      // el poste de adelante, por donde entra el balón
        double alto = yPiso - yTop;

        if (!frente) {
            // Vista de perfil: la red se ve de lado como una cuadrícula blanca fina sobre el costado del arco.
            Rectangle2D lateral = new Rectangle2D.Double(Math.min(x0, x1), yTop, x1 - x0, alto);
            g.setColor(new Color(255, 255, 255, 28));
            g.fill(lateral);
            g.setColor(new Color(255, 255, 255, 175));
            g.setStroke(new BasicStroke(1.2f));
            double paso = 9;
            for (double xx = borde; Math.abs(xx - fondo) > 0.5 && (izquierda ? xx > fondo : xx < fondo); xx += izquierda ? -paso : paso) {
                g.draw(new Line2D.Double(xx, yTop, xx, yPiso));
            }
            for (double yy = yTop; yy <= yPiso; yy += paso) {
                g.draw(new Line2D.Double(x0, yy, x1, yy));
            }
            // Techo de la red: una tira inclinada hacia atrás, como se ve en la foto de referencia
            Path2D.Double techo = new Path2D.Double();
            techo.moveTo(borde, yTop);
            techo.lineTo(fondo, yTop);
            techo.lineTo(fondo, yTop - 16);
            techo.lineTo(borde + (izquierda ? -10 : 10), yTop - 16);
            techo.closePath();
            g.setColor(new Color(255, 255, 255, 40));
            g.fill(techo);
            // La tira del techo también es de malla: cuadrícula fina recortada a su forma
            Shape recorte = g.getClip();
            g.clip(techo);
            g.setColor(new Color(255, 255, 255, 175));
            g.setStroke(new BasicStroke(1.1f));
            for (double xx = Math.min(x0, x1) - 12; xx <= Math.max(x0, x1) + 12; xx += 6) {
                g.draw(new Line2D.Double(xx, yTop - 16, xx, yTop));
            }
            for (double yy = yTop - 16; yy <= yTop; yy += 5) {
                g.draw(new Line2D.Double(Math.min(x0, x1) - 12, yy, Math.max(x0, x1) + 12, yy));
            }
            g.setClip(recorte);
            g.setColor(new Color(255, 255, 255, 190));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(techo);
            g.setColor(new Color(255, 255, 255, 215));
            g.setStroke(new BasicStroke(2.4f));
            g.draw(new Line2D.Double(x0, yPiso, x1, yPiso));
        } else {
            // Travesaño y postes (marco blanco con volumen)
            g.setPaint(new GradientPaint(0, (float) yTop, Color.WHITE, 0, (float) (yTop + Porteria.GROSOR), new Color(0xC8, 0xD2, 0xE4)));
            g.fill(new Rectangle2D.Double(x0, yTop, x1 - x0, Porteria.GROSOR));
            double px = izquierda ? x1 - 8 : x0;
            g.setPaint(new GradientPaint((float) px, 0, Color.WHITE, (float) (px + 8), 0, new Color(0xC8, 0xD2, 0xE4)));
            g.fill(new Rectangle2D.Double(px, yTop, 8, yPiso - yTop));
            g.setColor(new Color(0x9A, 0xA8, 0xC4));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new Rectangle2D.Double(x0, yTop, x1 - x0, Porteria.GROSOR));
            g.draw(new Rectangle2D.Double(px, yTop, 8, yPiso - yTop));
        }
    }

    /**
     * Pinta confeti u otros puntos de luz sobre el fondo (cámaras del público).
     *
     * @param g      el pincel
     * @param x      posición
     * @param y      posición
     * @param brillo entre 0 y 1
     */
    static void destello(Graphics2D g, double x, double y, double brillo) {
        GeneralPath estrella = new GeneralPath();
        double r = 7 * brillo;
        estrella.moveTo(x, y - r);
        estrella.quadTo(x, y, x + r, y);
        estrella.quadTo(x, y, x, y + r);
        estrella.quadTo(x, y, x - r, y);
        estrella.quadTo(x, y, x, y - r);
        g.setColor(new Color(255, 255, 255, (int) (230 * brillo)));
        g.fill(estrella);
    }
}
