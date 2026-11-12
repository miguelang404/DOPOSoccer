package presentation;

import domain.Personaje;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Cada jugador elige a su personaje. Se puede con el mouse o con el teclado:
 * A y D para el jugador 1, las flechas para el jugador 2.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class PantallaPersonajes extends PantallaBase {

    PantallaPersonajes(VentanaPrincipal ventana) {
        super(ventana, false);
        atajo("A", 0, -1);
        atajo("D", 0, 1);
        atajo("LEFT", 1, -1);
        atajo("RIGHT", 1, 1);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "volver");
        getActionMap().put("volver", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ventana.irAModalidad();
            }
        });
    }

    private void atajo(String tecla, final int jugador, final int paso) {
        String clave = "mover" + tecla;
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), clave);
        getActionMap().put(clave, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = ventana.getJuego().getPersonajes().length;
                ventana.elegirPersonaje(jugador, (ventana.getPersonajeElegido(jugador) + paso + n) % n);
            }
        });
    }

    @Override
    protected void pintar(Graphics2D g, double t) {
        velo(g, 130);
        titulo(g, "ELIGE TU PERSONAJE", 78);
        panelJugador(g, 0, 40, Estilo.AZUL_CLARO, "JUGADOR 1", "A  /  D", t);
        panelJugador(g, 1, 520, Estilo.ROJO, "JUGADOR 2", "←  /  →", t);
        boton(g, "VOLVER", Estilo.ROJO, 40, 488, 200, 56, true, ventana::irAModalidad);
        boton(g, "CONTINUAR", Estilo.VERDE, 740, 488, 220, 56, true, ventana::irAConfigPartido);
    }

    private void panelJugador(Graphics2D g, int jugador, double x, Color color, String titulo, String teclas, double t) {
        double w = 440;
        double y = 96;
        double h = 380;
        tarjeta(g, x, y, w, h, color, false);
        Personaje[] lista = ventana.getJuego().getPersonajes();
        int elegido = ventana.getPersonajeElegido(jugador);
        Personaje p = lista[elegido];
        double cx = x + w / 2;

        Estilo.textoCentrado(g, titulo, Estilo.fuente(26), cx, y + 36, color);
        texto(g, "Teclas: " + teclas, 14, cx, y + 56, new Color(0xBF, 0xD6, 0xFF));

        // Vitrina con el personaje en grande
        g.setPaint(new java.awt.RadialGradientPaint((float) cx, (float) (y + 150), 130f, new float[]{0f, 1f},
                new Color[]{Estilo.conAlfa(color, 110), Estilo.conAlfa(color, 0)}));
        g.fill(new Ellipse2D.Double(cx - 130, y + 10, 260, 260));
        boolean alterno = jugador == 1 && ventana.getPersonajeElegido(0) == elegido;
        AffineTransform original = g.getTransform();
        g.translate(cx, y + 62);
        g.scale(1.0, 1.0);
        double mirada = jugador == 0 ? 1 : -1;
        DibujoPersonaje.dibujarGrande(g, 0, 48 + Math.sin(t * 0.08) * 3, 168, p.getCodigo(), alterno, (int) mirada, 0,
                p.getPoder().getCodigo(), t);
        g.setTransform(original);

        // Bandera y nombre
        DibujoBandera.dibujar(g, p.getCodigo(), x + 22, y + 22, 54, 36);
        Estilo.textoCentrado(g, p.getPais().toUpperCase(), Estilo.fuente(30), cx, y + 254, Color.WHITE);
        Estilo.textoCentrado(g, "Poder: " + p.getPoder().getNombre(), Estilo.fuente(20), cx, y + 278,
                Estilo.mezclar(DibujoBalon.colorEfecto(p.getPoder().getCodigo()), 0.35));
        Font f = Estilo.fuenteTexto(15);
        g.setFont(f);
        int linea = 0;
        for (String l : partir(g, p.getPoder().getDescripcion(), f, w - 60)) {
            if (linea < 2) {
                texto(g, l, 15, cx, y + 300 + linea * 18, new Color(0xD8, 0xE6, 0xFF));
            }
            linea++;
        }

        // Selector con las cuatro banderas
        double tam = 72;
        double sep = 14;
        double inicio = cx - (lista.length * tam + (lista.length - 1) * sep) / 2;
        for (int i = 0; i < lista.length; i++) {
            final int indice = i;
            double bx = inicio + i * (tam + sep);
            double by = y + h - 34;
            boolean sel = i == elegido;
            boolean hover = zona(bx, by - 22, tam, 54, () -> ventana.elegirPersonaje(jugador, indice));
            if (sel) {
                g.setColor(Estilo.conAlfa(color, 130));
                g.fill(new RoundRectangle2D.Double(bx - 6, by - 28, tam + 12, 66, 18, 18));
            }
            DibujoBandera.dibujar(g, lista[i].getCodigo(), bx + 6, by - 18, tam - 12, 46 - 8);
            g.setColor(sel ? Color.WHITE : (hover ? Estilo.AMARILLO : new Color(255, 255, 255, 120)));
            g.setStroke(new java.awt.BasicStroke(sel ? 4f : 2f));
            g.draw(new RoundRectangle2D.Double(bx + 5, by - 19, tam - 10, 40, 6, 6));
        }
    }
}
