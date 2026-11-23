package co.edu.javeriana.fis.electoral.estado;

import co.edu.javeriana.fis.electoral.modelo.ProcesoElectoral;

/** Jornada electoral en curso: las mesas reciben votos. */
public class EstadoEnVotacion implements EstadoProceso {

    @Override
    public String getNombre() {
        return "EN_VOTACION";
    }

    @Override
    public EstadoProceso cerrarVotacion(ProcesoElectoral proceso) {
        return new EstadoEscrutinio();
    }
}
