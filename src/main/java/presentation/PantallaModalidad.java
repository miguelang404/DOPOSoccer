package presentation;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Pantalla donde se elige cómo jugar: jugador contra jugador o contra la máquina
 * (en ese caso después se escoge el perfil de la máquina).
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
class PantallaModalidad extends PantallaBase {

    PantallaModalidad(VentanaPrincipal ventana) {
        super(ventana, false);
    }

    @Override
    protected void pintar(Graphics2D g, double t) {
        velo(g, 120);
        botonVolver(g, ventana::irAPortada);
        titulo(g, "ELIGE LA MODALIDAD", 88);

        // Tarjeta 1: jugador vs jugador
        double x1 = 90;
        double x2 = 530;
        double y = 150;
        double w = 380;
        double h = 280;
        boolean hover = zona(x1, y, w, h, ventana::elegirContraJugador);
        tarjeta(g, x1, y, w, h, hover ? Estilo.AMARILLO : Estilo.AZUL_CLARO, hover);
        miniatura(g, x1 + w / 2, y + 40, "COREA", "CAMERUN", t, hover);
        Estilo.textoCentrado(g, "JUGADOR vs JUGADOR", Estilo.ajustar(g, Estilo.fuente(22), "JUGADOR vs MÁQUINA", w - 50),
                x1 + w / 2, y + 240, Color.WHITE);

        // Tarjeta 2: jugador vs máquina
        boolean hover2 = zona(x2, y, w, h, ventana::elegirContraMaquina);
        tarjeta(g, x2, y, w, h, hover2 ? Estilo.AMARILLO : Estilo.ROJO, hover2);
        miniatura(g, x2 + w / 2, y + 40, "NIGERIA", "EEUU", t, hover2);
        Estilo.textoCentrado(g, "JUGADOR vs MÁQUINA", Estilo.ajustar(g, Estilo.fuente(22), "JUGADOR vs MÁQUINA", w - 50),
                x2 + w / 2, y + 240, Color.WHITE);
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
