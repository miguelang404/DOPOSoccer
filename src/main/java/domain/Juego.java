package domain;

import exceptions.DOPOSoccerExcepcion;
import util.RegistroErrores;

/**
 * Puerta de entrada al dominio. La interfaz habla con esta clase para ver los
 * personajes disponibles, guardar la configuración y empezar un partido; ella se
 * encarga de armar todo lo demás.
 *
 * En esta versión solo existe la modalidad jugador vs jugador (en el mismo
 * teclado). Jugador vs máquina llega en la versión dos.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Juego {

    public static final String JUGADOR_VS_JUGADOR = "JUGADOR_VS_JUGADOR";
    public static final String JUGADOR_VS_MAQUINA = "JUGADOR_VS_MAQUINA";

    private final Configuracion configuracion;
    private final Personaje[] personajes;
    private final Jugador jugador1;
    private final Jugador jugador2;
    private String modalidad;
    private Partido partido;

    /** Crea el juego con la configuración de fábrica y los cuatro personajes del anexo. */
    public Juego() {
        configuracion = new Configuracion();
        // Para agregar un personaje nuevo basta con sumarlo a esta lista.
        personajes = new Personaje[] {new CoreaDelSur(), new Camerun(), new Nigeria(), new EstadosUnidos()};
        jugador1 = new Jugador("Jugador 1", personajes[0], Porteria.IZQUIERDA);
        jugador2 = new Jugador("Jugador 2", personajes[1], Porteria.DERECHA);
        modalidad = JUGADOR_VS_JUGADOR;
    }

    /**
     * Elige la modalidad de juego.
     *
     * @param nuevaModalidad JUGADOR_VS_JUGADOR (la única disponible por ahora)
     * @throws DOPOSoccerExcepcion si piden jugador vs máquina, que aún no existe
     */
    public void seleccionarModalidad(String nuevaModalidad) throws DOPOSoccerExcepcion {
        if (!JUGADOR_VS_JUGADOR.equals(nuevaModalidad)) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.MODALIDAD_NO_DISPONIBLE));
        }
        modalidad = nuevaModalidad;
    }

    /**
     * Guarda la configuración general.
     *
     * @param sonido si suenan los efectos
     * @param musica si suena la música
     * @param idioma Español o Inglés
     * @throws DOPOSoccerExcepcion si el idioma no está disponible
     */
    public void configurar(boolean sonido, boolean musica, String idioma) throws DOPOSoccerExcepcion {
        configuracion.setIdioma(idioma);
        configuracion.setSonidoActivo(sonido);
        configuracion.setMusicaActiva(musica);
    }

    /**
     * Arma un partido nuevo y lo deja listo para empezar.
     *
     * @param personaje1 personaje del jugador 1
     * @param personaje2 personaje del jugador 2
     * @param tipo       Partido.TIPO_TIEMPO o Partido.TIPO_GOLES
     * @param valor      minutos o goles, según el tipo
     * @param resolucion tamaño de la cancha (Cancha.PEQUEÑA, MEDIANA o GRANDE)
     * @return el partido creado
     * @throws DOPOSoccerExcepcion si algo de lo anterior no es válido
     */
    public Partido iniciarPartido(Personaje personaje1, Personaje personaje2, String tipo, int valor,
                                  int resolucion) throws DOPOSoccerExcepcion {
        if (personaje1 == null || personaje2 == null) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.PERSONAJE_VACIO));
        }
        Cancha cancha = new Cancha(resolucion);
        jugador1.prepararPartido(personaje1);
        jugador2.prepararPartido(personaje2);
        partido = new Partido(jugador1, jugador2, tipo, valor, cancha);
        configuracion.setResolucion(resolucion);
        return partido;
    }

    /** @return una copia de la lista de personajes que se pueden elegir */
    public Personaje[] getPersonajes() {
        return personajes.clone();
    }

    public Configuracion getConfiguracion() {
        return configuracion;
    }

    public String getModalidad() {
        return modalidad;
    }

    public Partido getPartido() {
        return partido;
    }

    public Jugador getJugador1() {
        return jugador1;
    }

    public Jugador getJugador2() {
        return jugador2;
    }
}
