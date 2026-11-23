package co.edu.javeriana.fis.electoral.novedad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reclamacion")
class ReclamacionTest {

    private Reclamacion reclamacion;

    @BeforeEach
    void setUp() {
        reclamacion = new Reclamacion("REC-PARTIDO-1", "M1", "Diferencia entre votos y firmas del acta",
                "Partido Liberal", TipoReclamante.PARTIDO);
    }

    @Test
    @DisplayName("constructor: crea la reclamación abierta del reclamante dado")
    void constructorValido() {
        assertEquals("RECLAMACION", reclamacion.getTipo());
        assertEquals(TipoReclamante.PARTIDO, reclamacion.getTipoReclamante());
        assertTrue(reclamacion.estaAbierta());
    }

    @Test
    @DisplayName("constructor (negativa): sin tipo de reclamante")
    void constructorSinReclamante() {
        assertThrows(IllegalArgumentException.class,
                () -> new Reclamacion("R", "M1", "Descripción suficiente", "Testigo", null));
    }

    @Test
    @DisplayName("constructor (negativa): descripción demasiado corta")
    void constructorDescripcionCorta() {
        assertThrows(IllegalArgumentException.class,
                () -> new Reclamacion("R", "M1", "corta", "Testigo", TipoReclamante.TESTIGO));
    }

    @Test
    @DisplayName("requiereRevisionActa: toda reclamación obliga a revisar el acta")
    void requiereRevision() {
        assertTrue(reclamacion.requiereRevisionActa());
    }

    @Test
    @DisplayName("requiereRevisionActa (límite): sigue siendo true aunque ya esté resuelta")
    void requiereRevisionResuelta() {
        reclamacion.resolver("Se recontaron los votos y el acta es correcta");
        assertTrue(reclamacion.requiereRevisionActa());
        assertFalse(reclamacion.estaAbierta());
    }

    @Test
    @DisplayName("resolver (negativa): respuesta nula")
    void resolverNulo() {
        assertThrows(IllegalArgumentException.class, () -> reclamacion.resolver(null));
    }
}
