package co.edu.javeriana.fis.electoral.territorio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nodo compuesto del Composite: guarda hijos y calcula sus totales sumando los de sus hijos.
 * Cada subclase decide qué tipo de hijo acepta (departamento → municipios, etc.).
 */
public abstract class UnidadTerritorial implements ComponenteElectoral {

    private final String codigo;
    private final String nombre;
    private final List<ComponenteElectoral> hijos = new ArrayList<>();

    protected UnidadTerritorial(String codigo, String nombre) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
    }

    /** Tipo de hijo permitido en este nivel de la jerarquía. */
    protected abstract boolean aceptaHijo(ComponenteElectoral hijo);

    public void agregar(ComponenteElectoral hijo) {
        if (hijo == null) {
            throw new IllegalArgumentException("El componente no puede ser nulo");
        }
        if (!aceptaHijo(hijo)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " no puede contener "
                    + hijo.getClass().getSimpleName());
        }
        if (buscarHijo(hijo.getCodigo()) != null) {
            throw new IllegalStateException("Ya existe un componente con código " + hijo.getCodigo());
        }
        hijos.add(hijo);
    }

    public boolean remover(String codigoHijo) {
        return hijos.removeIf(h -> h.getCodigo().equals(codigoHijo));
    }

    public ComponenteElectoral buscarHijo(String codigoHijo) {
        return hijos.stream().filter(h -> h.getCodigo().equals(codigoHijo)).findFirst().orElse(null);
    }

    public List<ComponenteElectoral> getHijos() {
        return Collections.unmodifiableList(hijos);
    }

    @Override
    public int getCenso() {
        return hijos.stream().mapToInt(ComponenteElectoral::getCenso).sum();
    }

    @Override
    public int getTotalVotos() {
        return hijos.stream().mapToInt(ComponenteElectoral::getTotalVotos).sum();
    }

    @Override
    public int getVotosCandidato(String documentoCandidato) {
        return hijos.stream().mapToInt(h -> h.getVotosCandidato(documentoCandidato)).sum();
    }

    @Override
    public int contarMesas() {
        return hijos.stream().mapToInt(ComponenteElectoral::contarMesas).sum();
    }

    @Override
    public String getCodigo() {
        return codigo;
    }

    @Override
    public String getNombre() {
        return nombre;
    }
}
