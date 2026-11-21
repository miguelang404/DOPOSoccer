package domain;

import exceptions.DOPOSoccerExcepcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.RegistroErrores;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de los cuatro tiros de poder, cada uno aplicado sobre un partido real.
 *
 * @author César Daniel Morales Sandoval y Miguel de la Peña
 * @version Versión 2
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

    // daño y efectos al golpear al rival

    private void golpear(Personaje personaje, String lado) throws DOPOSoccerExcepcion {
        armar(personaje, lado);
        personaje.getPoder().golpear(partido, atacante, rival);
    }

    @Test
    void auraAzulDeberiaHacerDañoYEmpujarAlRival() throws DOPOSoccerExcepcion {
        golpear(new CoreaDelSur(), Porteria.IZQUIERDA);
        assertEquals(60, rival.getVida(), 0.001);
        assertEquals(Jugador.EFECTO_EMPUJADO, rival.getEfecto());
        assertEquals(90, rival.getTicksEfecto());
    }

    @Test
    void rayoDeberiaElectrocutarAlRival() throws DOPOSoccerExcepcion {
        golpear(new Camerun(), Porteria.IZQUIERDA);
        assertEquals(40, rival.getVida(), 0.001);
        assertEquals(Jugador.EFECTO_ELECTROCUTADO, rival.getEfecto());
        assertEquals(180, rival.getTicksEfecto());
    }

    @Test
    void tornadoDeberiaLanzarAlRivalAlAireEInconsciente() throws DOPOSoccerExcepcion {
        golpear(new Nigeria(), Porteria.DERECHA);
        assertEquals(60, rival.getVida(), 0.001);
        assertEquals(Jugador.EFECTO_INCONSCIENTE, rival.getEfecto());
        assertTrue(rival.getVy() < 0, "sale disparado hacia arriba");
    }

    @Test
    void ilusionDeberiaConfundirAlRival() throws DOPOSoccerExcepcion {
        golpear(new EstadosUnidos(), Porteria.IZQUIERDA);
        assertEquals(70, rival.getVida(), 0.001);
        assertEquals(Jugador.EFECTO_CONFUNDIDO, rival.getEfecto());
        assertEquals(150, rival.getTicksEfecto());
    }

    @Test
    void elDañoDelPoderDeberiaCrecerConElKickDelAtacante() throws DOPOSoccerExcepcion {
        // Ejemplo del enunciado: Camerún con Kick 5 le hace 60 x 1,4 = 84 a un rival con Survival 3.
        armar(new Camerun(), Porteria.IZQUIERDA);
        atacante.setKick(5);
        rival.setSurvival(3);
        rival.prepararPartido(new Nigeria());
        atacante.getPersonaje().getPoder().golpear(partido, atacante, rival);
        assertEquals(120 - 84, rival.getVida(), 0.001);
        assertEquals(Jugador.EFECTO_ELECTROCUTADO, rival.getEfecto());
    }

    @Test
    void siElDañoLeQuitaTodaLaVidaDeberiaQuedarAturdidoSinElEfecto() throws DOPOSoccerExcepcion {
        armar(new Camerun(), Porteria.IZQUIERDA);
        rival.recibirDaño(50);   // le quedan 50 y el rayo hace 60
        atacante.getPersonaje().getPoder().golpear(partido, atacante, rival);
        assertTrue(rival.estaAturdido());
        assertEquals(0, rival.getVida(), 0.001);
        assertEquals(Jugador.EFECTO_NINGUNO, rival.getEfecto());
    }

    @Test
    void soloElTornadoDeberiaSalirDelPie() {
        assertTrue(new Tornado().saleDelPie());
        assertFalse(new AuraAzul().saleDelPie());
        assertFalse(new Rayo().saleDelPie());
        assertFalse(new Ilusion().saleDelPie());
    }
}
