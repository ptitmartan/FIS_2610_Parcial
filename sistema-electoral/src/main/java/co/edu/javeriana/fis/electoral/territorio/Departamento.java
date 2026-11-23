package co.edu.javeriana.fis.electoral.territorio;

/** Departamento donde se desarrolla la votación; agrupa municipios. */
public class Departamento extends UnidadTerritorial {

    public Departamento(String codigo, String nombre) {
        super(codigo, nombre);
    }

    @Override
    protected boolean aceptaHijo(ComponenteElectoral hijo) {
        return hijo instanceof Municipio;
    }
}
