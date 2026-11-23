package co.edu.javeriana.fis.electoral.modelo;

import co.edu.javeriana.fis.electoral.estado.EstadoProceso;
import co.edu.javeriana.fis.electoral.estado.EstadoProgramado;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Proceso electoral registrado por la autoridad (tipo, fecha y estado en el calendario).
 * Es el <i>contexto</i> del patrón State: delega en {@link EstadoProceso} las reglas de cada etapa.
 */
public class ProcesoElectoral {

    private final String codigo;
    private final TipoEleccion tipo;
    private final LocalDate fecha;
    private final List<Candidato> candidatos = new ArrayList<>();
    private EstadoProceso estado;

    public ProcesoElectoral(String codigo, TipoEleccion tipo, LocalDate fecha) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del proceso es obligatorio");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de elección es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la elección es obligatoria");
        }
        this.codigo = codigo.trim();
        this.tipo = tipo;
        this.fecha = fecha;
        this.estado = new EstadoProgramado();
    }

    /** Inscribe un candidato en la contienda. Solo se permite mientras el proceso está PROGRAMADO. */
    public void inscribirCandidato(Candidato candidato) {
        if (candidato == null) {
            throw new IllegalArgumentException("El candidato no puede ser nulo");
        }
        if (!estado.permiteInscripcionCandidatos()) {
            throw new IllegalStateException("No se pueden inscribir candidatos en estado " + estado.getNombre());
        }
        if (candidatos.contains(candidato)) {
            throw new IllegalStateException("El candidato " + candidato.getDocumento() + " ya está inscrito");
        }
        candidatos.add(candidato);
    }

    public boolean estaInscrito(String documentoCandidato) {
        return candidatos.stream().anyMatch(c -> c.getDocumento().equals(documentoCandidato));
    }

    public void abrirVotacion() {
        estado = estado.abrirVotacion(this);
    }

    public void cerrarVotacion() {
        estado = estado.cerrarVotacion(this);
    }

    public void publicarResultados() {
        estado = estado.publicarResultados(this);
    }

    public boolean permiteRegistroActas() {
        return estado.permiteRegistroActas();
    }

    public String getNombreEstado() {
        return estado.getNombre();
    }

    public List<Candidato> getCandidatos() {
        return Collections.unmodifiableList(candidatos);
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoEleccion getTipo() {
        return tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
