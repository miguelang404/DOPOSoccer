package domain;

import exceptions.DOPOSoccerExcepcion;
import util.RegistroErrores;

/**
 * Las preferencias del juego: sonido, música, idioma y la resolución con la que
 * aparece marcada la cancha al preparar un partido.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Configuracion {

    public static final String ESPAÑOL = "Español";
    public static final String INGLES = "Inglés";

    private boolean sonidoActivo = true;
    private boolean musicaActiva = true;
    private String idioma = ESPAÑOL;
    private int resolucion = Cancha.MEDIANA;

    public boolean isSonidoActivo() {
        return sonidoActivo;
    }

    public void setSonidoActivo(boolean sonidoActivo) {
        this.sonidoActivo = sonidoActivo;
    }

    public boolean isMusicaActiva() {
        return musicaActiva;
    }

    public void setMusicaActiva(boolean musicaActiva) {
        this.musicaActiva = musicaActiva;
    }

    public String getIdioma() {
        return idioma;
    }

    /**
     * Cambia el idioma.
     *
     * @param idioma Español o Inglés
     * @throws DOPOSoccerExcepcion si el idioma no está disponible
     */
    public void setIdioma(String idioma) throws DOPOSoccerExcepcion {
        if (!ESPAÑOL.equals(idioma) && !INGLES.equals(idioma)) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.IDIOMA_INVALIDO));
        }
        this.idioma = idioma;
    }

    public int getResolucion() {
        return resolucion;
    }

    /**
     * Guarda la resolución preferida.
     *
     * @param resolucion Cancha.PEQUEÑA, MEDIANA o GRANDE
     * @throws DOPOSoccerExcepcion si no existe
     */
    public void setResolucion(int resolucion) throws DOPOSoccerExcepcion {
        if (resolucion < 0 || resolucion >= Cancha.cantidadResoluciones()) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.RESOLUCION_INVALIDA));
        }
        this.resolucion = resolucion;
    }
}
