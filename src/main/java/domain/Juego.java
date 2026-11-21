package domain;

import exceptions.DOPOSoccerExcepcion;
import util.RegistroErrores;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Puerta de entrada al dominio. La interfaz habla con esta clase para ver los
 * personajes disponibles, guardar la configuración, empezar un partido (contra otra
 * persona o contra la máquina), guardar y abrir partidas, y conservar el progreso de
 * los jugadores (puntos y estadísticas). Ella se encarga de armar todo lo demás.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
 */
public class Juego {

    public static final String JUGADOR_VS_JUGADOR = "JUGADOR_VS_JUGADOR";
    public static final String JUGADOR_VS_MAQUINA = "JUGADOR_VS_MAQUINA";

    /** Los perfiles de máquina disponibles. Para agregar uno nuevo se suma aquí y en crearMaquina. */
    private static final String[] PERFILES = {Maquina.TORPE, Maquina.OFENSIVA, Maquina.DEFENSIVA, Maquina.EXPERTA};

    private final Configuracion configuracion;
    private final Personaje[] personajes;
    private Jugador jugador1;
    private Jugador jugador2;
    private String modalidad;
    private String perfilMaquina;
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
     * @param nuevaModalidad JUGADOR_VS_JUGADOR o JUGADOR_VS_MAQUINA
     * @throws DOPOSoccerExcepcion si la modalidad no existe
     */
    public void seleccionarModalidad(String nuevaModalidad) throws DOPOSoccerExcepcion {
        if (!JUGADOR_VS_JUGADOR.equals(nuevaModalidad) && !JUGADOR_VS_MAQUINA.equals(nuevaModalidad)) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.MODALIDAD_NO_DISPONIBLE));
        }
        modalidad = nuevaModalidad;
    }

    /**
     * Elige el perfil de la máquina (solo se usa en jugador contra máquina).
     *
     * @param perfil Torpe, Ofensiva, Defensiva o Experta
     * @throws DOPOSoccerExcepcion si el perfil no existe
     */
    public void seleccionarPerfil(String perfil) throws DOPOSoccerExcepcion {
        for (String conocido : PERFILES) {
            if (conocido.equals(perfil)) {
                perfilMaquina = perfil;
                return;
            }
        }
        throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.PERFIL_INVALIDO));
    }

    /**
     * Crea una máquina del perfil pedido.
     *
     * @param perfil el nombre del perfil
     * @return la máquina
     * @throws DOPOSoccerExcepcion si el perfil no existe
     */
    private Maquina crearMaquina(String perfil) throws DOPOSoccerExcepcion {
        long semilla = System.nanoTime();
        if (Maquina.TORPE.equals(perfil)) {
            return new MaquinaTorpe(semilla);
        } else if (Maquina.OFENSIVA.equals(perfil)) {
            return new MaquinaOfensiva(semilla);
        } else if (Maquina.DEFENSIVA.equals(perfil)) {
            return new MaquinaDefensiva(semilla);
        } else if (Maquina.EXPERTA.equals(perfil)) {
            return new MaquinaExperta(semilla);
        }
        throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.PERFIL_INVALIDO));
    }

    /**
     * Explica en una frase cómo juega un perfil de máquina.
     *
     * @param perfil el nombre del perfil
     * @return la descripción
     * @throws DOPOSoccerExcepcion si el perfil no existe
     */
    public String getDescripcionPerfil(String perfil) throws DOPOSoccerExcepcion {
        return crearMaquina(perfil).getDescripcion();
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
     * Arma un partido nuevo y lo deja listo para empezar. Si la modalidad es jugador contra
     * máquina, el jugador 2 queda controlado por la máquina del perfil elegido.
     *
     * @param personaje1 personaje del jugador 1
     * @param personaje2 personaje del jugador 2
     * @param tipo       Partido.TIPO_TIEMPO o Partido.TIPO_GOLES
     * @param valor      minutos o goles, según el tipo
     * @param resolucion tamaño de la cancha (Cancha.PEQUEÑA, MEDIANA o GRANDE)
     * @return el partido creado
     * @throws DOPOSoccerExcepcion si algo de lo anterior no es válido, o si falta el perfil de la máquina
     */
    public Partido iniciarPartido(Personaje personaje1, Personaje personaje2, String tipo, int valor,
                                  int resolucion) throws DOPOSoccerExcepcion {
        if (personaje1 == null || personaje2 == null) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.PERSONAJE_VACIO));
        }
        Maquina maquina = null;
        if (JUGADOR_VS_MAQUINA.equals(modalidad)) {
            if (perfilMaquina == null) {
                throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.SIN_PERFIL));
            }
            maquina = crearMaquina(perfilMaquina);
        }
        Cancha cancha = new Cancha(resolucion);
        jugador1.prepararPartido(personaje1);
        jugador2.prepararPartido(personaje2);
        partido = new Partido(jugador1, jugador2, tipo, valor, cancha);
        partido.setMaquina(maquina);
        configuracion.setResolucion(resolucion);
        return partido;
    }

    // guardar y abrir partidas

    /**
     * Guarda el estado completo del partido que se está jugando (marcador, tiempo, posiciones,
     * barras, vidas, efectos y la máquina, si la hay) en un archivo.
     *
     * @param archivo dónde guardarlo
     * @throws DOPOSoccerExcepcion si no hay partido en juego o no se puede escribir el archivo
     */
    public void guardarPartida(File archivo) throws DOPOSoccerExcepcion {
        if (partido == null || Partido.ESTADO_TERMINADO.equals(partido.getEstado())
                || Partido.ESTADO_ABANDONADO.equals(partido.getEstado())) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.SIN_PARTIDO_PARA_GUARDAR));
        }
        try {
            File carpeta = archivo.getAbsoluteFile().getParentFile();
            if (carpeta != null) {
                Files.createDirectories(carpeta.toPath());
            }
            try (OutputStream salida = Files.newOutputStream(archivo.toPath());
                 ObjectOutputStream objetos = new ObjectOutputStream(salida)) {
                objetos.writeObject(partido);
            }
        } catch (IOException e) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.NO_SE_PUDO_GUARDAR, e));
        }
    }

    /**
     * Abre una partida guardada y la deja como el partido actual. Los jugadores quedan con los
     * puntos y las estadísticas que tenían al guardar.
     *
     * @param archivo el archivo guardado antes
     * @return el partido recuperado (normalmente en pausa, tal como se guardó)
     * @throws DOPOSoccerExcepcion si el archivo no existe, está dañado o no es una partida
     */
    public Partido abrirPartida(File archivo) throws DOPOSoccerExcepcion {
        try (InputStream entrada = Files.newInputStream(archivo.toPath());
             ObjectInputStream objetos = new ObjectInputStream(entrada)) {
            Object leido = objetos.readObject();
            if (!(leido instanceof Partido)) {
                throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.NO_SE_PUDO_ABRIR));
            }
            Partido abierto = (Partido) leido;
            partido = abierto;
            jugador1 = abierto.getJugador1();
            jugador2 = abierto.getJugador2();
            Maquina maquina = abierto.getMaquina();
            modalidad = maquina == null ? JUGADOR_VS_JUGADOR : JUGADOR_VS_MAQUINA;
            perfilMaquina = maquina == null ? perfilMaquina : maquina.getPerfil();
            configuracion.setResolucion(abierto.getCancha().getResolucion());
            return abierto;
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.NO_SE_PUDO_ABRIR, e));
        }
    }

    // progreso de los jugadores

    /**
     * Guarda el progreso: los puntos y las estadísticas de los dos jugadores y la configuración
     * general. Es un archivo de texto sencillo.
     *
     * @param archivo dónde guardarlo
     * @throws DOPOSoccerExcepcion si no se puede escribir
     */
    public void guardarProgreso(Path archivo) throws DOPOSoccerExcepcion {
        Properties datos = new Properties();
        guardarJugador(datos, "j1", jugador1);
        guardarJugador(datos, "j2", jugador2);
        datos.setProperty("sonido", String.valueOf(configuracion.isSonidoActivo()));
        datos.setProperty("musica", String.valueOf(configuracion.isMusicaActiva()));
        datos.setProperty("idioma", configuracion.getIdioma());
        try {
            if (archivo.toAbsolutePath().getParent() != null) {
                Files.createDirectories(archivo.toAbsolutePath().getParent());
            }
            try (BufferedWriter escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                datos.store(escritor, "Progreso de DOPOSoccer");
            }
        } catch (IOException e) {
            throw RegistroErrores.registrar(new DOPOSoccerExcepcion(DOPOSoccerExcepcion.NO_SE_PUDO_GUARDAR, e));
        }
    }

    /**
     * Carga el progreso guardado antes. Si el archivo no existe no pasa nada (es la primera vez
     * que se juega); los valores que estén dañados se ignoran.
     *
     * @param archivo el archivo del progreso
     * @return true si se encontró y se cargó el archivo
     */
    public boolean cargarProgreso(Path archivo) {
        if (!Files.exists(archivo)) {
            return false;
        }
        Properties datos = new Properties();
        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            datos.load(lector);
        } catch (IOException | IllegalArgumentException e) {
            RegistroErrores.registrar("Juego.cargarProgreso", e);
            return false;
        }
        cargarJugador(datos, "j1", jugador1);
        cargarJugador(datos, "j2", jugador2);
        try {
            configuracion.setSonidoActivo(Boolean.parseBoolean(datos.getProperty("sonido", "true")));
            configuracion.setMusicaActiva(Boolean.parseBoolean(datos.getProperty("musica", "true")));
            configuracion.setIdioma(datos.getProperty("idioma", Configuracion.ESPAÑOL));
        } catch (DOPOSoccerExcepcion e) {
            RegistroErrores.registrar("Juego.cargarProgreso", e);
        }
        return true;
    }

    private void guardarJugador(Properties datos, String clave, Jugador jugador) {
        datos.setProperty(clave + ".puntos", String.valueOf(jugador.getPuntos()));
        datos.setProperty(clave + ".speed", String.valueOf(jugador.getSpeed()));
        datos.setProperty(clave + ".jump", String.valueOf(jugador.getJump()));
        datos.setProperty(clave + ".kick", String.valueOf(jugador.getKick()));
        datos.setProperty(clave + ".survival", String.valueOf(jugador.getSurvival()));
    }

    private void cargarJugador(Properties datos, String clave, Jugador jugador) {
        try {
            jugador.setSpeed(Integer.parseInt(datos.getProperty(clave + ".speed", "1").trim()));
            jugador.setJump(Integer.parseInt(datos.getProperty(clave + ".jump", "1").trim()));
            jugador.setKick(Integer.parseInt(datos.getProperty(clave + ".kick", "1").trim()));
            jugador.setSurvival(Integer.parseInt(datos.getProperty(clave + ".survival", "1").trim()));
            jugador.setPuntos(Integer.parseInt(datos.getProperty(clave + ".puntos", "0").trim()));
        } catch (NumberFormatException | DOPOSoccerExcepcion e) {
            RegistroErrores.registrar("Juego.cargarJugador", e);
        }
    }

    // consultas

    /** @return una copia de la lista de personajes que se pueden elegir */
    public Personaje[] getPersonajes() {
        return personajes.clone();
    }

    /** @return una copia de la lista de perfiles de máquina */
    public String[] getPerfiles() {
        return PERFILES.clone();
    }

    public Configuracion getConfiguracion() {
        return configuracion;
    }

    public String getModalidad() {
        return modalidad;
    }

    /** @return el perfil de máquina elegido (null si no se ha elegido ninguno) */
    public String getPerfilMaquina() {
        return perfilMaquina;
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
