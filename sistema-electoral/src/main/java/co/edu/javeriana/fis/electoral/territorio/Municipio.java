package co.edu.javeriana.fis.electoral.territorio;

/** Municipio de un departamento; agrupa puestos de votación. */
public class Municipio extends UnidadTerritorial {

    public Municipio(String codigo, String nombre) {
        super(codigo, nombre);
    }

    @Override
    protected boolean aceptaHijo(ComponenteElectoral hijo) {
        return hijo instanceof PuestoVotacion;
    }
}
