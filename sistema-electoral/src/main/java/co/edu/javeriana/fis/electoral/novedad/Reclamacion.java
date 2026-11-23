package co.edu.javeriana.fis.electoral.novedad;

/** Solicitud de un partido o testigo electoral para revisar los resultados de una mesa. */
public class Reclamacion extends Novedad {

    private final TipoReclamante tipoReclamante;

    public Reclamacion(String id, String codigoMesa, String descripcion, String reportadoPor,
                       TipoReclamante tipoReclamante) {
        super(id, codigoMesa, descripcion, reportadoPor);
        if (tipoReclamante == null) {
            throw new IllegalArgumentException("El tipo de reclamante es obligatorio");
        }
        this.tipoReclamante = tipoReclamante;
    }

    @Override
    public String getTipo() {
        return "RECLAMACION";
    }

    @Override
    public boolean requiereRevisionActa() {
        return true;
    }

    public TipoReclamante getTipoReclamante() {
        return tipoReclamante;
    }
}
