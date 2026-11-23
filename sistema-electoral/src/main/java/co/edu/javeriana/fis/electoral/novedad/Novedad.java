package co.edu.javeriana.fis.electoral.novedad;

import java.time.LocalDateTime;

/** Situación registrada durante la jornada (incidencia o reclamación) sobre una mesa. */
public abstract class Novedad {

    public static final int MIN_DESCRIPCION = 10;
    public static final int MAX_DESCRIPCION = 500;

    private final String id;
    private final String codigoMesa;
    private final String descripcion;
    private final String reportadoPor;
    private final LocalDateTime fechaRegistro;
    private String respuesta;

    protected Novedad(String id, String codigoMesa, String descripcion, String reportadoPor) {
        if (id == null || id.isBlank() || codigoMesa == null || codigoMesa.isBlank()) {
            throw new IllegalArgumentException("La novedad debe tener id y mesa");
        }
        if (descripcion == null || descripcion.trim().length() < MIN_DESCRIPCION
                || descripcion.trim().length() > MAX_DESCRIPCION) {
            throw new IllegalArgumentException("La descripción debe tener entre " + MIN_DESCRIPCION
                    + " y " + MAX_DESCRIPCION + " caracteres");
        }
        if (reportadoPor == null || reportadoPor.isBlank()) {
            throw new IllegalArgumentException("Debe indicarse quién reporta la novedad");
        }
        this.id = id;
        this.codigoMesa = codigoMesa;
        this.descripcion = descripcion.trim();
        this.reportadoPor = reportadoPor.trim();
        this.fechaRegistro = LocalDateTime.now();
    }

    /** Cierra la novedad con la respuesta de la autoridad. Una novedad solo se resuelve una vez. */
    public void resolver(String respuestaAutoridad) {
        if (respuestaAutoridad == null || respuestaAutoridad.isBlank()) {
            throw new IllegalArgumentException("La respuesta es obligatoria para resolver la novedad");
        }
        if (!estaAbierta()) {
            throw new IllegalStateException("La novedad " + id + " ya fue resuelta");
        }
        this.respuesta = respuestaAutoridad.trim();
    }

    public boolean estaAbierta() {
        return respuesta == null;
    }

    public abstract String getTipo();

    /** Indica si la novedad obliga a revisar el acta de la mesa. */
    public abstract boolean requiereRevisionActa();

    public String getId() {
        return id;
    }

    public String getCodigoMesa() {
        return codigoMesa;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getReportadoPor() {
        return reportadoPor;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public String getRespuesta() {
        return respuesta;
    }
}
