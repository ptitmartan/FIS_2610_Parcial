package co.edu.javeriana.fis.electoral.modelo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Acta que elaboran los jurados al cierre de la jornada con los resultados de una mesa.
 *
 * <p>Patrón <b>Builder</b> (creacional): el acta tiene muchos datos (votos por cada candidato,
 * votos en blanco, nulos, no marcados, jurado que la firma) que se van consignando paso a paso.
 * El {@link Builder} permite armarla de forma legible y valida todo al final en
 * {@link Builder#construir()}, de modo que nunca exista un acta a medio llenar o inconsistente.
 * El acta resultante es inmutable, lo que favorece la trazabilidad.</p>
 */
public final class ActaEscrutinio {

    private final String codigoMesa;
    private final Map<String, Integer> votosPorCandidato;
    private final int votosEnBlanco;
    private final int votosNulos;
    private final int votosNoMarcados;
    private final String documentoJuradoFirmante;
    private final LocalDateTime fechaCierre;

    private ActaEscrutinio(Builder b) {
        this.codigoMesa = b.codigoMesa;
        this.votosPorCandidato = Collections.unmodifiableMap(new LinkedHashMap<>(b.votosPorCandidato));
        this.votosEnBlanco = b.votosEnBlanco;
        this.votosNulos = b.votosNulos;
        this.votosNoMarcados = b.votosNoMarcados;
        this.documentoJuradoFirmante = b.documentoJuradoFirmante;
        this.fechaCierre = b.fechaCierre;
    }

    public static Builder builder(String codigoMesa) {
        return new Builder(codigoMesa);
    }

    /** Total de tarjetones depositados: candidatos + blanco + nulos + no marcados. */
    public int getTotalVotos() {
        return getVotosCandidatos() + votosEnBlanco + votosNulos + votosNoMarcados;
    }

    /** Votos válidos: los marcados por candidatos más los votos en blanco. */
    public int getVotosValidos() {
        return getVotosCandidatos() + votosEnBlanco;
    }

    /** Votos de un candidato en esta mesa; 0 si el candidato no aparece en el acta. */
    public int getVotosCandidato(String documentoCandidato) {
        if (documentoCandidato == null) {
            throw new IllegalArgumentException("El documento del candidato es obligatorio");
        }
        return votosPorCandidato.getOrDefault(documentoCandidato, 0);
    }

    /** Candidato con más votos en la mesa. Vacío si hay empate en el primer lugar o nadie tiene votos. */
    public Optional<String> getCandidatoGanador() {
        int maximo = votosPorCandidato.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (maximo == 0) {
            return Optional.empty();
        }
        var empatados = votosPorCandidato.entrySet().stream()
                .filter(e -> e.getValue() == maximo)
                .map(Map.Entry::getKey)
                .toList();
        return empatados.size() == 1 ? Optional.of(empatados.get(0)) : Optional.empty();
    }

    private int getVotosCandidatos() {
        return votosPorCandidato.values().stream().mapToInt(Integer::intValue).sum();
    }

    public String getCodigoMesa() {
        return codigoMesa;
    }

    public Map<String, Integer> getVotosPorCandidato() {
        return votosPorCandidato;
    }

    public int getVotosEnBlanco() {
        return votosEnBlanco;
    }

    public int getVotosNulos() {
        return votosNulos;
    }

    public int getVotosNoMarcados() {
        return votosNoMarcados;
    }

    public String getDocumentoJuradoFirmante() {
        return documentoJuradoFirmante;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    /** Builder del acta: acumula los datos y valida en {@link #construir()}. */
    public static final class Builder {

        private final String codigoMesa;
        private final Map<String, Integer> votosPorCandidato = new LinkedHashMap<>();
        private int votosEnBlanco;
        private int votosNulos;
        private int votosNoMarcados;
        private String documentoJuradoFirmante;
        private LocalDateTime fechaCierre = LocalDateTime.now();

        private Builder(String codigoMesa) {
            this.codigoMesa = codigoMesa;
        }

        public Builder votosCandidato(String documentoCandidato, int votos) {
            if (documentoCandidato == null || documentoCandidato.isBlank()) {
                throw new IllegalArgumentException("El documento del candidato es obligatorio");
            }
            if (votosPorCandidato.containsKey(documentoCandidato)) {
                throw new IllegalArgumentException("El candidato " + documentoCandidato
                        + " ya tiene votos consignados en el acta");
            }
            votosPorCandidato.put(documentoCandidato, votos);
            return this;
        }

        public Builder votosEnBlanco(int votos) {
            this.votosEnBlanco = votos;
            return this;
        }

        public Builder votosNulos(int votos) {
            this.votosNulos = votos;
            return this;
        }

        public Builder votosNoMarcados(int votos) {
            this.votosNoMarcados = votos;
            return this;
        }

        public Builder firmadaPor(String documentoJurado) {
            this.documentoJuradoFirmante = documentoJurado;
            return this;
        }

        public Builder fechaCierre(LocalDateTime fecha) {
            this.fechaCierre = fecha;
            return this;
        }

        /** Valida la información consignada y crea el acta inmutable. */
        public ActaEscrutinio construir() {
            if (codigoMesa == null || codigoMesa.isBlank()) {
                throw new IllegalStateException("El acta debe indicar la mesa");
            }
            if (votosPorCandidato.isEmpty()) {
                throw new IllegalStateException("El acta debe consignar votos de al menos un candidato");
            }
            if (votosPorCandidato.values().stream().anyMatch(v -> v < 0)
                    || votosEnBlanco < 0 || votosNulos < 0 || votosNoMarcados < 0) {
                throw new IllegalStateException("Ninguna cantidad de votos puede ser negativa");
            }
            if (documentoJuradoFirmante == null || documentoJuradoFirmante.isBlank()) {
                throw new IllegalStateException("El acta debe estar firmada por un jurado");
            }
            if (fechaCierre == null) {
                throw new IllegalStateException("El acta debe tener fecha de cierre");
            }
            return new ActaEscrutinio(this);
        }
    }
}
