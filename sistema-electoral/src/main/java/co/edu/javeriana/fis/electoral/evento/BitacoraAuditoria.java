package co.edu.javeriana.fis.electoral.evento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Observador concreto: guarda todos los eventos para trazabilidad y auditoría. */
public class BitacoraAuditoria implements ObservadorElectoral {

    private final List<EventoElectoral> registros = new ArrayList<>();

    @Override
    public void actualizar(EventoElectoral evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo");
        }
        registros.add(evento);
    }

    public List<EventoElectoral> filtrarPorTipo(TipoEvento tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo es obligatorio");
        }
        return registros.stream().filter(e -> e.tipo() == tipo).toList();
    }

    public int cantidadRegistros() {
        return registros.size();
    }

    public List<EventoElectoral> getRegistros() {
        return Collections.unmodifiableList(registros);
    }
}
