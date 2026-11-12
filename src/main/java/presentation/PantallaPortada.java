package presentation;

import domain.Cancha;

import java.awt.*;

/**
 * La portada: el título del juego, dos jugadores esperando y el menú principal.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class PantallaPortada extends PantallaBase {

    PantallaPortada(VentanaPrincipal ventana) {
        super(ventana, true);
    }

    @Override
    protected void pintar(Graphics2D g, double t) {
        velo(g, 70);
        double suelo = Cancha.SUELO;

        // Dos jugadores de adorno, mirando al centro, y un balón que rebota entre ellos.
        double rebote = Math.abs(Math.sin(t * 0.045));
        double bolaY = suelo - 22 - rebote * 150;
        DibujoPersonaje.dibujarGrande(g, 175, suelo - 88, suelo, "COREA", false, 1, 0, "AURA_AZUL", t);
        DibujoPersonaje.dibujarGrande(g, 825, suelo - 88, suelo, "EEUU", false, -1, 0, "RAYO", t);

        // Título
        double flota = Math.sin(t * 0.06) * 4;
        Estilo.textoContorno(g, "HEAD", Estilo.fuente(104), 500, 128 + flota,
                Estilo.AMARILLO, Estilo.NARANJA, new Color(0x4A, 0x20, 0x00), 7f);
        Estilo.textoContorno(g, "SOCCER", Estilo.fuente(104), 500, 226 + flota,
                new Color(0x9B, 0xE7, 0xFF), Estilo.AZUL, new Color(0x06, 0x1A, 0x55), 7f);
        texto(g, "DOPOSoccer  ·  Versión 1", 18, 500, 266, new Color(0xD8, 0xE6, 0xFF));

        boton(g, "JUGAR", Estilo.VERDE, 350, 292, 300, 62, true, ventana::irAModalidad);
        boton(g, "CONFIGURACIÓN", Estilo.AZUL, 350, 363, 300, 62, true, ventana::irAConfiguracion);
        boton(g, "SALIR", Estilo.ROJO, 350, 434, 300, 62, true, ventana::salir);

        texto(g, "Escuela Colombiana de Ingeniería Julio Garavito  ·  DOPO 2026-2", 15, 500, 538,
                new Color(255, 255, 255, 200));
    }
}
