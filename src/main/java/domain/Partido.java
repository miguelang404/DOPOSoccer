package domain;

import exceptions.DOPOSoccerExcepcion;
import util.RegistroErrores;

import java.util.ArrayList;
import java.util.List;

/**
 * Un partido de DOPOSoccer. Aquí vive todo lo que pasa mientras se juega: la
 * cuenta regresiva del inicio, el cronómetro, los goles, la celebración, la
 * pausa, la muerte súbita y el cierre con ganador y puntos.
 * <p>
 * La interfaz llama a actualizar() 60 veces por segundo y dibuja lo que este
 * objeto le cuenta; el partido nunca sabe cómo se ve.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Partido {

    public static final String TIPO_TIEMPO = "TIEMPO";
    public static final String TIPO_GOLES = "GOLES";

    public static final String ESTADO_CUENTA = "CUENTA";
    public static final String ESTADO_EN_CURSO = "EN_CURSO";
    public static final String ESTADO_GOL = "GOL";
    public static final String ESTADO_PAUSADO = "PAUSADO";
    public static final String ESTADO_TERMINADO = "TERMINADO";
    public static final String ESTADO_ABANDONADO = "ABANDONADO";

    public static final int TICKS_POR_SEGUNDO = 60;
    public static final int TICKS_CUENTA = 3 * TICKS_POR_SEGUNDO;
    public static final int TICKS_CELEBRACION = 2 * TICKS_POR_SEGUNDO;
    /**
     * Cuánto dura el balón flotando sobre la cabeza antes de salir lanzado con el poder.
     */
    public static final int TICKS_CARGA_PODER = 40;
    public static final int PUNTOS_GANADOR = 100;
    public static final int PUNTOS_PERDEDOR = 25;

    private final Jugador jugador1;
    private final Jugador jugador2;
    private final Cancha cancha;
    private final Balon balon;
    private final Marcador marcador;
    private final Porteria porteriaIzquierda;
    private final Porteria porteriaDerecha;
    private final List<Balon> señuelos = new ArrayList<Balon>();

    private final String tipo;
    private final int minutos;
    private final int golesParaGanar;

    private String estado;
    private String estadoAntesDePausa;
    private int ticksCuenta;
    private int ticksRestantes;
    private int ticksJugados;
    private int ticksCelebracion;
    private int ticksSeñuelos;
    private int ticksAtasco;
    private Jugador jugadorCargando;
    private int ticksCargando;
    private Jugador lanzadorPoder;
    private boolean muerteSubita;
    private boolean finalPendiente;
    private Jugador ultimoAnotador;
    private Jugador ganador;
    private int puntosJugador1;
    private int puntosJugador2;

    /**
     * Crea el partido, con el marcador en 0 - 0 y las barras de poder vacías.
     *
     * @param jugador1 el jugador del arco izquierdo
     * @param jugador2 el jugador del arco derecho
     * @param tipo     TIPO_TIEMPO o TIPO_GOLES
     * @param valor    minutos (1 a 10) si es por tiempo, o goles para ganar (1 a 20) si es por goles
     * @param cancha   la cancha donde se juega
     * @throws DOPOSoccerExcepcion si falta algo o el valor no es válido
     */
    public Partido(Jugador jugador1, Jugador jugador2, String tipo, int valor, Cancha cancha)
            throws DOPOSoccerExcepcion {
        if (jugador1 == null || jugador2 == null || cancha == null) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.JUGADORES_VACIOS));
        }
        if (jugador1.getPersonaje() == null || jugador2.getPersonaje() == null) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.PERSONAJE_VACIO));
        }
        if (TIPO_TIEMPO.equals(tipo)) {
            if (valor < 1 || valor > 10) {
                throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.MINUTOS_INVALIDOS));
            }
            this.minutos = valor;
            this.golesParaGanar = 0;
        } else if (TIPO_GOLES.equals(tipo)) {
            if (valor < 1 || valor > 20) {
                throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.GOLES_INVALIDOS));
            }
            this.minutos = 0;
            this.golesParaGanar = valor;
        } else {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.TIPO_PARTIDO_INVALIDO));
        }
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.cancha = cancha;
        this.tipo = tipo;
        this.balon = new Balon();
        this.marcador = new Marcador(jugador1, jugador2);
        this.porteriaIzquierda = new Porteria(Porteria.IZQUIERDA);
        this.porteriaDerecha = new Porteria(Porteria.DERECHA);
        this.ticksRestantes = minutos * 60 * TICKS_POR_SEGUNDO;
        this.ticksCuenta = TICKS_CUENTA;
        this.estado = ESTADO_CUENTA;
        reubicarTodo();
    }

    // el reloj del juego

    /**
     * Avanza el partido un tick (la interfaz lo llama 60 veces por segundo).
     */
    public void actualizar() {
        if (ESTADO_CUENTA.equals(estado)) {
            limpiarPedidos();
            ticksCuenta--;
            if (ticksCuenta <= 0) {
                estado = ESTADO_EN_CURSO;
            }
        } else if (ESTADO_GOL.equals(estado)) {
            limpiarPedidos();
            balon.mover();
            moverSeñuelos();
            ticksCelebracion--;
            if (ticksCelebracion <= 0) {
                terminarCelebracion();
            }
        } else if (ESTADO_EN_CURSO.equals(estado)) {
            jugar();
        }
    }

    private void jugar() {
        // Recomendación de la IA generativa para el orden de los contactos: se alterna quién revisa
        // primero según si el tick es par o impar, para que ningún jugador tenga ventaja cuando los dos
        // tocan el balón a la vez.
        jugador1.actualizar();
        jugador2.actualizar();
        separarJugadores();
        // Las patadas que le llegan al rival le hacen daño.
        jugador1.golpearConPie(jugador2);
        jugador2.golpearConPie(jugador1);

        if (jugadorCargando != null) {
            cargarTiroDePoder();
        } else {
            balon.mover();
            moverSeñuelos();

            // Alternamos quién revisa primero el contacto para que ninguno tenga ventaja.
            if (ticksJugados % 2 == 0) {
                tocarBalon(jugador1);
                tocarBalon(jugador2);
            } else {
                tocarBalon(jugador2);
                tocarBalon(jugador1);
            }

            boolean encima = porteriaIzquierda.rebotar(balon);
            encima = porteriaDerecha.rebotar(balon) || encima;
            evitarAtasco(encima);
            if (balon.getEfecto().isEmpty()) {
                lanzadorPoder = null;   // el tiro de poder ya se acabó
            }
        }

        ticksJugados++;
        if (porteriaIzquierda.esGol(balon)) {
            registrarGol(porteriaIzquierda);
        } else if (porteriaDerecha.esGol(balon)) {
            registrarGol(porteriaDerecha);
        } else if (TIPO_TIEMPO.equals(tipo) && !muerteSubita) {
            ticksRestantes--;
            if (ticksRestantes <= 0) {
                terminarPorTiempo();
            }
        }
    }

    private void tocarBalon(Jugador jugador) {
        if (!jugador.resolverContacto(balon)) {
            return;
        }
        if (lanzadorPoder != null && jugador != lanzadorPoder && !balon.getEfecto().isEmpty()) {
            // Un tiro de poder alcanzó al rival: recibe el daño y el efecto de ese poder.
            Jugador atacante = lanzadorPoder;
            lanzadorPoder = null;
            atacante.getPersonaje().getPoder().golpear(this, atacante, jugador);
            balon.terminarEfecto();
        } else if (jugador.isPoderActivo() && jugadorCargando == null) {
            // Con el poder activo, al tocar el balón no sale disparado de una vez:
            // primero se pone delante de la frente (o del pie, según el poder) y ahí se carga el tiro.
            jugadorCargando = jugador;
            ticksCargando = 0;
            balon.setVelocidad(0, 0);
            balon.setEfecto(jugador.getPersonaje().getPoder().getCodigo(), TICKS_CARGA_PODER + 5);
        } else {
            balon.terminarEfecto();   // chocar con un jugador acaba el poder
            lanzadorPoder = null;
        }
    }

    /**
     * Mientras se carga el tiro: el balón se acerca a la frente (delante de la cara), o al pie en el
     * caso del Tornado, y al terminar sale lanzado.
     */
    private void cargarTiroDePoder() {
        // Recomendación de la IA generativa para la carga del tiro de poder: en vez de teletransportar
        // el balón, cada tick se acerca un 30 % de la distancia que le falta a la frente del jugador.
        // Así se ve como un deslizamiento suave y no un salto.
        Jugador j = jugadorCargando;
        double objetivoX;
        double objetivoY;
        if (j.getPersonaje().getPoder().saleDelPie()) {
            // Delante del pie, pegado al suelo (el Tornado corre a ras de piso)
            objetivoX = j.getX() + j.getMirando() * (9 + Jugador.RADIO_PIE + Balon.RADIO + 3);
            objetivoY = Cancha.SUELO - Balon.RADIO;
        } else {
            // Delante de la cara, a la altura de la frente (un poco arriba del centro de la cabeza)
            objetivoX = j.getX() + j.getMirando() * (Jugador.RADIO_CABEZA + Balon.RADIO + 4);
            objetivoY = j.getY() - 8;
        }
        balon.setPosicion(balon.getX() + (objetivoX - balon.getX()) * 0.3,
                balon.getY() + (objetivoY - balon.getY()) * 0.3);
        balon.setVelocidad(0, 0);
        ticksCargando++;
        if (ticksCargando >= TICKS_CARGA_PODER) {
            balon.setPosicion(objetivoX, objetivoY);
            jugadorCargando = null;
            lanzadorPoder = j;
            j.usarPoder(this);
        }
    }

    /**
     * Si el balón se queda quieto encima del travesaño, lo empujamos de vuelta a la cancha.
     */
    private void evitarAtasco(boolean encimaDelTravesaño) {
        // Recomendación de la IA generativa para evitar el atasco sobre el travesaño: si el balón se
        // queda casi quieto encima del arco durante 90 ticks, se le da un empujón de vuelta a la
        // cancha.
        if (encimaDelTravesaño && Math.abs(balon.getVx()) < 0.5) {
            ticksAtasco++;
            if (ticksAtasco > 90) {
                double haciaElCentro = balon.getX() < Cancha.ANCHO / 2.0 ? 1 : -1;
                balon.impulsar(haciaElCentro * 4, -3);
                ticksAtasco = 0;
            }
        } else {
            ticksAtasco = 0;
        }
    }

    /**
     * Si dos cabezas se chocan de lado, se separan.
     */
    private void separarJugadores() {
        // Recomendación de la IA generativa para separar a los jugadores cuando chocan de lado: se
        // calcula cuánto se solapan y cada uno se corre la mitad en sentido contrario.
        double dx = jugador2.getX() - jugador1.getX();
        double dy = Math.abs(jugador2.getY() - jugador1.getY());
        double minima = Jugador.RADIO_CABEZA * 1.8;
        if (dy < Jugador.RADIO_CABEZA * 1.2 && Math.abs(dx) < minima) {
            double falta = (minima - Math.abs(dx)) / 2;
            double signo = dx >= 0 ? 1 : -1;
            jugador1.empujar(-signo * falta);
            jugador2.empujar(signo * falta);
        }
    }

    /**
     * Hace que los balones falsos duren al menos los ticks dados (los usa el poder Ilusión
     * cuando alcanza al rival).
     *
     * @param ticks cuántos ticks más se quedan
     */
    public void prolongarSeñuelos(int ticks) {
        if (!señuelos.isEmpty()) {
            ticksSeñuelos = Math.max(ticksSeñuelos, ticks);
        }
    }

    private void moverSeñuelos() {
        if (!señuelos.isEmpty()) {
            for (Balon falso : señuelos) {
                falso.mover();
            }
            ticksSeñuelos--;
            if (ticksSeñuelos <= 0) {
                señuelos.clear();
            }
        }
    }

    private void limpiarPedidos() {
        jugador1.limpiarPedidos();
        jugador2.limpiarPedidos();
    }

    // goles

    /**
     * Anota un gol en una portería: suma al otro jugador, le sube un cuarto de
     * barra a quien lo recibió y arranca la celebración.
     *
     * @param porteria el arco donde entró el balón
     */
    public void registrarGol(Porteria porteria) {
        balon.terminarEfecto();   // el poder se acaba al entrar en el arco (sin daño ni efecto)
        lanzadorPoder = null;
        Jugador anotador = porteria == porteriaIzquierda ? jugador2 : jugador1;
        Jugador recibe = anotador == jugador1 ? jugador2 : jugador1;
        marcador.sumarGol(anotador);
        recibe.recibirGol();
        ultimoAnotador = anotador;
        estado = ESTADO_GOL;
        ticksCelebracion = TICKS_CELEBRACION;

        if (muerteSubita) {
            finalPendiente = true;
        } else if (TIPO_GOLES.equals(tipo) && marcador.getGoles(anotador) >= golesParaGanar) {
            finalPendiente = true;
        }
    }

    private void terminarCelebracion() {
        if (finalPendiente) {
            cerrarPartido(ultimoAnotador);
        } else {
            reubicarTodo();
            estado = ESTADO_EN_CURSO;
        }
    }

    private void terminarPorTiempo() {
        if (marcador.hayEmpate()) {
            muerteSubita = true;   // gana el primero que marque
        } else {
            cerrarPartido(marcador.getGanador());
        }
    }

    private void cerrarPartido(Jugador quienGano) {
        ganador = quienGano;
        estado = ESTADO_TERMINADO;
        if (ganador == jugador1) {
            puntosJugador1 = PUNTOS_GANADOR;
            puntosJugador2 = PUNTOS_PERDEDOR;
        } else {
            puntosJugador1 = PUNTOS_PERDEDOR;
            puntosJugador2 = PUNTOS_GANADOR;
        }
        jugador1.sumarPuntos(puntosJugador1);
        jugador2.sumarPuntos(puntosJugador2);
    }

    /**
     * Los jugadores y el balón vuelven al centro, y el balón cae desde arriba.
     */
    private void reubicarTodo() {
        jugador1.reubicar();
        jugador2.reubicar();
        balon.reiniciar();
        señuelos.clear();
        ticksAtasco = 0;
        jugadorCargando = null;
        ticksCargando = 0;
        lanzadorPoder = null;
    }

    // pausa y salida

    /**
     * Pone el partido en pausa (solo si está jugando o en la cuenta atrás).
     */
    public void pausar() {
        if (ESTADO_EN_CURSO.equals(estado) || ESTADO_CUENTA.equals(estado)) {
            estadoAntesDePausa = estado;
            estado = ESTADO_PAUSADO;
            limpiarPedidos();
        }
    }

    /**
     * Quita la pausa y sigue donde se había quedado.
     */
    public void reanudar() {
        if (ESTADO_PAUSADO.equals(estado)) {
            estado = estadoAntesDePausa;
        }
    }

    /**
     * Termina el partido antes de tiempo: no hay ganador ni puntos.
     */
    public void abandonar() {
        if (!ESTADO_TERMINADO.equals(estado)) {
            estado = ESTADO_ABANDONADO;
        }
    }

    // consultas

    /**
     * @return el estado actual (ver las constantes ESTADO_...)
     */
    public String getEstado() {
        return estado;
    }

    public String getTipo() {
        return tipo;
    }

    public int getMinutos() {
        return minutos;
    }

    public int getGolesParaGanar() {
        return golesParaGanar;
    }

    public boolean isMuerteSubita() {
        return muerteSubita;
    }

    /**
     * @return segundos que faltan (solo tiene sentido en partidos por tiempo)
     */
    public int getSegundosRestantes() {
        return (int) Math.ceil(Math.max(0, ticksRestantes) / (double) TICKS_POR_SEGUNDO);
    }

    /**
     * @return segundos que lleva jugándose el partido
     */
    public int getSegundosJugados() {
        return ticksJugados / TICKS_POR_SEGUNDO;
    }

    /**
     * @return el número que se muestra en la cuenta atrás (3, 2, 1)
     */
    public int getNumeroCuenta() {
        return (int) Math.ceil(Math.max(0, ticksCuenta) / (double) TICKS_POR_SEGUNDO);
    }

    public int getTicksCuenta() {
        return ticksCuenta;
    }

    public int getTicksCelebracion() {
        return ticksCelebracion;
    }

    /**
     * @return el jugador que está cargando su tiro de poder (null si nadie)
     */
    public Jugador getJugadorCargando() {
        return jugadorCargando;
    }

    /**
     * @return quien disparó el tiro de poder que va en el aire (null si no hay ninguno)
     */
    public Jugador getLanzadorPoder() {
        return lanzadorPoder;
    }

    public Jugador getUltimoAnotador() {
        return ultimoAnotador;
    }

    /**
     * @return el ganador, o null si todavía no hay (o si se abandonó)
     */
    public Jugador getGanador() {
        return ganador;
    }

    /**
     * @param jugador uno de los dos jugadores
     * @return los puntos que ganó en este partido
     */
    public int getPuntosGanados(Jugador jugador) {
        return jugador == jugador1 ? puntosJugador1 : puntosJugador2;
    }

    public Jugador getJugador1() {
        return jugador1;
    }

    public Jugador getJugador2() {
        return jugador2;
    }

    public Balon getBalon() {
        return balon;
    }

    public Cancha getCancha() {
        return cancha;
    }

    public Marcador getMarcador() {
        return marcador;
    }

    public Porteria getPorteriaIzquierda() {
        return porteriaIzquierda;
    }

    public Porteria getPorteriaDerecha() {
        return porteriaDerecha;
    }

    /**
     * @return los balones falsos del poder de Estados Unidos (puede estar vacía)
     */
    public List<Balon> getSeñuelos() {
        return señuelos;
    }

    /**
     * Agrega balones falsos (los usa el poder Ilusión).
     *
     * @param falsos los balones falsos
     * @param ticks  cuánto tiempo se quedan
     */
    public void agregarSeñuelos(List<Balon> falsos, int ticks) {
        señuelos.clear();
        señuelos.addAll(falsos);
        ticksSeñuelos = ticks;
    }
}
