package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del marcador.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 1
 */
class MarcadorTest {

    private Jugador j1;
    private Jugador j2;
    private Marcador marcador;

    @BeforeEach
    void preparar() {
        j1 = new Jugador("Jugador 1", new CoreaDelSur(), Porteria.IZQUIERDA);
        j2 = new Jugador("Jugador 2", new Camerun(), Porteria.DERECHA);
        marcador = new Marcador(j1, j2);
    }

    @Test
    void deberiaEmpezarEmpatadoACero() {
        assertTrue(marcador.hayEmpate());
        assertNull(marcador.getGanador());
        assertEquals(0, marcador.getGoles(j1));
    }

    @Test
    void deberiaSumarGolesACadaJugador() {
        marcador.sumarGol(j1);
        marcador.sumarGol(j1);
        marcador.sumarGol(j2);
        assertEquals(2, marcador.getGolesJugador1());
        assertEquals(1, marcador.getGoles(j2));
        assertFalse(marcador.hayEmpate());
        assertSame(j1, marcador.getGanador());
    }

    @Test
    void noDeberiaAceptarUnJugadorQueNoEsDelPartido() {
        Jugador otro = new Jugador("Otro", new Nigeria(), Porteria.DERECHA);
        assertThrows(IllegalArgumentException.class, () -> marcador.sumarGol(otro));
    }

    @Test
    void reiniciarDeberiaVolverACero() {
        marcador.sumarGol(j2);
        marcador.reiniciar();
        assertTrue(marcador.hayEmpate());
    }
}
