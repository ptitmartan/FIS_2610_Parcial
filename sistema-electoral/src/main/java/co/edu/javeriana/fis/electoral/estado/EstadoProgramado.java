package co.edu.javeriana.fis.electoral.estado;

import co.edu.javeriana.fis.electoral.modelo.ProcesoElectoral;

/** Proceso registrado en el calendario: se pueden inscribir candidatos. */
public class EstadoProgramado implements EstadoProceso {

    @Override
    public String getNombre() {
        return "PROGRAMADO";
    }

    @Override
    public boolean permiteInscripcionCandidatos() {
        return true;
    }

    @Override
    public EstadoProceso abrirVotacion(ProcesoElectoral proceso) {
        if (proceso.getCandidatos().isEmpty()) {
            throw new IllegalStateException("No se puede abrir la votación sin candidatos inscritos");
        }
        return new EstadoEnVotacion();
    }
}
