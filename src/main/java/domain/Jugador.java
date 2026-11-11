package domain;
import exceptions.DOPOSoccerExcepcion;
import util.RegistroErrores;

/**
 * Un jugador de DOPOSoccer. Es una cabeza grande con un pie: camina, salta,
 * patea y acumula poder. De él guardamos qué personaje eligió, dónde está,
 * sus estadísticas, los puntos que ha ganado y la barra de poder.
 *
 * El jugador no lee el teclado: la interfaz le "pide" cosas (mover, saltar,
 * patear...) y él las ejecuta cuando el partido le dice que avance un tick.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Jugador {

    /** Radio de la cabeza (el tamaño del jugador). */
    public static final int RADIO_CABEZA = 29;
    /** Distancia del centro de la cabeza al piso cuando está parado. */
    public static final int ALTURA_CENTRO = 56;
    /** Radio del pie que patea. */
    public static final int RADIO_PIE = 9;
    /** La barra de poder va de 0 a este valor. */
    public static final double PODER_MAXIMO = 100;
    /** Ticks que tarda en llenarse la barra (10 segundos a 60 ticks por segundo). */
    public static final int TICKS_CARGA = 600;
    /** Lo que sube la barra cuando le meten un gol (un cuarto). */
    public static final double CARGA_POR_GOL = 25;
    /** Duración de la patada, en ticks. */
    public static final int DURACION_PATADA = 14;

    public static final int ESTADISTICA_MINIMA = 1;
    public static final int ESTADISTICA_MAXIMA = 10;

    private static final double VELOCIDAD_BASE = 6.2;
    private static final double SALTO_BASE = 11.0;
    private static final double VELOCIDAD_PATADA = 16;
    private static final int LIMITE_IZQUIERDO = Cancha.PORTERIA_ANCHO + 40;
    private static final int LIMITE_DERECHO = Cancha.ANCHO - Cancha.PORTERIA_ANCHO - 40;

    private final String nombre;
    private final String lado;
    private Personaje personaje;

    private double x;
    private double y;
    private double vx;
    private double vy;
    private int mirando;

    private int speed = 1;
    private int jump = 1;
    private int kick = 1;
    private int survival = 1;
    private int puntos;

    private double poder;
    private boolean poderActivo;

    private int direccionPedida;
    private boolean saltoPedido;
    private boolean patadaPedida;
    private int ticksPatada;
    private boolean golpeAplicado;

    /**
     * Crea un jugador.
     *
     * @param nombre    cómo se muestra ("Jugador 1")
     * @param personaje el personaje que eligió
     * @param lado      Porteria.IZQUIERDA o Porteria.DERECHA: el arco que defiende
     */
    public Jugador(String nombre, Personaje personaje, String lado) {
        this.nombre = nombre;
        this.personaje = personaje;
        this.lado = lado;
        reubicar();
    }

    /**
     * Prepara al jugador para un partido nuevo: personaje elegido, barra de poder
     * vacía y de vuelta en su posición inicial. Los puntos y las estadísticas se
     * conservan.
     *
     * @param nuevoPersonaje el personaje con el que va a jugar
     */
    public void prepararPartido(Personaje nuevoPersonaje) {
        this.personaje = nuevoPersonaje;
        this.poder = 0;
        this.poderActivo = false;
        reubicar();
    }

    /** Lo devuelve a su lugar de salida (después de cada gol, por ejemplo). */
    public void reubicar() {
        x = Porteria.IZQUIERDA.equals(lado) ? 250 : Cancha.ANCHO - 250;
        y = Cancha.SUELO - ALTURA_CENTRO;
        vx = 0;
        vy = 0;
        mirando = Porteria.IZQUIERDA.equals(lado) ? 1 : -1;
        limpiarPedidos();
        ticksPatada = 0;
        golpeAplicado = false;
    }

    // pedidos

    /**
     * Pide caminar. Se mantiene hasta que se vuelva a pedir otra cosa.
     *
     * @param direccion -1 izquierda, 0 quieto, 1 derecha
     */
    public void mover(int direccion) {
        direccionPedida = Integer.signum(direccion);
    }

    /** Pide un salto (solo funciona si está en el piso). */
    public void saltar() {
        saltoPedido = true;
    }

    /** Pide una patada (no se puede patear dos veces seguidas sin terminar la primera). */
    public void patear() {
        patadaPedida = true;
    }

    /** Borra todo lo que se había pedido (se usa mientras el juego está en pausa o en cuenta atrás). */
    public void limpiarPedidos() {
        direccionPedida = 0;
        saltoPedido = false;
        patadaPedida = false;
    }

    /**
     * Intenta activar el poder: necesita la barra llena y no tener otro poder
     * activo. Al activarlo la barra se vacía y empieza a recargarse de una vez.
     *
     * @return true si se activó
     */
    public boolean activarPoder() {
        if (poder >= PODER_MAXIMO && !poderActivo) {
            poder = 0;
            poderActivo = true;
            return true;
        }
        return false;
    }

    // cada instante

    /** Avanza al jugador un tick: caminar, saltar, caer, patear y cargar poder. */
    public void actualizar() {
        // Caminar
        vx = direccionPedida * VELOCIDAD_BASE * getMultiplicadorVelocidad();
        x += vx;
        if (x < LIMITE_IZQUIERDO) {
            x = LIMITE_IZQUIERDO;
        }
        if (x > LIMITE_DERECHO) {
            x = LIMITE_DERECHO;
        }

        // Saltar y caer
        double piso = Cancha.SUELO - ALTURA_CENTRO;
        boolean enElPiso = y >= piso;
        if (saltoPedido && enElPiso) {
            vy = -SALTO_BASE * getMultiplicadorSalto();
        }
        saltoPedido = false;
        vy += Cancha.GRAVEDAD_JUGADOR;
        y += vy;
        if (y >= piso) {
            y = piso;
            vy = 0;
        }

        // Patada
        if (patadaPedida && ticksPatada == 0) {
            ticksPatada = 1;
            golpeAplicado = false;
        } else if (ticksPatada > 0) {
            ticksPatada++;
            if (ticksPatada > DURACION_PATADA) {
                ticksPatada = 0;
            }
        }
        patadaPedida = false;

        // Poder
        cargarPoder(PODER_MAXIMO / TICKS_CARGA);
    }

    /**
     * Sube la barra de poder sin pasarse del máximo.
     *
     * @param cantidad cuánto sube
     */
    public void cargarPoder(double cantidad) {
        poder = Math.min(PODER_MAXIMO, poder + cantidad);
    }

    /** Se llama cuando le anotan un gol: la barra sube un cuarto de inmediato. */
    public void recibirGol() {
        cargarPoder(CARGA_POR_GOL);
    }

    /**
     * Dispara el tiro de poder del personaje (solo si tiene el aura activa) y
     * el aura desaparece.
     *
     * @param partido el partido en juego
     */
    public void usarPoder(Partido partido) {
        if (poderActivo) {
            personaje.getPoder().aplicar(partido, this);
            poderActivo = false;
        }
    }

    /**
     * Se gira hacia donde está el rival.
     *
     * @param xRival posición horizontal del rival
     */
    public void mirarHacia(double xRival) {
        if (xRival > x) {
            mirando = 1;
        } else if (xRival < x) {
            mirando = -1;
        }
    }

    /**
     * Separa a este jugador de otro cuando se chocan de lado (se mueve en x).
     *
     * @param desplazamiento cuánto se corre
     */
    public void empujar(double desplazamiento) {
        x = Math.max(LIMITE_IZQUIERDO, Math.min(LIMITE_DERECHO, x + desplazamiento));
    }

    // el balón

    /** @return progreso de la patada entre 0 y 1 (0 si no está pateando) */
    public double getProgresoPatada() {
        if (ticksPatada == 0) {
            return 0;
        }
        double t = (ticksPatada - 1) / (double) (DURACION_PATADA - 1);
        return Math.sin(Math.PI * Math.min(1, t));
    }

    /** @return true mientras el pie está estirado golpeando */
    public boolean estaPateando() {
        return ticksPatada > 0;
    }

    /** @return x del centro del pie */
    public double getPieX() {
        return x + mirando * (9 + 29 * getProgresoPatada());
    }

    /** @return y del centro del pie */
    public double getPieY() {
        return y + 47 - 16 * getProgresoPatada();
    }

    /**
     * Revisa si el jugador toca el balón (con la cabeza o con el pie) y, si lo toca,
     * lo hace rebotar o lo patea.
     *
     * @param balon el balón
     * @return true si hubo contacto
     */
    public boolean resolverContacto(Balon balon) {
        // Recomendación de la IA generativa para el contacto con el balón: la cabeza y el pie se tratan
        // como círculos. Se saca el balón del círculo, se descompone su velocidad relativa en la
        // dirección de choque (la normal) y se refleja, como en un choque elástico.
        boolean contacto = false;

        // Cabeza: rebote elástico contra un círculo que se mueve.
        double dx = balon.getX() - x;
        double dy = balon.getY() - y;
        double distancia = Math.hypot(dx, dy);
        double minima = RADIO_CABEZA + Balon.RADIO;
        if (distancia < minima && distancia > 0) {
            double nx = dx / distancia;
            double ny = dy / distancia;
            balon.setPosicion(x + nx * minima, y + ny * minima);
            double relX = balon.getVx() - vx;
            double relY = balon.getVy() - vy;
            double normal = relX * nx + relY * ny;
            double nuevoVx = balon.getVx();
            double nuevoVy = balon.getVy();
            if (normal < 0) {
                nuevoVx -= 1.85 * normal * nx;
                nuevoVy -= 1.85 * normal * ny;
            }
            // Que siempre salga con un mínimo de fuerza, y no se quede equilibrado encima de la cabeza.
            double sale = nuevoVx * nx + nuevoVy * ny;
            if (sale < 5) {
                nuevoVx += (5 - sale) * nx;
                nuevoVy += (5 - sale) * ny;
            }
            if (ny < -0.9 && Math.abs(nuevoVx) < 0.5) {
                nuevoVx += balon.getX() < x ? -1.2 : 1.2;
            }
            balon.setVelocidad(nuevoVx, nuevoVy);
            contacto = true;
        }

        // Pie
        double px = getPieX();
        double py = getPieY();
        double fx = balon.getX() - px;
        double fy = balon.getY() - py;
        double distPie = Math.hypot(fx, fy);
        double minPie = RADIO_PIE + Balon.RADIO;
        if (distPie < minPie && distPie > 0) {
            double nx = fx / distPie;
            double ny = fy / distPie;
            balon.setPosicion(px + nx * minPie, py + ny * minPie);
            if (estaPateando() && !golpeAplicado && getProgresoPatada() > 0.35) {
                // La patada sale hacia el arco rival y un poco hacia arriba.
                double dirX = 0.5 * nx + 0.5 * mirando * 0.8;
                double dirY = 0.5 * ny - 0.5 * 0.6;
                double largo = Math.hypot(dirX, dirY);
                balon.setVelocidad(dirX / largo * VELOCIDAD_PATADA, dirY / largo * VELOCIDAD_PATADA);
                golpeAplicado = true;
            } else {
                double normal = balon.getVx() * nx + balon.getVy() * ny;
                if (normal < 0) {
                    balon.setVelocidad(balon.getVx() - 1.6 * normal * nx, balon.getVy() - 1.6 * normal * ny);
                }
            }
            contacto = true;
        }
        return contacto;
    }

    // estadísticas

    /** @return 1 + 0,10 x (kick - 1), como dice el enunciado */
    public double getMultiplicadorKick() {
        return 1 + 0.10 * (kick - 1);
    }

    /** @return multiplicador de la velocidad al caminar */
    public double getMultiplicadorVelocidad() {
        return 1 + 0.10 * (speed - 1);
    }

    /** @return multiplicador de la altura del salto */
    public double getMultiplicadorSalto() {
        return 1 + 0.10 * (jump - 1);
    }

    /** @return vida máxima: 100 x (1 + 0,10 x (survival - 1)) */
    public double getVidaMaxima() {
        return 100 * (1 + 0.10 * (survival - 1));
    }

    private void validarEstadistica(int valor) throws DOPOSoccerExcepcion {
        if (valor < ESTADISTICA_MINIMA || valor > ESTADISTICA_MAXIMA) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.ESTADISTICA_INVALIDA));
        }
    }

    public void setSpeed(int valor) throws DOPOSoccerExcepcion {
        validarEstadistica(valor);
        speed = valor;
    }

    public void setJump(int valor) throws DOPOSoccerExcepcion {
        validarEstadistica(valor);
        jump = valor;
    }

    public void setKick(int valor) throws DOPOSoccerExcepcion {
        validarEstadistica(valor);
        kick = valor;
    }

    public void setSurvival(int valor) throws DOPOSoccerExcepcion {
        validarEstadistica(valor);
        survival = valor;
    }

    public int getSpeed() {
        return speed;
    }

    public int getJump() {
        return jump;
    }

    public int getKick() {
        return kick;
    }

    public int getSurvival() {
        return survival;
    }

    /**
     * Suma puntos ganados en un partido.
     *
     * @param cantidad puntos que se suman
     */
    public void sumarPuntos(int cantidad) {
        puntos += cantidad;
    }

    // consultas

    public String getNombre() {
        return nombre;
    }

    public String getLado() {
        return lado;
    }

    public Personaje getPersonaje() {
        return personaje;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    /** @return 1 si mira a la derecha, -1 si mira a la izquierda */
    public int getMirando() {
        return mirando;
    }

    /** @return 1 si ataca el arco de la derecha, -1 si ataca el de la izquierda */
    public int getDireccionAtaque() {
        return Porteria.IZQUIERDA.equals(lado) ? 1 : -1;
    }

    public boolean estaEnElPiso() {
        return y >= Cancha.SUELO - ALTURA_CENTRO;
    }

    public int getPuntos() {
        return puntos;
    }

    public double getPoder() {
        return poder;
    }

    public boolean isPoderActivo() {
        return poderActivo;
    }
}
