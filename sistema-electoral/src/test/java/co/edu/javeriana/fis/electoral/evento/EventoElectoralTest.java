package co.edu.javeriana.fis.electoral.evento;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventoElectoral")
class EventoElectoralTest {

    @Test
    @DisplayName("de: crea el evento con la fecha actual")
    void crearEvento() {
        EventoElectoral e = EventoElectoral.de(TipoEvento.ACTA_REGISTRADA, "M1", "Acta con 200 votos");
        assertEquals(TipoEvento.ACTA_REGISTRADA, e.tipo());
        assertEquals("M1", e.referencia());
        assertNotNull(e.fecha());
    }

    @Test
    @DisplayName("constructor (negativa): tipo nulo, referencia vacía o fecha nula")
    void eventoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> EventoElectoral.de(null, "M1", "x"));
        assertThrows(IllegalArgumentException.class, () -> EventoElectoral.de(TipoEvento.ACTA_REGISTRADA, " ", "x"));
        assertThrows(IllegalArgumentException.class,
                () -> new EventoElectoral(TipoEvento.ACTA_REGISTRADA, "M1", "x", null));
    }

    @Test
    @DisplayName("constructor (límite): detalle nulo se convierte en texto vacío")
    void detalleNulo() {
        EventoElectoral e = new EventoElectoral(TipoEvento.ACTA_REGISTRADA, "M1", null, LocalDateTime.now());
        assertEquals("", e.detalle());
    }
}
