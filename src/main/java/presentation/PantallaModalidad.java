package presentation;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

/**
 * Pantalla donde se elige cómo jugar: jugador contra jugador o contra la máquina
 * (esta última se ve, pero queda bloqueada hasta la versión dos).
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class PantallaModalidad extends PantallaBase {

    PantallaModalidad(VentanaPrincipal ventana) {
        super(ventana, false);
    }

    @Override
    protected void pintar(Graphics2D g, double t) {
        velo(g, 120);
        titulo(g, "ELIGE LA MODALIDAD", 88);

        // Tarjeta 1: jugador vs jugador
        double x1 = 90;
        double x2 = 530;
        double y = 135;
        double w = 380;
        double h = 290;
        boolean hover = zona(x1, y, w, h, ventana::irAPersonajes);
        tarjeta(g, x1, y, w, h, hover ? Estilo.AMARILLO : Estilo.AZUL_CLARO, hover);
        miniatura(g, x1 + w / 2, y + 40, "COREA", "CAMERUN", t, hover);
        Estilo.textoCentrado(g, "JUGADOR vs JUGADOR", Estilo.fuente(28), x1 + w / 2, y + 228, Color.WHITE);
        texto(g, "Dos jugadores en el mismo teclado", 17, x1 + w / 2, y + 258, new Color(0xBF, 0xD6, 0xFF));

        // Tarjeta 2: jugador vs máquina (bloqueada)
        tarjeta(g, x2, y, w, h, Estilo.GRIS, false);
        miniatura(g, x2 + w / 2, y + 40, "NIGERIA", "EEUU", t, false);
        g.setColor(new Color(10, 16, 36, 150));
        g.fill(new RoundRectangle2D.Double(x2 + 3, y + 3, w - 6, h - 6, 30, 30));
        Estilo.textoCentrado(g, "JUGADOR vs MÁQUINA", Estilo.fuente(28), x2 + w / 2, y + 228, new Color(0xB5, 0xBC, 0xCC));
        // candado y cinta
        double cx = x2 + w / 2;
        g.setColor(Estilo.AMARILLO);
        g.fill(new RoundRectangle2D.Double(cx - 24, y + 108, 48, 40, 10, 10));
        g.setStroke(new java.awt.BasicStroke(8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.Arc2D.Double(cx - 15, y + 76, 30, 50, 0, 180, java.awt.geom.Arc2D.OPEN));
        g.setColor(new Color(0x7A, 0x5A, 0x00));
        g.fill(new java.awt.geom.Ellipse2D.Double(cx - 5, y + 120, 10, 10));
        texto(g, "Llega en la versión 2", 17, cx, y + 258, Estilo.AMARILLO);

        boton(g, "VOLVER", Estilo.ROJO, 40, 468, 200, 56, true, ventana::irAPortada);
    }

    /**
     * Dibuja dos personajes pequeños frente a frente dentro de la tarjeta.
     */
    private void miniatura(Graphics2D g, double cx, double y, String a, String b, double t, boolean salto) {
        AffineTransform original = g.getTransform();
        g.translate(cx, y);
        g.scale(0.9, 0.9);
        double brinco = salto ? Math.abs(Math.sin(t * 0.12)) * 18 : 0;
        DibujoPersonaje.dibujarGrande(g, -85, 70 - brinco, 180, a, false, 1, 0, "", t);
        DibujoPersonaje.dibujarGrande(g, 85, 70 - brinco, 180, b, false, -1, 0, "", t);
        DibujoBalon.dibujar(g, 0, 140 - brinco, 20, t * 0.05, "", 0, 0, t);
        g.setTransform(original);
    }
}
