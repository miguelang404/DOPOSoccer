package presentation;

import util.RegistroErrores;

import javax.swing.*;

/**
 * Arranca el juego.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
public class Main {

    private Main() {
        // Solo tiene el main.
    }

    /**
     * @param args no se usan
     */
    public static void main(String[] args) {
        RegistroErrores.setArchivo("logs/errores.log");
        System.setProperty("sun.java2d.opengl", "false");
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
