package co.edu.javeriana.fis.electoral.novedad;

import co.edu.javeriana.fis.electoral.territorio.MesaVotacion;

/** Creador concreto: fabrica reclamaciones. Solo se puede reclamar sobre un acta ya registrada. */
public class RegistradorReclamacion extends RegistradorNovedad {

    private final TipoReclamante tipoReclamante;

    public RegistradorReclamacion(TipoReclamante tipoReclamante) {
        if (tipoReclamante == null) {
            throw new IllegalArgumentException("El tipo de reclamante es obligatorio");
        }
        this.tipoReclamante = tipoReclamante;
    }

    @Override
    protected void validarContexto(MesaVotacion mesa) {
        if (!mesa.tieneActa()) {
            throw new IllegalStateException("No se puede reclamar sobre la mesa " + mesa.getCodigo()
                    + " porque aún no tiene acta");
        }
    }

    @Override
    protected Novedad crearNovedad(String id, String codigoMesa, String descripcion, String reportadoPor) {
        return new Reclamacion(id, codigoMesa, descripcion, reportadoPor, tipoReclamante);
    }

    @Override
    protected String prefijo() {
        return "REC-" + tipoReclamante;
    }
}
