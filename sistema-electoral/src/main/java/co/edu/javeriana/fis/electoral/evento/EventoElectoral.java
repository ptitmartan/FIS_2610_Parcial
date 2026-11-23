package co.edu.javeriana.fis.electoral.evento;

import java.time.LocalDateTime;

/**
 * Evento inmutable que se envía a los observadores.
 *
 * @param tipo       qué ocurrió
 * @param referencia código del objeto afectado (proceso o mesa)
 * @param detalle    descripción legible del hecho
 * @param fecha      momento en que ocurrió
 */
public record EventoElectoral(TipoEvento tipo, String referencia, String detalle, LocalDateTime fecha) {

    public EventoElectoral {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de evento es obligatorio");
        }
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia del evento es obligatoria");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria");
        }
        detalle = detalle == null ? "" : detalle;
    }

    public static EventoElectoral de(TipoEvento tipo, String referencia, String detalle) {
        return new EventoElectoral(tipo, referencia, detalle, LocalDateTime.now());
    }
}
