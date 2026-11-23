package co.edu.javeriana.fis.electoral.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Candidato")
class CandidatoTest {

    private PartidoPolitico partido;

    @BeforeEach
    void setUp() {
        partido = new PartidoPolitico("PL", "Partido Liberal");
    }

    // ---- constructor ----
    @Test
    @DisplayName("constructor: crea el candidato con su partido")
    void constructorValido() {
        Candidato c = new Candidato("1012345678", "Ana Gómez", partido);
        assertEquals("1012345678", c.getDocumento());
        assertEquals("Ana Gómez", c.getNombre());
        assertSame(partido, c.getPartido());
    }

    @Test
    @DisplayName("constructor (límite): documento de 6 y de 10 dígitos son válidos")
    void constructorDocumentoLimites() {
        assertDoesNotThrow(() -> new Candidato("123456", "Mínimo", partido));
        assertDoesNotThrow(() -> new Candidato("1234567890", "Máximo", partido));
    }

    @Test
    @DisplayName("constructor (negativa): documento de 5 u 11 dígitos se rechaza")
    void constructorDocumentoFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> new Candidato("12345", "Corto", partido));
        assertThrows(IllegalArgumentException.class, () -> new Candidato("12345678901", "Largo", partido));
    }

    @Test
    @DisplayName("constructor (negativa): documento con letras se rechaza")
    void constructorDocumentoConLetras() {
        assertThrows(IllegalArgumentException.class, () -> new Candidato("12AB5678", "Ana", partido));
    }

    @Test
    @DisplayName("constructor (negativa): sin partido se rechaza")
    void constructorSinPartido() {
        assertThrows(IllegalArgumentException.class, () -> new Candidato("1012345678", "Ana", null));
    }

    // ---- perteneceA ----
    @Test
    @DisplayName("perteneceA: true para su propio partido")
    void perteneceASuPartido() {
        Candidato c = new Candidato("1012345678", "Ana Gómez", partido);
        assertTrue(c.perteneceA(partido));
    }

    @Test
    @DisplayName("perteneceA: false para otro partido")
    void perteneceAOtroPartido() {
        Candidato c = new Candidato("1012345678", "Ana Gómez", partido);
        assertFalse(c.perteneceA(new PartidoPolitico("PC", "Conservador")));
    }

    @Test
    @DisplayName("perteneceA (límite): false con partido nulo, sin lanzar excepción")
    void perteneceANulo() {
        Candidato c = new Candidato("1012345678", "Ana Gómez", partido);
        assertFalse(c.perteneceA(null));
    }

    // ---- equals ----
    @Test
    @DisplayName("equals: se identifica por documento")
    void equalsPorDocumento() {
        assertEquals(new Candidato("1012345678", "Ana", partido), new Candidato("1012345678", "Ana G.", partido));
    }

    @Test
    @DisplayName("equals (límite): distinto documento no es igual")
    void equalsDistintoDocumento() {
        assertNotEquals(new Candidato("1012345678", "Ana", partido), new Candidato("1012345679", "Ana", partido));
    }
}
