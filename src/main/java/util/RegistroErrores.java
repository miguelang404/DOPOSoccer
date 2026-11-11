package util;
import exceptions.DOPOSoccerExcepcion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Bitácora de errores para los programadores. Cada vez que algo sale mal se
 * escribe una línea en el archivo logs/errores.log con la fecha, el lugar y el
 * detalle técnico. El jugador solo ve el mensaje amable; esto es para nosotros.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class RegistroErrores {

    private static Path archivo = Paths.get("logs", "errores.log");
    private static boolean activo = true;

    private RegistroErrores() {
        // Clase de utilidad: no se crean objetos de ella.
    }

    /**
     * Cambia el archivo donde se guarda el registro (las pruebas lo usan para
     * no ensuciar el log real).
     *
     * @param ruta ruta del nuevo archivo
     */
    public static void setArchivo(String ruta) {
        archivo = Paths.get(ruta);
    }

    /**
     * Apaga o enciende el registro.
     *
     * @param valor true para escribir en el archivo
     */
    public static void setActivo(boolean valor) {
        activo = valor;
    }

    /**
     * Guarda una excepción del juego en el log y la devuelve, para poder
     * escribir directamente: throw RegistroErrores.registrar(new ...).
     *
     * @param error la excepción que ocurrió
     * @return la misma excepción
     */
    public static DOPOSoccerExcepcion registrar(DOPOSoccerExcepcion error) {
        escribir("JUEGO", error);
        return error;
    }

    /**
     * Guarda cualquier otro error inesperado (por ejemplo de la interfaz).
     *
     * @param contexto dónde pasó
     * @param error    lo que pasó
     */
    public static void registrar(String contexto, Throwable error) {
        escribir(contexto, error);
    }

    private static void escribir(String contexto, Throwable error) {
        if (!activo) {
            return;
        }
        String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String linea = hora + " | " + contexto + " | " + error.getClass().getSimpleName()
                + " | " + error.getMessage() + System.lineSeparator();
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            Files.write(archivo, linea.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            // Si ni siquiera se puede escribir el log, al menos lo mostramos en consola.
            System.err.println("No pude guardar el log: " + e.getMessage());
            System.err.println(linea);
        }
    }
}
