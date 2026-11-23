package co.edu.javeriana.fis.electoral.modelo;

import java.util.Objects;

/** Persona que participa en la contienda respaldada por un partido político. */
public class Candidato {

    private final String documento;
    private final String nombre;
    private final PartidoPolitico partido;

    public Candidato(String documento, String nombre, PartidoPolitico partido) {
        if (documento == null || !documento.matches("\\d{6,10}")) {
            throw new IllegalArgumentException("El documento debe tener entre 6 y 10 dígitos");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del candidato es obligatorio");
        }
        if (partido == null) {
            throw new IllegalArgumentException("El candidato debe estar respaldado por un partido");
        }
        this.documento = documento;
        this.nombre = nombre.trim();
        this.partido = partido;
    }

    /** Indica si el candidato está respaldado por el partido dado. */
    public boolean perteneceA(PartidoPolitico otroPartido) {
        return otroPartido != null && partido.equals(otroPartido);
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    public PartidoPolitico getPartido() {
        return partido;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Candidato)) return false;
        return documento.equals(((Candidato) o).documento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documento);
    }

    @Override
    public String toString() {
        return nombre + " - " + partido.getCodigo();
    }
}
