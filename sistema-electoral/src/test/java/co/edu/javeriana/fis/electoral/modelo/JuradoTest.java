package co.edu.javeriana.fis.electoral.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Jurado")
class JuradoTest {

    private Jurado jurado;

    @BeforeEach
    void setUp() {
        jurado = new Jurado("52123456", "Laura Pérez");
    }

    // ---- constructor ----
    @Test
    @DisplayName("constructor: crea el jurado sin mesa asignada")
    void constructorValido() {
        assertEquals("52123456", jurado.getDocumento());
        assertEquals("Laura Pérez", jurado.getNombre());
        assertNull(jurado.getCodigoMesaAsignada());
    }

    @Test
    @DisplayName("constructor (negativa): documento inválido")
    void constructorDocumentoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Jurado("abc", "Laura"));
        assertThrows(IllegalArgumentException.class, () -> new Jurado(null, "Laura"));
    }

    @Test
    @DisplayName("constructor (negativa): nombre vacío")
    void constructorNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Jurado("52123456", " "));
    }

    @Test
    @DisplayName("constructor (límite): documento de 6 y 10 dígitos")
    void constructorLimitesDocumento() {
        assertDoesNotThrow(() -> new Jurado("123456", "A"));
        assertDoesNotThrow(() -> new Jurado("1234567890", "B"));
    }

    // ---- asignarAMesa ----
    @Test
    @DisplayName("asignarAMesa: guarda la mesa asignada")
    void asignarAMesaValido() {
        jurado.asignarAMesa("11001-P1-M1");
        assertEquals("11001-P1-M1", jurado.getCodigoMesaAsignada());
    }

    @Test
    @DisplayName("asignarAMesa (negativa): no se puede asignar a dos mesas")
    void asignarAMesaDosVeces() {
        jurado.asignarAMesa("11001-P1-M1");
        assertThrows(IllegalStateException.class, () -> jurado.asignarAMesa("11001-P1-M2"));
        assertEquals("11001-P1-M1", jurado.getCodigoMesaAsignada());
    }

    @Test
    @DisplayName("asignarAMesa (negativa): código de mesa vacío o nulo")
    void asignarAMesaCodigoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> jurado.asignarAMesa(""));
        assertThrows(IllegalArgumentException.class, () -> jurado.asignarAMesa(null));
    }

    // ---- estaAsignado ----
    @Test
    @DisplayName("estaAsignado (límite): jurado recién creado no está asignado")
    void estaAsignadoInicial() {
        assertFalse(jurado.estaAsignado());
    }

    @Test
    @DisplayName("estaAsignado: true después de asignarlo")
    void estaAsignadoDespues() {
        jurado.asignarAMesa("M1");
        assertTrue(jurado.estaAsignado());
    }

    @Test
    @DisplayName("estaAsignado (negativa): un intento fallido no lo deja asignado")
    void estaAsignadoTrasFallo() {
        assertThrows(IllegalArgumentException.class, () -> jurado.asignarAMesa(" "));
        assertFalse(jurado.estaAsignado());
    }
}
