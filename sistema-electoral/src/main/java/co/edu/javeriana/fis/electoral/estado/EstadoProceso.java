package co.edu.javeriana.fis.electoral.estado;

import co.edu.javeriana.fis.electoral.modelo.ProcesoElectoral;

/**
 * Patrón <b>State</b> (comportamental): cada estado del calendario electoral decide qué
 * operaciones están permitidas y cuál es el siguiente estado. Así el {@link ProcesoElectoral}
 * no necesita cadenas de if/switch sobre su estado: delega en el objeto estado actual.
 *
 * <p>Ciclo: PROGRAMADO → EN_VOTACION → ESCRUTINIO → FINALIZADO.</p>
 */
public interface EstadoProceso {

    String getNombre();

    /** Inscripción de candidatos: solo antes de la jornada. */
    default boolean permiteInscripcionCandidatos() {
        return false;
    }

    /** Registro de actas de mesa: solo después del cierre de la jornada. */
    default boolean permiteRegistroActas() {
        return false;
    }

    default EstadoProceso abrirVotacion(ProcesoElectoral proceso) {
        throw transicionInvalida("abrir la votación");
    }

    default EstadoProceso cerrarVotacion(ProcesoElectoral proceso) {
        throw transicionInvalida("cerrar la votación");
    }

    default EstadoProceso publicarResultados(ProcesoElectoral proceso) {
        throw transicionInvalida("publicar resultados");
    }

    private IllegalStateException transicionInvalida(String accion) {
        return new IllegalStateException("No se puede " + accion + " en estado " + getNombre());
    }
}
