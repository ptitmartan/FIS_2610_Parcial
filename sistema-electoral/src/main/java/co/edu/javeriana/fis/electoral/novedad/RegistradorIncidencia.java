package co.edu.javeriana.fis.electoral.novedad;

/** Creador concreto: fabrica incidencias con la severidad configurada. */
public class RegistradorIncidencia extends RegistradorNovedad {

    private final Severidad severidad;

    public RegistradorIncidencia(Severidad severidad) {
        if (severidad == null) {
            throw new IllegalArgumentException("La severidad es obligatoria");
        }
        this.severidad = severidad;
    }

    @Override
    protected Novedad crearNovedad(String id, String codigoMesa, String descripcion, String reportadoPor) {
        return new Incidencia(id, codigoMesa, descripcion, reportadoPor, severidad);
    }

    @Override
    protected String prefijo() {
        return "INC-" + severidad;
    }
}
