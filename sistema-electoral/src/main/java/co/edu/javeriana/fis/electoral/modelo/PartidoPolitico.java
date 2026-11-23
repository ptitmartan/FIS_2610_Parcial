package co.edu.javeriana.fis.electoral.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Organización política que respalda (avala) candidatos en la contienda. */
public class PartidoPolitico {

    private final String codigo;
    private final String nombre;
    private final List<Candidato> candidatosAvalados = new ArrayList<>();

    public PartidoPolitico(String codigo, String nombre) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del partido es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del partido es obligatorio");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
    }

    /**
     * Registra el aval del partido a un candidato. El candidato debe haber sido creado
     * bajo este mismo partido y no puede avalarse dos veces.
     */
    public void avalarCandidato(Candidato candidato) {
        if (candidato == null) {
            throw new IllegalArgumentException("El candidato no puede ser nulo");
        }
        if (!candidato.perteneceA(this)) {
            throw new IllegalArgumentException("El candidato " + candidato.getDocumento()
                    + " no pertenece al partido " + codigo);
        }
        if (candidatosAvalados.contains(candidato)) {
            throw new IllegalStateException("El candidato " + candidato.getDocumento() + " ya está avalado");
        }
        candidatosAvalados.add(candidato);
    }

    public int cantidadCandidatos() {
        return candidatosAvalados.size();
    }

    public List<Candidato> getCandidatosAvalados() {
        return Collections.unmodifiableList(candidatosAvalados);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartidoPolitico)) return false;
        return codigo.equals(((PartidoPolitico) o).codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}
