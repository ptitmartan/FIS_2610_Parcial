package co.edu.javeriana.fis.electoral.territorio;

/**
 * Patrón <b>Composite</b> (estructural): interfaz común para la mesa (hoja) y para las
 * unidades que agrupan mesas (puesto, municipio, departamento). Gracias a ella un analista
 * puede pedir censo, votos o participación a cualquier nivel del territorio con el mismo
 * código, sin importar si es una mesa o un departamento entero.
 */
public interface ComponenteElectoral {

    String getCodigo();

    String getNombre();

    int getCenso();

    int getTotalVotos();

    int getVotosCandidato(String documentoCandidato);

    int contarMesas();

    /** Participación = votos depositados / censo. 0 si el censo es 0 (unidad sin mesas). */
    default double getParticipacion() {
        int censo = getCenso();
        return censo == 0 ? 0.0 : (double) getTotalVotos() / censo;
    }
}
