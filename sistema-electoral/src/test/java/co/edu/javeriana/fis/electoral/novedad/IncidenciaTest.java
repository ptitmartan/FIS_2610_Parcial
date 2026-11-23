package co.edu.javeriana.fis.electoral.novedad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Incidencia")
class IncidenciaTest {

    private static final String DESCRIPCION = "Faltan tarjetones en la mesa";
    private Incidencia incidencia;

    @BeforeEach
    void setUp() {
        incidencia = new Incidencia("INC-ALTA-1", "M1", DESCRIPCION, "Jurado 52123456", Severidad.ALTA);
    }

    // ---- constructor ----
    @Test
    @DisplayName("constructor: queda abierta con fecha de registro")
    void constructorValido() {
        assertTrue(incidencia.estaAbierta());
        assertNotNull(incidencia.getFechaRegistro());
        assertEquals("INCIDENCIA", incidencia.getTipo());
        assertEquals(Severidad.ALTA, incidencia.getSeveridad());
    }

    @Test
    @DisplayName("constructor (límite): descripción de 10 y de 500 caracteres es válida")
    void descripcionLimitesValidos() {
        assertDoesNotThrow(() -> new Incidencia("I", "M1", "a".repeat(10), "Jurado", Severidad.BAJA));
        assertDoesNotThrow(() -> new Incidencia("I", "M1", "a".repeat(500), "Jurado", Severidad.BAJA));
    }

    @Test
    @DisplayName("constructor (negativa): descripción de 9 o 501 caracteres se rechaza")
    void descripcionFueraDeRango() {
        assertThrows(IllegalArgumentException.class,
                () -> new Incidencia("I", "M1", "a".repeat(9), "Jurado", Severidad.BAJA));
        assertThrows(IllegalArgumentException.class,
                () -> new Incidencia("I", "M1", "a".repeat(501), "Jurado", Severidad.BAJA));
    }

    @Test
    @DisplayName("constructor (negativa): sin severidad, sin reportante o sin mesa")
    void constructorCamposFaltantes() {
        assertThrows(IllegalArgumentException.class, () -> new Incidencia("I", "M1", DESCRIPCION, "Jurado", null));
        assertThrows(IllegalArgumentException.class, () -> new Incidencia("I", "M1", DESCRIPCION, " ", Severidad.BAJA));
        assertThrows(IllegalArgumentException.class, () -> new Incidencia("I", null, DESCRIPCION, "J", Severidad.BAJA));
    }

    // ---- esCritica ----
    @Test
    @DisplayName("esCritica: true solo con severidad ALTA")
    void esCriticaAlta() {
        assertTrue(incidencia.esCritica());
    }

    @Test
    @DisplayName("esCritica: false con severidad BAJA y MEDIA")
    void esCriticaOtras() {
        assertFalse(new Incidencia("I", "M1", DESCRIPCION, "J", Severidad.BAJA).esCritica());
        assertFalse(new Incidencia("I", "M1", DESCRIPCION, "J", Severidad.MEDIA).esCritica());
    }

    // ---- requiereRevisionActa ----
    @Test
    @DisplayName("requiereRevisionActa: una incidencia logística no obliga a revisar el acta")
    void noRequiereRevision() {
        assertFalse(incidencia.requiereRevisionActa());
    }

    // ---- resolver / estaAbierta ----
    @Test
    @DisplayName("resolver: cierra la novedad y guarda la respuesta")
    void resolverValido() {
        incidencia.resolver("Se enviaron 200 tarjetones adicionales");
        assertFalse(incidencia.estaAbierta());
        assertEquals("Se enviaron 200 tarjetones adicionales", incidencia.getRespuesta());
    }

    @Test
    @DisplayName("resolver (negativa): no se puede resolver dos veces")
    void resolverDosVeces() {
        incidencia.resolver("Atendida");
        assertThrows(IllegalStateException.class, () -> incidencia.resolver("Otra respuesta"));
        assertEquals("Atendida", incidencia.getRespuesta());
    }

    @Test
    @DisplayName("resolver (negativa/límite): respuesta vacía no cierra la novedad")
    void resolverVacio() {
        assertThrows(IllegalArgumentException.class, () -> incidencia.resolver("   "));
        assertTrue(incidencia.estaAbierta());
    }
}
