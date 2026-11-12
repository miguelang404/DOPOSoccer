package presentation;

import util.RegistroErrores;

import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Efectos de sonido del juego. No usamos archivos de audio: los sonidos se
 * fabrican con matemáticas (ondas y ruido) la primera vez que se necesitan.
 * Si el computador no tiene audio, el juego sigue funcionando en silencio.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
final class Sonidos {

    private static final float FRECUENCIA = 22050f;
    private static final AudioFormat FORMATO = new AudioFormat(FRECUENCIA, 16, 1, true, false);
    private static final Map<String, byte[]> CACHE = new HashMap<String, byte[]>();

    private static boolean activo = true;
    private static boolean disponible = true;
    private static int abiertos;

    private Sonidos() {
        // Clase de utilidad.
    }

    /**
     * Enciende o apaga los efectos (viene de la pantalla de configuración).
     *
     * @param valor true para que suenen
     */
    static void setActivo(boolean valor) {
        activo = valor;
    }

    /**
     * Reproduce un efecto.
     *
     * @param nombre patada, rebote, gol, silbato, poder, cuenta o ya
     */
    static void reproducir(String nombre) {
        if (!activo || !disponible || abiertos > 8) {
            return;
        }
        try {
            byte[] datos = obtener(nombre);
            final Clip clip = AudioSystem.getClip();
            clip.open(FORMATO, datos, 0, datos.length);
            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent evento) {
                    if (evento.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        abiertos--;
                    }
                }
            });
            abiertos++;
            clip.start();
        } catch (Exception | Error e) {
            // Sin tarjeta de sonido o sin permisos: seguimos jugando en silencio.
            disponible = false;
            RegistroErrores.registrar("Sonido", e);
        }
    }

    private static byte[] obtener(String nombre) {
        byte[] datos = CACHE.get(nombre);
        if (datos == null) {
            datos = fabricar(nombre);
            CACHE.put(nombre, datos);
        }
        return datos;
    }

    private static byte[] fabricar(String nombre) {
        // Recomendación de la IA generativa para generar los sonidos sin archivos: cada efecto se
        // construye con ondas seno y ruido, con una caída suave del volumen, y se guarda en bytes de 16
        // bits.
        Random azar = new Random(5);
        double duracion;
        if ("gol".equals(nombre)) {
            duracion = 0.9;
        } else if ("silbato".equals(nombre)) {
            duracion = 0.55;
        } else if ("poder".equals(nombre)) {
            duracion = 0.4;
        } else if ("patada".equals(nombre)) {
            duracion = 0.12;
        } else if ("rebote".equals(nombre)) {
            duracion = 0.09;
        } else {
            duracion = 0.16;
        }
        int n = (int) (duracion * FRECUENCIA);
        byte[] bytes = new byte[n * 2];
        double fase = 0;
        for (int i = 0; i < n; i++) {
            double t = i / FRECUENCIA;
            double progreso = i / (double) n;
            double valor;
            if ("patada".equals(nombre)) {
                double frec = 170 - 110 * progreso;
                fase += 2 * Math.PI * frec / FRECUENCIA;
                valor = (Math.sin(fase) * 0.8 + (azar.nextDouble() - 0.5) * 0.5) * Math.exp(-progreso * 5);
            } else if ("rebote".equals(nombre)) {
                fase += 2 * Math.PI * (260 - 120 * progreso) / FRECUENCIA;
                valor = Math.sin(fase) * Math.exp(-progreso * 6) * 0.7;
            } else if ("gol".equals(nombre)) {
                double[] notas = {523, 659, 784, 1047};
                int k = Math.min(3, (int) (progreso * 4));
                fase += 2 * Math.PI * notas[k] / FRECUENCIA;
                double ruido = (azar.nextDouble() - 0.5) * 0.25 * Math.min(1, progreso * 3);
                valor = Math.sin(fase) * 0.45 * (1 - progreso * 0.5) + ruido;
            } else if ("silbato".equals(nombre)) {
                double frec = 2600 + 120 * Math.sin(2 * Math.PI * 28 * t);
                fase += 2 * Math.PI * frec / FRECUENCIA;
                valor = Math.sin(fase) * 0.4 * Math.min(1, progreso * 20) * (1 - progreso);
            } else if ("poder".equals(nombre)) {
                double frec = 220 + 1400 * progreso * progreso;
                fase += 2 * Math.PI * frec / FRECUENCIA;
                valor = (Math.sin(fase) * 0.5 + (azar.nextDouble() - 0.5) * 0.3) * (1 - progreso);
            } else if ("ya".equals(nombre)) {
                fase += 2 * Math.PI * 880 / FRECUENCIA;
                valor = Math.sin(fase) * 0.45 * (1 - progreso);
            } else {
                fase += 2 * Math.PI * 520 / FRECUENCIA;
                valor = Math.sin(fase) * 0.4 * (1 - progreso);
            }
            short muestra = (short) Math.max(-32767, Math.min(32767, valor * 22000));
            bytes[2 * i] = (byte) (muestra & 0xFF);
            bytes[2 * i + 1] = (byte) ((muestra >> 8) & 0xFF);
        }
        return bytes;
    }
}
