package presentation;

import exceptions.DOPOSoccerExcepcion;

import java.awt.*;

/**
 * Cuando se juega contra la máquina, aquí se escoge cómo juega: Torpe, Ofensiva,
 * Defensiva o Experta.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
class PantallaPerfil extends PantallaBase {

    private static final Color[] COLORES = {Estilo.GRIS, Estilo.ROJO, Estilo.AZUL, Estilo.VERDE};

    PantallaPerfil(VentanaPrincipal ventana) {
        super(ventana, false);
    }

    @Override
    protected void pintar(Graphics2D g, double t) {
        velo(g, 130);
        botonVolver(g, ventana::irAModalidad);
        titulo(g, "ELIGE A LA MÁQUINA", 88);
        String[] perfiles = ventana.getJuego().getPerfiles();
        double w = 190;
        double sep = 20;
        double inicio = 500 - (perfiles.length * w + (perfiles.length - 1) * sep) / 2;
        for (int i = 0; i < perfiles.length; i++) {
            final String perfil = perfiles[i];
            double x = inicio + i * (w + sep);
            double y = 140;
            double h = 300;
            boolean hover = zona(x, y, w, h, () -> ventana.elegirPerfil(perfil));
            tarjeta(g, x, y, w, h, hover ? Estilo.AMARILLO : COLORES[i % COLORES.length], hover);
            Estilo.textoCentrado(g, perfil.toUpperCase(), Estilo.ajustar(g, Estilo.fuente(24), perfil.toUpperCase(), w - 24),
                    x + w / 2, y + 60, Color.WHITE);
            String descripcion = "";
            try {
                descripcion = ventana.getJuego().getDescripcionPerfil(perfil);
            } catch (DOPOSoccerExcepcion e) {
                VentanaPrincipal.registrar("PantallaPerfil.pintar", e);
            }
            Font f = Estilo.fuenteTexto(16);
            g.setFont(f);
            int linea = 0;
            for (String l : partir(g, descripcion, f, w - 36)) {
                texto(g, l, 16, x + w / 2, y + 120 + linea * 24, new Color(0xD8, 0xE6, 0xFF));
                linea++;
            }
        }
    }
}
