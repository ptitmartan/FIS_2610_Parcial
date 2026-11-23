package co.edu.javeriana.fis.electoral.territorio;

/** Lugar habilitado para votar (colegio, coliseo...). Agrupa mesas de votación. */
public class PuestoVotacion extends UnidadTerritorial {

    private final String direccion;

    public PuestoVotacion(String codigo, String nombre, String direccion) {
        super(codigo, nombre);
        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("La dirección del puesto es obligatoria");
        }
        this.direccion = direccion.trim();
    }

    @Override
    protected boolean aceptaHijo(ComponenteElectoral hijo) {
        return hijo instanceof MesaVotacion;
    }

    /** Siguiente número de mesa libre dentro del puesto (1 si aún no tiene mesas). */
    public int siguienteNumeroMesa() {
        return getHijos().stream()
                .mapToInt(h -> ((MesaVotacion) h).getNumero())
                .max().orElse(0) + 1;
    }

    public String getDireccion() {
        return direccion;
    }
}
