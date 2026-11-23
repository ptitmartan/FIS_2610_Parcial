package co.edu.javeriana.fis.electoral.estado;

import co.edu.javeriana.fis.electoral.modelo.ProcesoElectoral;

/** Cierre de la jornada: los jurados registran las actas de cada mesa. */
public class EstadoEscrutinio implements EstadoProceso {

    @Override
    public String getNombre() {
        return "ESCRUTINIO";
    }

    @Override
    public boolean permiteRegistroActas() {
        return true;
    }

    @Override
    public EstadoProceso publicarResultados(ProcesoElectoral proceso) {
        return new EstadoFinalizado();
    }
}
