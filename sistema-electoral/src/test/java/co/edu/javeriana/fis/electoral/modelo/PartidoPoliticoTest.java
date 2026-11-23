package co.edu.javeriana.fis.electoral.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PartidoPolitico")
class PartidoPoliticoTest {

    private PartidoPolitico partido;

    @BeforeEach
    void setUp() {
        partido = new PartidoPolitico("PL", "Partido Liberal");
    }

    // ---- constructor ----
    @Test
    @DisplayName("constructor: crea el partido y limpia espacios")
    void constructorValido() {
        PartidoPolitico p = new PartidoPolitico("  PV ", "  Partido Verde  ");
        assertEquals("PV", p.getCodigo());
        assertEquals("Partido Verde", p.getNombre());
    }

    @Test
    @DisplayName("constructor (negativa): código vacío lanza excepción")
    void constructorCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () -> new PartidoPolitico("  ", "Partido"));
    }

    @Test
    @DisplayName("constructor (negativa): nombre nulo lanza excepción")
    void constructorNombreNulo() {
        assertThrows(IllegalArgumentException.class, () -> new PartidoPolitico("PL", null));
    }

    // ---- avalarCandidato ----
    @Test
    @DisplayName("avalarCandidato: agrega un candidato del mismo partido")
    void avalarCandidatoValido() {
        Candidato c = new Candidato("1012345678", "Ana Gómez", partido);
        partido.avalarCandidato(c);
        assertEquals(1, partido.cantidadCandidatos());
        assertSame(c, partido.getCandidatosAvalados().get(0));
    }

    @Test
    @DisplayName("avalarCandidato (negativa): candidato nulo")
    void avalarCandidatoNulo() {
        assertThrows(IllegalArgumentException.class, () -> partido.avalarCandidato(null));
    }

    @Test
    @DisplayName("avalarCandidato (negativa): candidato de otro partido")
    void avalarCandidatoDeOtroPartido() {
        PartidoPolitico otro = new PartidoPolitico("PC", "Partido Conservador");
        Candidato c = new Candidato("1012345678", "Ana Gómez", otro);
        assertThrows(IllegalArgumentException.class, () -> partido.avalarCandidato(c));
    }

    @Test
    @DisplayName("avalarCandidato (negativa): el mismo candidato dos veces")
    void avalarCandidatoDuplicado() {
        Candidato c = new Candidato("1012345678", "Ana Gómez", partido);
        partido.avalarCandidato(c);
        assertThrows(IllegalStateException.class, () -> partido.avalarCandidato(c));
    }

    // ---- cantidadCandidatos ----
    @Test
    @DisplayName("cantidadCandidatos (límite): partido sin candidatos devuelve 0")
    void cantidadCandidatosVacio() {
        assertEquals(0, partido.cantidadCandidatos());
    }

    @Test
    @DisplayName("cantidadCandidatos: cuenta todos los avalados")
    void cantidadCandidatosVarios() {
        partido.avalarCandidato(new Candidato("1000001", "Uno", partido));
        partido.avalarCandidato(new Candidato("1000002", "Dos", partido));
        assertEquals(2, partido.cantidadCandidatos());
    }

    // ---- getCandidatosAvalados ----
    @Test
    @DisplayName("getCandidatosAvalados (límite): lista vacía al inicio")
    void candidatosAvaladosVacio() {
        assertTrue(partido.getCandidatosAvalados().isEmpty());
    }

    @Test
    @DisplayName("getCandidatosAvalados (negativa): la lista no se puede modificar desde fuera")
    void candidatosAvaladosInmodificable() {
        assertThrows(UnsupportedOperationException.class,
                () -> partido.getCandidatosAvalados().add(new Candidato("1000003", "Tres", partido)));
    }

    // ---- equals ----
    @Test
    @DisplayName("equals: dos partidos con el mismo código son iguales")
    void equalsMismoCodigo() {
        assertEquals(partido, new PartidoPolitico("PL", "Otro nombre"));
        assertEquals(partido.hashCode(), new PartidoPolitico("PL", "X").hashCode());
    }

    @Test
    @DisplayName("equals (límite): distinto código o nulo no son iguales")
    void equalsDistinto() {
        assertNotEquals(partido, new PartidoPolitico("PC", "Partido Liberal"));
        assertFalse(partido.equals(null));
    }
}
