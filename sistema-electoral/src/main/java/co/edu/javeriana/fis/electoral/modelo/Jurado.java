package co.edu.javeriana.fis.electoral.modelo;

import java.util.Objects;

/** Ciudadano designado para instalar una mesa, atender votantes y registrar resultados. */
public class Jurado {

    private final String documento;
    private final String nombre;
    private String codigoMesaAsignada;

    public Jurado(String documento, String nombre) {
        if (documento == null || !documento.matches("\\d{6,10}")) {
            throw new IllegalArgumentException("El documento debe tener entre 6 y 10 dígitos");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del jurado es obligatorio");
        }
        this.documento = documento;
        this.nombre = nombre.trim();
    }

    /** Asigna el jurado a una mesa. Un jurado solo puede servir en una mesa por jornada. */
    public void asignarAMesa(String codigoMesa) {
        if (codigoMesa == null || codigoMesa.isBlank()) {
            throw new IllegalArgumentException("El código de la mesa es obligatorio");
        }
        if (codigoMesaAsignada != null) {
            throw new IllegalStateException("El jurado " + documento + " ya está asignado a la mesa "
                    + codigoMesaAsignada);
        }
        this.codigoMesaAsignada = codigoMesa;
    }

    public boolean estaAsignado() {
        return codigoMesaAsignada != null;
    }

    public String getCodigoMesaAsignada() {
        return codigoMesaAsignada;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Jurado)) return false;
        return documento.equals(((Jurado) o).documento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documento);
    }
}
