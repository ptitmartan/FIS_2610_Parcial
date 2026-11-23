package co.edu.javeriana.fis.electoral.territorio;

import co.edu.javeriana.fis.electoral.modelo.ActaEscrutinio;
import co.edu.javeriana.fis.electoral.modelo.Jurado;
import co.edu.javeriana.fis.electoral.modelo.RolJurado;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hoja del Composite: mesa de votación con número, censo, jurados asignados y el acta
 * de escrutinio con sus resultados.
 */
public class MesaVotacion implements ComponenteElectoral {

    public static final int CENSO_MAXIMO = 1000;
    public static final int MAX_JURADOS = 6;

    private final String codigo;
    private final int numero;
    private final int censo;
    private final Map<Jurado, RolJurado> jurados = new LinkedHashMap<>();
    private ActaEscrutinio acta;

    public MesaVotacion(String codigo, int numero, int censo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la mesa es obligatorio");
        }
        if (numero < 1) {
            throw new IllegalArgumentException("El número de mesa debe ser mayor o igual a 1");
        }
        if (censo < 1 || censo > CENSO_MAXIMO) {
            throw new IllegalArgumentException("El censo de la mesa debe estar entre 1 y " + CENSO_MAXIMO);
        }
        this.codigo = codigo.trim();
        this.numero = numero;
        this.censo = censo;
    }

    /**
     * Asigna un jurado con su función. Máximo {@value #MAX_JURADOS} jurados por mesa,
     * un solo presidente y un solo vicepresidente.
     */
    public void asignarJurado(Jurado jurado, RolJurado rol) {
        if (jurado == null || rol == null) {
            throw new IllegalArgumentException("El jurado y su rol son obligatorios");
        }
        if (jurados.containsKey(jurado)) {
            throw new IllegalStateException("El jurado " + jurado.getDocumento() + " ya está en esta mesa");
        }
        if (jurados.size() >= MAX_JURADOS) {
            throw new IllegalStateException("La mesa ya tiene el máximo de " + MAX_JURADOS + " jurados");
        }
        if (rol != RolJurado.VOCAL && jurados.containsValue(rol)) {
            throw new IllegalStateException("La mesa ya tiene un " + rol);
        }
        jurado.asignarAMesa(codigo);
        jurados.put(jurado, rol);
    }

    /** Función del jurado en esta mesa, o null si no pertenece a ella. */
    public RolJurado getRolDe(Jurado jurado) {
        return jurados.get(jurado);
    }

    /**
     * Registra el acta del cierre de la jornada. El acta debe ser de esta mesa, estar firmada
     * por uno de sus jurados, no superar el censo y solo puede registrarse una vez.
     */
    public void registrarActa(ActaEscrutinio nuevaActa) {
        if (nuevaActa == null) {
            throw new IllegalArgumentException("El acta no puede ser nula");
        }
        if (!codigo.equals(nuevaActa.getCodigoMesa())) {
            throw new IllegalArgumentException("El acta corresponde a la mesa " + nuevaActa.getCodigoMesa()
                    + " y no a la mesa " + codigo);
        }
        if (acta != null) {
            throw new IllegalStateException("La mesa " + codigo + " ya tiene un acta registrada");
        }
        boolean firmadaPorJuradoDeLaMesa = jurados.keySet().stream()
                .anyMatch(j -> j.getDocumento().equals(nuevaActa.getDocumentoJuradoFirmante()));
        if (!firmadaPorJuradoDeLaMesa) {
            throw new IllegalStateException("El acta debe estar firmada por un jurado de la mesa");
        }
        if (nuevaActa.getTotalVotos() > censo) {
            throw new IllegalStateException("El acta reporta " + nuevaActa.getTotalVotos()
                    + " votos y el censo de la mesa es " + censo);
        }
        this.acta = nuevaActa;
    }

    public boolean tieneActa() {
        return acta != null;
    }

    @Override
    public int getTotalVotos() {
        return acta == null ? 0 : acta.getTotalVotos();
    }

    @Override
    public int getVotosCandidato(String documentoCandidato) {
        return acta == null ? 0 : acta.getVotosCandidato(documentoCandidato);
    }

    @Override
    public int contarMesas() {
        return 1;
    }

    @Override
    public int getCenso() {
        return censo;
    }

    @Override
    public String getCodigo() {
        return codigo;
    }

    @Override
    public String getNombre() {
        return "Mesa " + numero;
    }

    public int getNumero() {
        return numero;
    }

    public Map<Jurado, RolJurado> getJurados() {
        return Collections.unmodifiableMap(jurados);
    }

    public ActaEscrutinio getActa() {
        return acta;
    }
}
