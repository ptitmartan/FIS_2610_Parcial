package co.edu.javeriana.fis.electoral.evento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BitacoraAuditoria (observador)")
class BitacoraAuditoriaTest {

    private BitacoraAuditoria bitacora;

    @BeforeEach
    void setUp() {
        bitacora = new BitacoraAuditoria();
    }

    @Test
    @DisplayName("actualizar: guarda cada evento recibido en orden")
    void actualizar() {
        bitacora.actualizar(EventoElectoral.de(TipoEvento.ACTA_REGISTRADA, "M1", "a"));
        bitacora.actualizar(EventoElectoral.de(TipoEvento.INCIDENCIA_REPORTADA, "M2", "b"));
        assertEquals(2, bitacora.cantidadRegistros());
        assertEquals("M2", bitacora.getRegistros().get(1).referencia());
    }

    @Test
    @DisplayName("actualizar (negativa): evento nulo")
    void actualizarNulo() {
        assertThrows(IllegalArgumentException.class, () -> bitacora.actualizar(null));
        assertEquals(0, bitacora.cantidadRegistros());
    }

    @Test
    @DisplayName("filtrarPorTipo: devuelve solo los eventos del tipo pedido")
    void filtrar() {
        bitacora.actualizar(EventoElectoral.de(TipoEvento.ACTA_REGISTRADA, "M1", "a"));
        bitacora.actualizar(EventoElectoral.de(TipoEvento.RECLAMACION_PRESENTADA, "M1", "b"));
        bitacora.actualizar(EventoElectoral.de(TipoEvento.ACTA_REGISTRADA, "M2", "c"));
        assertEquals(2, bitacora.filtrarPorTipo(TipoEvento.ACTA_REGISTRADA).size());
    }

    @Test
    @DisplayName("filtrarPorTipo (límite): bitácora vacía devuelve lista vacía")
    void filtrarVacio() {
        assertTrue(bitacora.filtrarPorTipo(TipoEvento.ACTA_REGISTRADA).isEmpty());
    }

    @Test
    @DisplayName("filtrarPorTipo (negativa): tipo nulo")
    void filtrarNulo() {
        assertThrows(IllegalArgumentException.class, () -> bitacora.filtrarPorTipo(null));
    }

    @Test
    @DisplayName("getRegistros (negativa): la bitácora no se puede alterar desde fuera (auditoría)")
    void registrosInmodificables() {
        assertThrows(UnsupportedOperationException.class, () -> bitacora.getRegistros().clear());
    }
}
