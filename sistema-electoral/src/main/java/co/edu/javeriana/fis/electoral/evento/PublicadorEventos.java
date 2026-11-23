package co.edu.javeriana.fis.electoral.evento;

import java.util.ArrayList;
import java.util.List;

/** Sujeto del Observer: mantiene la lista de observadores y les notifica cada evento. */
public class PublicadorEventos {

    private final List<ObservadorElectoral> observadores = new ArrayList<>();

    /** @return true si se suscribió; false si ya estaba suscrito. */
    public boolean suscribir(ObservadorElectoral observador) {
        if (observador == null) {
            throw new IllegalArgumentException("El observador no puede ser nulo");
        }
        if (observadores.contains(observador)) {
            return false;
        }
        return observadores.add(observador);
    }

    /** @return true si estaba suscrito y se retiró. */
    public boolean desuscribir(ObservadorElectoral observador) {
        return observadores.remove(observador);
    }

    /** Notifica el evento a todos los observadores. @return cuántos fueron notificados. */
    public int publicar(EventoElectoral evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo");
        }
        for (ObservadorElectoral observador : List.copyOf(observadores)) {
            observador.actualizar(evento);
        }
        return observadores.size();
    }

    public int cantidadObservadores() {
        return observadores.size();
    }
}
