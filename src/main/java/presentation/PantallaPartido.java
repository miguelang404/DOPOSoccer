package presentation;

import domain.Balon;
import domain.Cancha;
import domain.Jugador;
import domain.Partido;

import javax.swing.Timer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * La cancha en acción. Esta clase no calcula nada del juego: cada 1/60 de
 * segundo le pasa las teclas al dominio, le pide que avance un tick y luego
 * dibuja lo que el dominio le cuenta (jugadores, balón, marcador, poderes).
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class PantallaPartido extends PantallaBase {

    // Controles del jugador 1
    private static final int P1_IZQUIERDA = KeyEvent.VK_A;
    private static final int P1_DERECHA = KeyEvent.VK_D;
    private static final int P1_SALTO = KeyEvent.VK_W;
    private static final int P1_PATADA = KeyEvent.VK_F;
    private static final int P1_PODER = KeyEvent.VK_G;
    // Controles del jugador 2
    private static final int P2_IZQUIERDA = KeyEvent.VK_LEFT;
    private static final int P2_DERECHA = KeyEvent.VK_RIGHT;
    private static final int P2_SALTO = KeyEvent.VK_UP;
    private static final int P2_PATADA = KeyEvent.VK_PERIOD;
    private static final int P2_PODER = KeyEvent.VK_COMMA;

    private static final double SEGUNDO_POR_TICK = 1.0 / Partido.TICKS_POR_SEGUNDO;

    /**
     * Un papelito de confeti para celebrar los goles.
     */
    private static final class Confeti {
        double x;
        double y;
        double vx;
        double vy;
        double giro;
        Color color;
    }

    private final Set<Integer> teclas = new HashSet<Integer>();
    private final List<Confeti> confeti = new ArrayList<Confeti>();
    private final Random azar = new Random();
    private final Timer bucle;

    private Partido partido;
    private long ultimoInstante;
    private double acumulado;
    private int ticksEnCurso;
    private String estadoPrevio = "";
    private int cuentaPrevia;
    private boolean patada1Previa;
    private boolean patada2Previa;
    private boolean poder1Previo;
    private boolean poder2Previo;
    private boolean efectoPrevio;
    private double vyPrevia;
    private boolean poderPresionado1;
    private boolean poderPresionado2;

    PantallaPartido(VentanaPrincipal ventana) {
        super(ventana, true);
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                teclas.add(e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_P) {
                    alternarPausa();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                teclas.remove(e.getKeyCode());
            }
        });
        bucle = new Timer(8, e -> avanzar());
    }

    /**
     * Pone un partido nuevo en la pantalla y arranca el bucle.
     *
     * @param nuevo el partido ya creado por el dominio
     */
    void mostrarPartido(Partido nuevo) {
        partido = nuevo;
        teclas.clear();
        confeti.clear();
        ticksEnCurso = 0;
        estadoPrevio = "";
        cuentaPrevia = 0;
        t = 0;
        acumulado = 0;
        ultimoInstante = System.nanoTime();
        patada1Previa = false;
        patada2Previa = false;
        poder1Previo = false;
        poder2Previo = false;
        efectoPrevio = false;
    }

    @Override
    void activar() {
        super.activar();
        ultimoInstante = System.nanoTime();
        bucle.start();
    }

    @Override
    void desactivar() {
        super.desactivar();
        bucle.stop();
    }

    private void alternarPausa() {
        if (partido == null) {
            return;
        }
        if (Partido.ESTADO_PAUSADO.equals(partido.getEstado())) {
            partido.reanudar();
        } else {
            partido.pausar();
        }
        ultimoInstante = System.nanoTime();
    }

    // el bucle

    private void avanzar() {
        // Recomendación de la IA generativa para el bucle del juego: paso de tiempo fijo. Se acumula el
        // tiempo real que pasó y se ejecutan los ticks de 1/60 de segundo que quepan, con un máximo de
        // 5 por vuelta para no congelar la ventana si el computador se atrasa.
        if (partido == null) {
            return;
        }
        long ahora = System.nanoTime();
        acumulado += Math.min(0.1, (ahora - ultimoInstante) / 1e9);
        ultimoInstante = ahora;
        int vueltas = 0;
        while (acumulado >= SEGUNDO_POR_TICK && vueltas < 5) {
            tick();
            acumulado -= SEGUNDO_POR_TICK;
            vueltas++;
        }
        repaint();
    }

    private void tick() {
        Jugador j1 = partido.getJugador1();
        Jugador j2 = partido.getJugador2();
        if (Partido.ESTADO_EN_CURSO.equals(partido.getEstado())) {
            j1.mover((teclas.contains(P1_DERECHA) ? 1 : 0) - (teclas.contains(P1_IZQUIERDA) ? 1 : 0));
            j2.mover((teclas.contains(P2_DERECHA) ? 1 : 0) - (teclas.contains(P2_IZQUIERDA) ? 1 : 0));
            if (teclas.contains(P1_SALTO)) {
                j1.saltar();
            }
            if (teclas.contains(P2_SALTO)) {
                j2.saltar();
            }
            if (teclas.contains(P1_PATADA)) {
                j1.patear();
            }
            if (teclas.contains(P2_PATADA)) {
                j2.patear();
            }
            // El poder se activa al apretar la tecla, no mientras se mantiene.
            boolean p1 = teclas.contains(P1_PODER);
            boolean p2 = teclas.contains(P2_PODER);
            if (p1 && !poderPresionado1) {
                j1.activarPoder();
            }
            if (p2 && !poderPresionado2) {
                j2.activarPoder();
            }
            poderPresionado1 = p1;
            poderPresionado2 = p2;
            ticksEnCurso++;
        }

        double vyAntes = partido.getBalon().getVy();
        partido.actualizar();
        t++;
        sonidosYEventos(vyAntes);

        if (Partido.ESTADO_GOL.equals(partido.getEstado())) {
            moverConfeti();
        } else if (!confeti.isEmpty() && !Partido.ESTADO_TERMINADO.equals(partido.getEstado())) {
            confeti.clear();
        }
        if (Partido.ESTADO_ABANDONADO.equals(partido.getEstado())) {
            ventana.irAPortada();
        }
    }

    private void sonidosYEventos(double vyAntes) {
        String estado = partido.getEstado();
        Jugador j1 = partido.getJugador1();
        Jugador j2 = partido.getJugador2();
        Balon b = partido.getBalon();

        if (Partido.ESTADO_CUENTA.equals(estado) && partido.getNumeroCuenta() != cuentaPrevia) {
            cuentaPrevia = partido.getNumeroCuenta();
            Sonidos.reproducir("cuenta");
        }
        if (!estado.equals(estadoPrevio)) {
            if (Partido.ESTADO_EN_CURSO.equals(estado) && Partido.ESTADO_CUENTA.equals(estadoPrevio)) {
                Sonidos.reproducir("silbato");
                ticksEnCurso = 0;
            } else if (Partido.ESTADO_GOL.equals(estado)) {
                Sonidos.reproducir("gol");
                lanzarConfeti();
            } else if (Partido.ESTADO_TERMINADO.equals(estado)) {
                Sonidos.reproducir("silbato");
                lanzarConfeti();
            }
            if (Partido.ESTADO_EN_CURSO.equals(estado) && Partido.ESTADO_GOL.equals(estadoPrevio)) {
                cuentaPrevia = 0;
            }
            estadoPrevio = estado;
        }
        if (j1.estaPateando() && !patada1Previa || j2.estaPateando() && !patada2Previa) {
            Sonidos.reproducir("patada");
        }
        patada1Previa = j1.estaPateando();
        patada2Previa = j2.estaPateando();
        boolean efecto = !b.getEfecto().isEmpty();
        if ((efecto && !efectoPrevio) || (j1.isPoderActivo() && !poder1Previo) || (j2.isPoderActivo() && !poder2Previo)) {
            Sonidos.reproducir("poder");
        }
        efectoPrevio = efecto;
        poder1Previo = j1.isPoderActivo();
        poder2Previo = j2.isPoderActivo();
        if (vyAntes > 3 && b.getVy() < 0 && Partido.ESTADO_EN_CURSO.equals(estado)) {
            Sonidos.reproducir("rebote");
        }
    }

    private void lanzarConfeti() {
        Color[] paleta = {Estilo.AMARILLO, Estilo.ROJO, Estilo.AZUL_CLARO, Estilo.VERDE, Color.WHITE, Estilo.NARANJA};
        for (int i = 0; i < 150; i++) {
            Confeti c = new Confeti();
            c.x = 100 + azar.nextDouble() * 800;
            c.y = -20 - azar.nextDouble() * 120;
            c.vx = (azar.nextDouble() - 0.5) * 3;
            c.vy = 1.5 + azar.nextDouble() * 3.5;
            c.giro = azar.nextDouble() * 6;
            c.color = paleta[azar.nextInt(paleta.length)];
            confeti.add(c);
        }
    }

    private void moverConfeti() {
        for (Confeti c : confeti) {
            c.x += c.vx + Math.sin(t * 0.05 + c.giro) * 0.8;
            c.y += c.vy;
            c.giro += 0.15;
        }
    }

    // dibujo

    @Override
    protected void pintar(Graphics2D g, double tiempo) {
        if (partido == null) {
            return;
        }
        double f = t;
        Jugador j1 = partido.getJugador1();
        Jugador j2 = partido.getJugador2();
        Balon balon = partido.getBalon();
        String estado = partido.getEstado();

        // Atrás de los jugadores: la red de los arcos ya viene en el fondo.
        for (Balon falso : partido.getSeñuelos()) {
            DibujoBalon.dibujar(g, falso.getX(), falso.getY(), Balon.RADIO, falso.getGiro(), "ILUSION",
                    falso.getVx(), falso.getVy(), f);
        }

        boolean alterno = j1.getPersonaje().getCodigo().equals(j2.getPersonaje().getCodigo());
        dibujarJugador(g, j1, false, f);
        dibujarJugador(g, j2, alterno, f);
        DibujoBalon.dibujar(g, balon.getX(), balon.getY(), Balon.RADIO, balon.getGiro(), balon.getEfecto(),
                balon.getVx(), balon.getVy(), f);

        DibujoEstadio.dibujarArco(g, partido.getPorteriaIzquierda(), true);
        DibujoEstadio.dibujarArco(g, partido.getPorteriaDerecha(), true);

        dibujarConfeti(g);
        dibujarMarcador(g, j1, j2, f);

        // Teclas de ayuda abajo
        texto(g, "J1:  A D mover · W saltar · F patear · G poder", 14, 250, 540, new Color(255, 255, 255, 190));
        texto(g, "J2:  ← → mover · ↑ saltar · . patear · , poder", 14, 750, 540, new Color(255, 255, 255, 190));
        texto(g, "ESC pausa", 13, 500, 540, new Color(255, 255, 255, 150));

        if (Partido.ESTADO_CUENTA.equals(estado)) {
            cuentaAtras(g, f);
        } else if (Partido.ESTADO_EN_CURSO.equals(estado) && ticksEnCurso < 60) {
            double k = ticksEnCurso / 60.0;
            AffineTransform original = g.getTransform();
            g.translate(500, 200);
            g.scale(1 + k * 0.6, 1 + k * 0.6);
            Estilo.textoContorno(g, "¡YA!", Estilo.fuente(110), 0, 0, Estilo.AMARILLO, Estilo.NARANJA,
                    new Color(0x4A, 0x20, 0x00), 7f);
            g.setTransform(original);
        } else if (Partido.ESTADO_GOL.equals(estado)) {
            bannerGol(g, f);
        } else if (Partido.ESTADO_PAUSADO.equals(estado)) {
            menuPausa(g);
        } else if (Partido.ESTADO_TERMINADO.equals(estado)) {
            resultadoFinal(g, f);
        }
    }

    private void dibujarJugador(Graphics2D g, Jugador j, boolean alterno, double f) {
        String aura = j.isPoderActivo() ? j.getPersonaje().getPoder().getCodigo() : "";
        DibujoPersonaje.dibujar(g, j.getX(), j.getY(), Cancha.SUELO, j.getPersonaje().getCodigo(), alterno,
                j.getMirando(), j.getProgresoPatada(), aura, f);
    }

    private void dibujarConfeti(Graphics2D g) {
        for (Confeti c : confeti) {
            if (c.y > Cancha.ALTO + 10) {
                continue;
            }
            AffineTransform original = g.getTransform();
            g.translate(c.x, c.y);
            g.rotate(c.giro);
            g.setColor(c.color);
            g.fill(new Rectangle2D.Double(-5, -3, 10, 6 * Math.abs(Math.cos(c.giro)) + 1));
            g.setTransform(original);
        }
    }

    // marcador

    private void dibujarMarcador(Graphics2D g, Jugador j1, Jugador j2, double f) {
        // Panel central con goles y tiempo
        double w = 330;
        double x = 500 - w / 2;
        Estilo.panel(g, x, 6, w, 62, 30, new Color(0x10, 0x24, 0x58), Color.WHITE);
        Estilo.textoContorno(g, String.valueOf(partido.getMarcador().getGoles(j1)), Estilo.fuente(40), x + 52, 52,
                Color.WHITE, Estilo.AZUL_CLARO, Estilo.AZUL_NOCHE, 3.5f);
        Estilo.textoContorno(g, String.valueOf(partido.getMarcador().getGoles(j2)), Estilo.fuente(40), x + w - 52, 52,
                Color.WHITE, new Color(0xFF, 0x9A, 0x9A), new Color(0x5A, 0x10, 0x10), 3.5f);
        String reloj;
        Color colorReloj = Color.WHITE;
        if (partido.isMuerteSubita()) {
            reloj = "MUERTE SÚBITA";
            colorReloj = Estilo.mezclar(Estilo.AMARILLO, 0.1 * Math.sin(f * 0.15));
        } else if (Partido.TIPO_TIEMPO.equals(partido.getTipo())) {
            int s = partido.getSegundosRestantes();
            reloj = String.format("%d:%02d", s / 60, s % 60);
            if (s <= 10 && Partido.ESTADO_EN_CURSO.equals(partido.getEstado()) && (int) (f / 15) % 2 == 0) {
                colorReloj = Estilo.ROJO;
            }
        } else {
            reloj = "A " + partido.getGolesParaGanar() + (partido.getGolesParaGanar() == 1 ? " GOL" : " GOLES");
        }
        Estilo.textoCentrado(g, reloj, Estilo.fuente(partido.isMuerteSubita() ? 19 : 30), 500, partido.isMuerteSubita() ? 44 : 49,
                colorReloj);
        if (Partido.TIPO_GOLES.equals(partido.getTipo()) && !partido.isMuerteSubita()) {
            int s = partido.getSegundosJugados();
            Estilo.textoCentrado(g, String.format("%d:%02d", s / 60, s % 60), Estilo.fuenteTexto(14), 500, 63,
                    new Color(0xBF, 0xD6, 0xFF));
        }

        // Banderas y barras de poder a cada lado
        lado(g, j1, 16, false, Estilo.AZUL_CLARO, f);
        lado(g, j2, Cancha.ANCHO - 16, true, Estilo.ROJO, f);
    }

    private void lado(Graphics2D g, Jugador j, double borde, boolean derecha, Color color, double f) {
        double wBandera = 56;
        double hBandera = 38;
        double wBarra = 232;
        double xBandera = derecha ? borde - wBandera : borde;
        DibujoBandera.dibujar(g, j.getPersonaje().getCodigo(), xBandera, 12, wBandera, hBandera);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2.5f));
        g.draw(new RoundRectangle2D.Double(xBandera, 12, wBandera, hBandera, 4, 4));

        double xBarra = derecha ? xBandera - 12 - wBarra : xBandera + wBandera + 12;
        double yBarra = 24;
        double hBarra = 20;
        boolean listo = j.getPoder() >= Jugador.PODER_MAXIMO && !j.isPoderActivo();
        // marco
        g.setColor(new Color(0, 0, 0, 120));
        g.fill(new RoundRectangle2D.Double(xBarra - 3, yBarra - 3, wBarra + 6, hBarra + 6, 20, 20));
        g.setColor(new Color(0x14, 0x1C, 0x38));
        g.fill(new RoundRectangle2D.Double(xBarra, yBarra, wBarra, hBarra, 16, 16));
        double proporcion = Math.max(0, Math.min(1, j.getPoder() / Jugador.PODER_MAXIMO));
        if (proporcion > 0) {
            double relleno = Math.max(hBarra, wBarra * proporcion);
            double xr = derecha ? xBarra + wBarra - relleno : xBarra;
            Color c1 = listo ? Estilo.AMARILLO : color;
            Color c2 = listo ? Estilo.NARANJA : Estilo.mezclar(color, -0.25);
            if (listo) {
                double pulso = 0.5 + 0.5 * Math.sin(f * 0.25);
                c1 = Estilo.mezclar(c1, 0.2 * pulso);
            }
            g.setPaint(new java.awt.GradientPaint(0, (float) yBarra, c1, 0, (float) (yBarra + hBarra), c2));
            g.fill(new RoundRectangle2D.Double(xr, yBarra, relleno, hBarra, 16, 16));
            g.setColor(new Color(255, 255, 255, 90));
            g.fill(new RoundRectangle2D.Double(xr + 4, yBarra + 2, relleno - 8, hBarra * 0.35, 8, 8));
        }
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Double(xBarra, yBarra, wBarra, hBarra, 16, 16));
        String etiqueta;
        if (j.isPoderActivo()) {
            etiqueta = "¡PODER ACTIVO!";
        } else if (listo) {
            etiqueta = derecha ? "¡LISTO!  [ , ]" : "¡LISTO!  [ G ]";
        } else {
            etiqueta = "PODER " + (int) (proporcion * 100) + "%";
        }
        Estilo.textoCentrado(g, etiqueta, Estilo.fuente(13), xBarra + wBarra / 2, yBarra + hBarra - 5, Color.WHITE);
        Estilo.textoCentrado(g, j.getPersonaje().getPais(), Estilo.fuenteTexto(14), xBarra + wBarra / 2, yBarra + hBarra + 18,
                new Color(255, 255, 255, 230));
    }

    // mensajes grandes

    private void cuentaAtras(Graphics2D g, double f) {
        int n = partido.getNumeroCuenta();
        double fase = (partido.getTicksCuenta() % Partido.TICKS_POR_SEGUNDO) / (double) Partido.TICKS_POR_SEGUNDO;
        double escala = 1 + fase * 0.5;
        AffineTransform original = g.getTransform();
        g.translate(500, 240);
        g.scale(escala, escala);
        Estilo.textoContorno(g, String.valueOf(n), Estilo.fuente(150), 0, 0, Color.WHITE, Estilo.AZUL_CLARO,
                Estilo.AZUL_NOCHE, 8f);
        g.setTransform(original);
        Estilo.textoCentrado(g, partido.getJugador1().getPersonaje().getPais() + "  vs  "
                + partido.getJugador2().getPersonaje().getPais(), Estilo.fuente(26), 500, 300, Color.WHITE);
    }

    private void bannerGol(Graphics2D g, double f) {
        double k = 1 - partido.getTicksCelebracion() / (double) Partido.TICKS_CELEBRACION;
        double entrada = Math.min(1, k * 5);
        double escala = 0.4 + entrada * 0.6 + Math.sin(f * 0.25) * 0.03;
        AffineTransform original = g.getTransform();
        g.translate(500, 215);
        g.scale(escala, escala);
        Estilo.textoContorno(g, "¡GOOOL!", Estilo.fuente(120), 0, 0, Estilo.AMARILLO, Estilo.NARANJA,
                new Color(0x4A, 0x20, 0x00), 8f);
        g.setTransform(original);
        Jugador a = partido.getUltimoAnotador();
        if (a != null) {
            Estilo.textoCentrado(g, a.getNombre() + "  ·  " + a.getPersonaje().getPais(), Estilo.fuente(28), 500, 280,
                    Color.WHITE);
        }
        if (partido.isMuerteSubita() || Partido.ESTADO_GOL.equals(partido.getEstado())) {
            // nada más: el dominio decide si el partido sigue o termina
        }
    }

    private void menuPausa(Graphics2D g) {
        velo(g, 150);
        tarjeta(g, 300, 120, 400, 300, Estilo.AMARILLO, true);
        Estilo.textoContorno(g, "PAUSA", Estilo.fuente(64), 500, 205, Estilo.AMARILLO, Estilo.NARANJA,
                new Color(0x4A, 0x20, 0x00), 5f);
        boton(g, "CONTINUAR", Estilo.VERDE, 350, 245, 300, 56, true, this::alternarPausa);
        boton(g, "SALIR AL MENÚ", Estilo.ROJO, 350, 320, 300, 56, true, () -> {
            partido.abandonar();
            ventana.irAPortada();
        });
    }

    private void resultadoFinal(Graphics2D g, double f) {
        velo(g, 120);
        tarjeta(g, 190, 80, 620, 390, Estilo.AMARILLO, true);
        Jugador ganador = partido.getGanador();
        Jugador j1 = partido.getJugador1();
        Jugador j2 = partido.getJugador2();
        Estilo.textoContorno(g, "¡FIN DEL PARTIDO!", Estilo.fuente(44), 500, 146, Estilo.AMARILLO, Estilo.NARANJA,
                new Color(0x4A, 0x20, 0x00), 4f);
        String quien = ganador == null ? "EMPATE" : "GANA " + ganador.getNombre().toUpperCase();
        Estilo.textoCentrado(g, quien, Estilo.fuente(30), 500, 190, Color.WHITE);

        columnaResultado(g, j1, 340, Estilo.AZUL_CLARO, ganador == j1, f);
        columnaResultado(g, j2, 660, Estilo.ROJO, ganador == j2, f);
        Estilo.textoContorno(g, partido.getMarcador().getGoles(j1) + " - " + partido.getMarcador().getGoles(j2),
                Estilo.fuente(64), 500, 330, Color.WHITE, Estilo.AZUL_CLARO, Estilo.AZUL_NOCHE, 5f);

        boton(g, "REVANCHA", Estilo.VERDE, 240, 392, 240, 56, true, ventana::iniciarPartido);
        boton(g, "MENÚ", Estilo.AZUL, 520, 392, 240, 56, true, ventana::irAPortada);
    }

    private void columnaResultado(Graphics2D g, Jugador j, double cx, Color color, boolean gano, double f) {
        AffineTransform original = g.getTransform();
        g.translate(cx, 214);
        g.scale(0.62, 0.62);
        double brinco = gano ? Math.abs(Math.sin(f * 0.12)) * 16 : 0;
        boolean alterno = j == partido.getJugador2()
                && j.getPersonaje().getCodigo().equals(partido.getJugador1().getPersonaje().getCodigo());
        DibujoPersonaje.dibujarGrande(g, 0, 60 - brinco, 150, j.getPersonaje().getCodigo(), alterno,
                j == partido.getJugador1() ? 1 : -1, 0, "", f);
        g.setTransform(original);
        Estilo.textoCentrado(g, j.getPersonaje().getPais(), Estilo.fuente(18), cx, 322, color);
        Estilo.textoCentrado(g, "+" + partido.getPuntosGanados(j) + " puntos", Estilo.fuente(20), cx, 352,
                gano ? Estilo.AMARILLO : new Color(0xBF, 0xD6, 0xFF));
        texto(g, "Total: " + j.getPuntos(), 15, cx, 374, new Color(0xD8, 0xE6, 0xFF));
    }
}
