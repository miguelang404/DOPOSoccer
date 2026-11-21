package presentation;

import domain.Jugador;
import exceptions.DOPOSoccerExcepcion;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Aquí cada jugador gasta los puntos que ganó en los partidos para mejorar sus
 * estadísticas: velocidad, salto, patada y vida. La primera mejora de cada una
 * cuesta 500 puntos y cada mejora siguiente cuesta el doble.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
class PantallaMejoras extends PantallaBase {

    private static final String[] ESTADISTICAS = {Jugador.SPEED, Jugador.JUMP, Jugador.KICK, Jugador.SURVIVAL};
    private static final String[] NOMBRES = {"Velocidad", "Salto", "Patada", "Vida"};

    PantallaMejoras(VentanaPrincipal ventana) {
        super(ventana, false);
    }

    @Override
    protected void pintar(Graphics2D g, double t) {
        velo(g, 135);
        botonVolver(g, ventana::irAPersonajes);
        titulo(g, "MEJORAS", 78);
        texto(g, "Ganas 100 puntos si ganas y 25 si pierdes. Cada mejora cuesta el doble de la anterior.", 15, 500, 104,
                new Color(0xD8, 0xE6, 0xFF));
        panelJugador(g, ventana.getJuego().getJugador1(), 40, Estilo.AZUL_CLARO);
        panelJugador(g, ventana.getJuego().getJugador2(), 520, Estilo.ROJO);
        boton(g, "LISTO", Estilo.VERDE, 390, 498, 220, 50, true, ventana::irAPersonajes);
    }

    private void panelJugador(Graphics2D g, Jugador j, double x, Color color) {
        double w = 440;
        double y = 118;
        double h = 366;
        tarjeta(g, x, y, w, h, color, false);
        Estilo.textoCentrado(g, j.getNombre().toUpperCase(), Estilo.fuente(24), x + w / 2, y + 38, color);
        Estilo.textoCentrado(g, "Puntos: " + j.getPuntos(), Estilo.fuente(21), x + w / 2, y + 70, Estilo.AMARILLO);

        for (int i = 0; i < ESTADISTICAS.length; i++) {
            fila(g, j, ESTADISTICAS[i], NOMBRES[i], x, y + 92 + i * 66);
        }
    }

    private void fila(Graphics2D g, Jugador j, String estadistica, String nombre, double x, double y) {
        int nivel = j.getSpeed();
        int costo = -1;
        try {
            nivel = j.getNivel(estadistica);
            costo = j.getCostoMejora(estadistica);
        } catch (DOPOSoccerExcepcion e) {
            VentanaPrincipal.registrar("PantallaMejoras.fila", e);
        }

        // Nombre y valor actual
        g.setFont(Estilo.fuente(18));
        g.setColor(Color.WHITE);
        g.drawString(nombre, (float) (x + 28), (float) (y + 22));
        g.setFont(Estilo.fuenteTexto(14));
        g.setColor(new Color(0xBF, 0xD6, 0xFF));
        g.drawString(valorActual(j, estadistica), (float) (x + 28), (float) (y + 44));

        // Los 10 puntitos del nivel
        for (int p = 0; p < 10; p++) {
            boolean lleno = p < nivel;
            double px = x + 168 + p * 14;
            g.setColor(lleno ? Estilo.AMARILLO : new Color(255, 255, 255, 50));
            g.fill(new RoundRectangle2D.Double(px, y + 10, 11, 22, 4, 4));
        }

        // Botón de mejora
        boolean max = costo < 0;
        boolean alcanza = !max && j.puedeMejorar(estadistica);
        String texto = max ? "MÁX" : "+ " + costo;
        Color color = alcanza ? Estilo.NARANJA : Estilo.GRIS;
        boton(g, texto, color, x + 328, y + 2, 88, 40, alcanza, () -> ventana.mejorarEstadistica(j, estadistica));
    }

    private String valorActual(Jugador j, String estadistica) {
        if (Jugador.SPEED.equals(estadistica)) {
            return "×" + decimal(j.getMultiplicadorVelocidad());
        } else if (Jugador.JUMP.equals(estadistica)) {
            return "×" + decimal(j.getMultiplicadorSalto());
        } else if (Jugador.KICK.equals(estadistica)) {
            return "daño de patada " + decimal(Jugador.DANO_PATADA_BASE * j.getMultiplicadorKick());
        }
        return "vida máxima " + Math.round(j.getVidaMaxima());
    }

    private String decimal(double valor) {
        return String.format("%.1f", valor).replace('.', ',');
    }
}
