package co.edu.javeriana.fis.electoral.estado;

/** Resultados publicados: el proceso queda cerrado y solo se consulta. */
public class EstadoFinalizado implements EstadoProceso {

    @Override
    public String getNombre() {
        return "FINALIZADO";
    }
}
