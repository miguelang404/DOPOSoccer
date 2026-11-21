package presentation;

import domain.Cancha;
import domain.Partido;

import java.awt.*;

/**
 * Antes de empezar: tipo de partido (por tiempo o por goles), cuánto dura o a
 * cuántos goles se juega, y el tamaño de la ventana de juego.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class PantallaConfigPartido extends PantallaBase {

    PantallaConfigPartido(VentanaPrincipal ventana) {
        super(ventana, false);
    }

    @Override
    protected void pintar(Graphics2D g, double t) {
        velo(g, 135);
        botonVolver(g, ventana::irAPersonajes);
        titulo(g, "CONFIGURA EL PARTIDO", 78);
        tarjeta(g, 120, 100, 760, 366, Estilo.AZUL_CLARO, false);

        boolean porTiempo = Partido.TIPO_TIEMPO.equals(ventana.getTipo());

        // Tipo de partido
        Estilo.textoCentrado(g, "TIPO DE PARTIDO", Estilo.fuente(22), 500, 142, new Color(0xBF, 0xD6, 0xFF));
        boton(g, "POR TIEMPO", porTiempo ? Estilo.VERDE : Estilo.mezclar(Estilo.AZUL, -0.2), 190, 156, 290, 54, true,
                () -> ventana.setTipo(Partido.TIPO_TIEMPO));
        boton(g, "POR GOLES", !porTiempo ? Estilo.VERDE : Estilo.mezclar(Estilo.AZUL, -0.2), 520, 156, 290, 54, true,
                () -> ventana.setTipo(Partido.TIPO_GOLES));

        // Valor
        String etiqueta = porTiempo ? "MINUTOS DE JUEGO" : "GOLES PARA GANAR";
        int valor = porTiempo ? ventana.getMinutos() : ventana.getGoles();
        Estilo.textoCentrado(g, etiqueta, Estilo.fuente(22), 500, 252, new Color(0xBF, 0xD6, 0xFF));
        boton(g, "−", Estilo.NARANJA, 330, 266, 70, 62, true, () -> ventana.cambiarValor(-1));
        boton(g, "+", Estilo.NARANJA, 600, 266, 70, 62, true, () -> ventana.cambiarValor(1));
        Estilo.textoContorno(g, String.valueOf(valor), Estilo.fuente(64), 500, 322,
                Estilo.AMARILLO, Estilo.NARANJA, new Color(0x4A, 0x20, 0x00), 5f);
        String pista = porTiempo
                ? "Entre 1 y 10 minutos. Si hay empate, gana el siguiente gol (muerte súbita)."
                : "Entre 1 y 20 goles. Gana quien llegue primero.";
        texto(g, pista, 15, 500, 352, new Color(0xD8, 0xE6, 0xFF));

        // Resolución
        Estilo.textoCentrado(g, "TAMAÑO DE LA VENTANA", Estilo.fuente(22), 500, 392, new Color(0xBF, 0xD6, 0xFF));
        int n = Cancha.cantidadResoluciones();
        double ancho = 220;
        double sep = 18;
        double inicio = 500 - (n * ancho + (n - 1) * sep) / 2;
        for (int i = 0; i < n; i++) {
            final int indice = i;
            String nombre = Cancha.nombreResolucion(i);
            boolean sel = ventana.getResolucion() == i;
            boton(g, nombre, sel ? Estilo.VERDE : Estilo.mezclar(Estilo.AZUL, -0.2), inicio + i * (ancho + sep), 404,
                    ancho, 46, true, () -> ventana.setResolucion(indice));
        }

        boton(g, "¡A JUGAR!", Estilo.VERDE, 370, 482, 260, 56, true, ventana::iniciarPartido);
    }
}
