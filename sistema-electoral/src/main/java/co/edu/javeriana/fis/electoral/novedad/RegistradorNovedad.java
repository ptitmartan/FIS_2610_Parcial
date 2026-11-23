package co.edu.javeriana.fis.electoral.novedad;

import co.edu.javeriana.fis.electoral.territorio.MesaVotacion;

/**
 * Patrón <b>Factory Method</b> (creacional). El algoritmo para registrar una novedad es
 * siempre el mismo (validar la mesa, validar el contexto, generar un consecutivo y crear el
 * objeto), pero <i>qué</i> novedad se crea lo deciden las subclases a través del método
 * fábrica {@link #crearNovedad}. Agregar un nuevo tipo de novedad (por ejemplo, una
 * denuncia de compra de votos) solo requiere una nueva subclase, sin tocar este código.
 */
public abstract class RegistradorNovedad {

    private int consecutivo;

    /** Método plantilla que usa el método fábrica. */
    public final Novedad registrar(MesaVotacion mesa, String descripcion, String reportadoPor) {
        if (mesa == null) {
            throw new IllegalArgumentException("La mesa es obligatoria");
        }
        validarContexto(mesa);
        String id = prefijo() + "-" + (consecutivo + 1);
        Novedad novedad = crearNovedad(id, mesa.getCodigo(), descripcion, reportadoPor);
        consecutivo++;
        return novedad;
    }

    /** Método fábrica: cada subclase crea su tipo concreto de novedad. */
    protected abstract Novedad crearNovedad(String id, String codigoMesa, String descripcion, String reportadoPor);

    protected abstract String prefijo();

    /** Gancho opcional para reglas propias de cada tipo de novedad. */
    protected void validarContexto(MesaVotacion mesa) {
        // Por defecto no hay reglas adicionales.
    }

    public int getCantidadRegistradas() {
        return consecutivo;
    }
}
