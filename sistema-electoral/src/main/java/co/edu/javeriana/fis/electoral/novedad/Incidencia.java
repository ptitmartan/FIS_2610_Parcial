package co.edu.javeriana.fis.electoral.novedad;

/** Problema logístico o incidente en un puesto/mesa (falta de material, orden público...). */
public class Incidencia extends Novedad {

    private final Severidad severidad;

    public Incidencia(String id, String codigoMesa, String descripcion, String reportadoPor, Severidad severidad) {
        super(id, codigoMesa, descripcion, reportadoPor);
        if (severidad == null) {
            throw new IllegalArgumentException("La severidad es obligatoria");
        }
        this.severidad = severidad;
    }

    /** Una incidencia crítica debe atenderse de inmediato por la autoridad electoral. */
    public boolean esCritica() {
        return severidad == Severidad.ALTA;
    }

    @Override
    public String getTipo() {
        return "INCIDENCIA";
    }

    @Override
    public boolean requiereRevisionActa() {
        return false;
    }

    public Severidad getSeveridad() {
        return severidad;
    }
}
