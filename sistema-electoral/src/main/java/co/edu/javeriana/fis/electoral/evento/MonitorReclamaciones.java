package co.edu.javeriana.fis.electoral.evento;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Observador concreto: cuenta reclamaciones por mesa y marca para revisión detallada las mesas
 * que alcanzan el umbral configurado (apoya a los analistas a "detectar situaciones que
 * merezcan una revisión").
 */
public class MonitorReclamaciones implements ObservadorElectoral {

    private final int umbral;
    private final Map<String, Integer> reclamacionesPorMesa = new HashMap<>();

    public MonitorReclamaciones(int umbral) {
        if (umbral < 1) {
            throw new IllegalArgumentException("El umbral debe ser al menos 1");
        }
        this.umbral = umbral;
    }

    @Override
    public void actualizar(EventoElectoral evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo");
        }
        if (evento.tipo() == TipoEvento.RECLAMACION_PRESENTADA) {
            reclamacionesPorMesa.merge(evento.referencia(), 1, Integer::sum);
        }
    }

    public int getReclamaciones(String codigoMesa) {
        return reclamacionesPorMesa.getOrDefault(codigoMesa, 0);
    }

    /** Mesas con reclamaciones iguales o superiores al umbral, ordenadas por código. */
    public List<String> mesasEnRevision() {
        return reclamacionesPorMesa.entrySet().stream()
                .filter(e -> e.getValue() >= umbral)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    public int getUmbral() {
        return umbral;
    }
}
