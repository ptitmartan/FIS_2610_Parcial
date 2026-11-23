package co.edu.javeriana.fis.electoral.evento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MonitorReclamaciones (observador)")
class MonitorReclamacionesTest {

    private MonitorReclamaciones monitor;

    @BeforeEach
    void setUp() {
        monitor = new MonitorReclamaciones(2);
    }

    private void reclamar(String mesa) {
        monitor.actualizar(EventoElectoral.de(TipoEvento.RECLAMACION_PRESENTADA, mesa, "reclamo"));
    }

    // ---- constructor ----
    @Test
    @DisplayName("constructor (límite): umbral 1 es válido")
    void umbralMinimo() {
        assertEquals(1, new MonitorReclamaciones(1).getUmbral());
    }

    @Test
    @DisplayName("constructor (negativa): umbral 0 o negativo")
    void umbralInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new MonitorReclamaciones(0));
        assertThrows(IllegalArgumentException.class, () -> new MonitorReclamaciones(-5));
    }

    // ---- actualizar ----
    @Test
    @DisplayName("actualizar: cuenta las reclamaciones por mesa")
    void cuentaReclamaciones() {
        reclamar("M1");
        reclamar("M1");
        reclamar("M2");
        assertEquals(2, monitor.getReclamaciones("M1"));
        assertEquals(1, monitor.getReclamaciones("M2"));
    }

    @Test
    @DisplayName("actualizar: ignora eventos que no son reclamaciones")
    void ignoraOtrosEventos() {
        monitor.actualizar(EventoElectoral.de(TipoEvento.ACTA_REGISTRADA, "M1", "acta"));
        monitor.actualizar(EventoElectoral.de(TipoEvento.INCIDENCIA_REPORTADA, "M1", "inc"));
        assertEquals(0, monitor.getReclamaciones("M1"));
    }

    @Test
    @DisplayName("actualizar (negativa): evento nulo")
    void actualizarNulo() {
        assertThrows(IllegalArgumentException.class, () -> monitor.actualizar(null));
    }

    // ---- mesasEnRevision ----
    @Test
    @DisplayName("mesasEnRevision (límite): sin reclamaciones la lista está vacía")
    void sinMesasEnRevision() {
        assertTrue(monitor.mesasEnRevision().isEmpty());
    }

    @Test
    @DisplayName("mesasEnRevision (límite): entra justo al alcanzar el umbral, no antes")
    void umbralExacto() {
        reclamar("M1");
        assertTrue(monitor.mesasEnRevision().isEmpty());
        reclamar("M1");
        assertEquals(List.of("M1"), monitor.mesasEnRevision());
    }

    @Test
    @DisplayName("mesasEnRevision: devuelve las mesas ordenadas por código")
    void ordenadas() {
        reclamar("M9");
        reclamar("M9");
        reclamar("M3");
        reclamar("M3");
        assertEquals(List.of("M3", "M9"), monitor.mesasEnRevision());
    }
}
