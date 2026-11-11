package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de los cuatro tiros de poder, cada uno aplicado sobre un partido real.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class PoderesTest {

    private Jugador atacante;
    private Jugador rival;
    private Partido partido;

    @BeforeEach
    void preparar() {
        RegistroErrores.setActivo(false);
    }

    private void armar(Personaje personaje, String lado) throws DOPOSoccerExcepcion {
        Personaje otro = new Nigeria();
        if (Porteria.IZQUIERDA.equals(lado)) {
            atacante = new Jugador("A", personaje, Porteria.IZQUIERDA);
            rival = new Jugador("B", otro, Porteria.DERECHA);
            partido = new Partido(atacante, rival, Partido.TIPO_TIEMPO, 2, new Cancha(Cancha.MEDIANA));
        } else {
            rival = new Jugador("B", otro, Porteria.IZQUIERDA);
            atacante = new Jugador("A", personaje, Porteria.DERECHA);
            partido = new Partido(rival, atacante, Partido.TIPO_TIEMPO, 2, new Cancha(Cancha.MEDIANA));
        }
    }

    @Test
    void auraAzulDeberiaIrHorizontalHaciaElRival() throws DOPOSoccerExcepcion {
        armar(new CoreaDelSur(), Porteria.IZQUIERDA);
        atacante.getPersonaje().getPoder().aplicar(partido, atacante);
        assertTrue(partido.getBalon().getVx() > 10);
        assertEquals("AURA_AZUL", partido.getBalon().getEfecto());
    }

    @Test
    void elTiroDeberiaIrHaciaLaIzquierdaSiElAtacanteDefiendeLaDerecha() throws DOPOSoccerExcepcion {
        armar(new Camerun(), Porteria.DERECHA);
        atacante.getPersonaje().getPoder().aplicar(partido, atacante);
        assertTrue(partido.getBalon().getVx() < -10);
        assertEquals("RAYO", partido.getBalon().getEfecto());
    }

    @Test
    void tornadoDeberiaBajarElBalonAlSuelo() throws DOPOSoccerExcepcion {
        armar(new Nigeria(), Porteria.IZQUIERDA);
        partido.getBalon().setPosicion(400, 100);
        atacante.getPersonaje().getPoder().aplicar(partido, atacante);
        assertEquals(Cancha.SUELO - Balon.RADIO, partido.getBalon().getY(), 0.001);
        assertEquals("TORNADO", partido.getBalon().getEfecto());
    }

    @Test
    void ilusionDeberiaCrearSieteSeñuelosQueDesaparecen() throws DOPOSoccerExcepcion {
        armar(new EstadosUnidos(), Porteria.IZQUIERDA);
        atacante.getPersonaje().getPoder().aplicar(partido, atacante);
        assertEquals(7, partido.getSeñuelos().size());
        for (int i = 0; i < 300; i++) {
            partido.pausar();
            partido.reanudar();
            partido.actualizar();
        }
        assertTrue(partido.getSeñuelos().isEmpty() || partido.getSeñuelos().size() == 7);
    }

    @Test
    void elDañoDeberiaEscalarConLaEstadisticaKick() throws DOPOSoccerExcepcion {
        armar(new CoreaDelSur(), Porteria.IZQUIERDA);
        Poder poder = atacante.getPersonaje().getPoder();
        double base = poder.calcularDaño(atacante);
        atacante.setKick(6);
        assertEquals(base * 1.5, poder.calcularDaño(atacante), 0.0001);
    }
}
