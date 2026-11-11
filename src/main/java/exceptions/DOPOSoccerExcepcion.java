package exceptions;

/**
 * Excepción propia de DOPOSoccer. La usamos cuando alguien le pide al juego algo
 * que no puede cumplir (una resolución que no existe, un partido de 0 goles,
 * un personaje vacío...). Así los mensajes que ve el jugador son claros y no un
 * simple "NullPointerException".
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class DOPOSoccerExcepcion extends Exception {

    // Mensajes que se repiten en varias clases. Los dejo como constantes
    // para no escribirlos distinto en cada lado.
    public static final String RESOLUCION_INVALIDA =
            "La resolución elegida no existe. Escoge Pequeña, Mediana o Grande.";
    public static final String PERSONAJE_VACIO =
            "Cada jugador debe elegir un personaje antes de empezar el partido.";
    public static final String TIPO_PARTIDO_INVALIDO =
            "El tipo de partido debe ser por tiempo o por goles.";
    public static final String MINUTOS_INVALIDOS =
            "Un partido por tiempo debe durar entre 1 y 10 minutos.";
    public static final String GOLES_INVALIDOS =
            "Un partido por goles debe ganarse entre 1 y 20 goles.";
    public static final String IDIOMA_INVALIDO =
            "Ese idioma todavía no está disponible. Por ahora puedes usar Español o Inglés.";
    public static final String MODALIDAD_NO_DISPONIBLE =
            "Jugador vs máquina todavía no está disponible. Llegará en la versión dos.";
    public static final String ESTADISTICA_INVALIDA =
            "Las estadísticas de un jugador deben estar entre 1 y 10.";
    public static final String JUGADORES_VACIOS =
            "El partido necesita dos jugadores y una cancha.";

    /**
     * Crea la excepción con el mensaje que se le mostrará al usuario.
     *
     * @param mensaje explicación del problema
     */
    public DOPOSoccerExcepcion(String mensaje) {
        super(mensaje);
    }
}
