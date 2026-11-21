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

    /** Daño base de una patada que le llega al rival (se multiplica por el multiplicadorKick). */
    public static final double DANO_PATADA_BASE = 10;
    /** Lo que dura el aturdimiento cuando la vida llega a cero: 3 segundos. */
    public static final int TICKS_ATURDIMIENTO = 3 * 60;

    public static final int ESTADISTICA_MINIMA = 1;
    public static final int ESTADISTICA_MAXIMA = 10;
    /** Lo que cuesta la primera mejora de una estadística; cada mejora siguiente cuesta el doble. */
    public static final int COSTO_BASE_MEJORA = 500;

    // Las estadísticas que se pueden mejorar con puntos.
    public static final String SPEED = "SPEED";
    public static final String JUMP = "JUMP";
    public static final String KICK = "KICK";
    public static final String SURVIVAL = "SURVIVAL";

    // Los efectos que un tiro de poder le puede dejar al rival.
    public static final String EFECTO_NINGUNO = "";
    public static final String EFECTO_EMPUJADO = "EMPUJADO";
    public static final String EFECTO_ELECTROCUTADO = "ELECTROCUTADO";
    public static final String EFECTO_INCONSCIENTE = "INCONSCIENTE";
    public static final String EFECTO_CONFUNDIDO = "CONFUNDIDO";

    private static final double VELOCIDAD_BASE = 6.2;
    private static final double SALTO_BASE = 11.0;
    private static final double VELOCIDAD_PATADA = 16;
    private static final double VELOCIDAD_EMPUJE = 9;
    private static final double FUERZA_TORNADO = 14;
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

    private double vida;
    private int ticksAturdido;
    private String efecto = EFECTO_NINGUNO;
    private int ticksEfecto;

    private int direccionPedida;
    private boolean saltoPedido;
    private boolean patadaPedida;
    private int ticksPatada;
    private boolean golpeAplicado;
    private boolean golpeRivalAplicado;

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
        this.vida = getVidaMaxima();
        reubicar();
    }

    /**
     * Prepara al jugador para un partido nuevo: personaje elegido, barra de poder
     * vacía, vida al máximo y de vuelta en su posición inicial. Los puntos y las
     * estadísticas se conservan.
     *
     * @param nuevoPersonaje el personaje con el que va a jugar
     */
    public void prepararPartido(Personaje nuevoPersonaje) {
        this.personaje = nuevoPersonaje;
        this.poder = 0;
        this.poderActivo = false;
        this.vida = getVidaMaxima();
        this.ticksAturdido = 0;
        reubicar();
    }

    /**
     * Lo devuelve a su lugar de salida (después de cada gol, por ejemplo). Los
     * efectos y el aturdimiento se acaban; la vida se conserva, salvo que
     * estuviera aturdido, que vuelve al máximo como al terminar el aturdimiento.
     */
    public void reubicar() {
        x = Porteria.IZQUIERDA.equals(lado) ? 250 : Cancha.ANCHO - 250;
        y = Cancha.SUELO - ALTURA_CENTRO;
        vx = 0;
        vy = 0;
        // Cada jugador siempre mira hacia el arco rival: el de la izquierda a la derecha y el de la
        // derecha a la izquierda. Nunca se voltea.
        mirando = Porteria.IZQUIERDA.equals(lado) ? 1 : -1;
        limpiarPedidos();
        ticksPatada = 0;
        golpeAplicado = false;
        golpeRivalAplicado = false;
        efecto = EFECTO_NINGUNO;
        ticksEfecto = 0;
        if (ticksAturdido > 0) {
            ticksAturdido = 0;
            vida = getVidaMaxima();
        }
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

    /** Avanza al jugador un tick: caminar, saltar, caer, patear, cargar poder y gastar sus efectos. */
    public void actualizar() {
        boolean puedeActuar = puedeActuar();
        boolean empujado = EFECTO_EMPUJADO.equals(efecto) && ticksAturdido == 0;
        boolean electrocutado = EFECTO_ELECTROCUTADO.equals(efecto) && ticksAturdido == 0;

        // Caminar: aturdido o inconsciente no se mueve; empujado va solo hacia su propio arco;
        // electrocutado camina a la mitad de la velocidad.
        if (empujado) {
            vx = -getDireccionAtaque() * VELOCIDAD_EMPUJE;
        } else if (puedeActuar) {
            double factor = electrocutado ? 0.5 : 1;
            vx = direccionPedida * VELOCIDAD_BASE * getMultiplicadorVelocidad() * factor;
        } else {
            vx = 0;
        }
        x += vx;
        if (x < LIMITE_IZQUIERDO) {
            x = LIMITE_IZQUIERDO;
        }
        if (x > LIMITE_DERECHO) {
            x = LIMITE_DERECHO;
        }

        // Saltar y caer (electrocutado no puede saltar)
        double piso = Cancha.SUELO - ALTURA_CENTRO;
        boolean enElPiso = y >= piso;
        if (saltoPedido && enElPiso && puedeActuar && !electrocutado) {
            vy = -SALTO_BASE * getMultiplicadorSalto();
        }
        saltoPedido = false;
        vy += Cancha.GRAVEDAD_JUGADOR;
        y += vy;
        if (y >= piso) {
            y = piso;
            vy = 0;
        }

        // Patada (aturdido o inconsciente no puede patear)
        if (patadaPedida && ticksPatada == 0 && puedeActuar) {
            ticksPatada = 1;
            golpeAplicado = false;
            golpeRivalAplicado = false;
        } else if (ticksPatada > 0) {
            ticksPatada++;
            if (ticksPatada > DURACION_PATADA) {
                ticksPatada = 0;
            }
        }
        patadaPedida = false;

        // Poder
        cargarPoder(PODER_MAXIMO / TICKS_CARGA);

        gastarEfectos();
    }

    /** Descuenta un tick al aturdimiento y al efecto que tenga encima. */
    private void gastarEfectos() {
        if (ticksAturdido > 0) {
            ticksAturdido--;
            if (ticksAturdido == 0) {
                vida = getVidaMaxima();   // al despertar, la vida vuelve al máximo
            }
        }
        if (ticksEfecto > 0) {
            ticksEfecto--;
            if (ticksEfecto == 0) {
                efecto = EFECTO_NINGUNO;
            }
        }
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
     * Separa a este jugador de otro cuando se chocan de lado (se mueve en x).
     *
     * @param desplazamiento cuánto se corre
     */
    public void empujar(double desplazamiento) {
        x = Math.max(LIMITE_IZQUIERDO, Math.min(LIMITE_DERECHO, x + desplazamiento));
    }

    // vida, daño y efectos

    /**
     * Le quita vida. Nunca baja de cero: cuando llega a cero queda aturdido
     * (no se mueve, no salta ni patea) durante 3 segundos y después su vida
     * vuelve al máximo. Mientras está aturdido no recibe más daño.
     *
     * @param daño cuánto daño recibe
     */
    public void recibirDaño(double daño) {
        if (ticksAturdido > 0 || daño <= 0) {
            return;
        }
        vida = Math.max(0, vida - daño);
        if (vida == 0) {
            ticksAturdido = TICKS_ATURDIMIENTO;
            efecto = EFECTO_NINGUNO;   // el aturdimiento manda sobre cualquier otro efecto
            ticksEfecto = 0;
            ticksPatada = 0;
        }
    }

    /**
     * Le deja un efecto de un tiro de poder. Los efectos no se acumulan: si ya
     * tenía uno, el nuevo lo reemplaza. Si está aturdido no pasa nada.
     *
     * @param nuevoEfecto uno de los EFECTO_...
     * @param ticks       cuánto dura
     */
    public void aplicarEfecto(String nuevoEfecto, int ticks) {
        if (ticksAturdido > 0) {
            return;
        }
        efecto = nuevoEfecto;
        ticksEfecto = ticks;
        if (!EFECTO_NINGUNO.equals(nuevoEfecto) && !EFECTO_CONFUNDIDO.equals(nuevoEfecto)) {
            ticksPatada = 0;
        }
    }

    /**
     * Lo lanza hacia arriba (lo usa el Tornado).
     */
    public void lanzarAlAire() {
        vy = -FUERZA_TORNADO;
    }

    /**
     * Si está pateando y su pie le alcanza al rival, le hace daño: 10 x multiplicadorKick.
     * Solo cuenta una vez por patada. Si la patada no le llega al rival, no pasa nada
     * (y si toca el balón, el balón simplemente sale impulsado).
     *
     * @param rival el otro jugador
     * @return true si le hizo daño
     */
    public boolean golpearConPie(Jugador rival) {
        if (!estaPateando() || golpeRivalAplicado || getProgresoPatada() <= 0.35) {
            return false;
        }
        // El rival se toma como un círculo un poco más grande que la cabeza, para incluir el cuerpo.
        double distancia = Math.hypot(getPieX() - rival.getX(), getPieY() - (rival.getY() + 14));
        if (distancia < RADIO_PIE + RADIO_CABEZA + 7) {
            golpeRivalAplicado = true;
            rival.recibirDaño(DANO_PATADA_BASE * getMultiplicadorKick());
            return true;
        }
        return false;
    }

    /** @return true si puede moverse, saltar y patear (no está aturdido, inconsciente ni siendo empujado) */
    public boolean puedeActuar() {
        return ticksAturdido == 0 && !EFECTO_INCONSCIENTE.equals(efecto) && !EFECTO_EMPUJADO.equals(efecto);
    }

    /** @return true mientras está aturdido porque su vida llegó a cero */
    public boolean estaAturdido() {
        return ticksAturdido > 0;
    }

    public double getVida() {
        return vida;
    }

    /** @return el efecto de poder que tiene encima (EFECTO_NINGUNO si no tiene) */
    public String getEfecto() {
        return efecto;
    }

    public int getTicksEfecto() {
        return ticksEfecto;
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

    // mejoras con puntos

    /**
     * @param estadistica SPEED, JUMP, KICK o SURVIVAL
     * @return el nivel actual (de 1 a 10)
     * @throws DOPOSoccerExcepcion si la estadística no existe
     */
    public int getNivel(String estadistica) throws DOPOSoccerExcepcion {
        if (SPEED.equals(estadistica)) {
            return speed;
        } else if (JUMP.equals(estadistica)) {
            return jump;
        } else if (KICK.equals(estadistica)) {
            return kick;
        } else if (SURVIVAL.equals(estadistica)) {
            return survival;
        }
        throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.ESTADISTICA_DESCONOCIDA));
    }

    /**
     * Lo que cuesta subir la estadística un punto: 500, 1000, 2000... (se duplica con cada mejora).
     *
     * @param estadistica SPEED, JUMP, KICK o SURVIVAL
     * @return el costo en puntos, o -1 si ya está en el máximo
     * @throws DOPOSoccerExcepcion si la estadística no existe
     */
    public int getCostoMejora(String estadistica) throws DOPOSoccerExcepcion {
        int nivel = getNivel(estadistica);
        if (nivel >= ESTADISTICA_MAXIMA) {
            return -1;
        }
        return COSTO_BASE_MEJORA * (1 << (nivel - 1));
    }

    /** @return true si hay puntos suficientes y la estadística no está en su máximo */
    public boolean puedeMejorar(String estadistica) {
        try {
            int costo = getCostoMejora(estadistica);
            return costo > 0 && puntos >= costo;
        } catch (DOPOSoccerExcepcion e) {
            return false;
        }
    }

    /**
     * Compra un punto de mejora: gasta los puntos y sube la estadística uno.
     *
     * @param estadistica SPEED, JUMP, KICK o SURVIVAL
     * @throws DOPOSoccerExcepcion si no existe, ya está en el máximo o no alcanzan los puntos
     */
    public void mejorar(String estadistica) throws DOPOSoccerExcepcion {
        int costo = getCostoMejora(estadistica);
        if (costo < 0) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.ESTADISTICA_AL_MAXIMO));
        }
        if (puntos < costo) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.PUNTOS_INSUFICIENTES));
        }
        puntos -= costo;
        int nuevo = getNivel(estadistica) + 1;
        if (SPEED.equals(estadistica)) {
            speed = nuevo;
        } else if (JUMP.equals(estadistica)) {
            jump = nuevo;
        } else if (KICK.equals(estadistica)) {
            kick = nuevo;
        } else {
            survival = nuevo;
        }
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
