package co.edu.javeriana.fis.electoral.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActaEscrutinio (Builder)")
class ActaEscrutinioTest {

    private static final String MESA = "11001-P1-M1";

    private ActaEscrutinio actaBase() {
        return ActaEscrutinio.builder(MESA)
                .votosCandidato("1000001", 120)
                .votosCandidato("1000002", 80)
                .votosEnBlanco(10)
                .votosNulos(5)
                .votosNoMarcados(3)
                .firmadaPor("52123456")
                .construir();
    }

    // ---- Builder.construir ----
    @Test
    @DisplayName("construir: arma un acta completa con todos sus datos")
    void construirValida() {
        ActaEscrutinio acta = actaBase();
        assertEquals(MESA, acta.getCodigoMesa());
        assertEquals(2, acta.getVotosPorCandidato().size());
        assertEquals(10, acta.getVotosEnBlanco());
        assertEquals("52123456", acta.getDocumentoJuradoFirmante());
        assertNotNull(acta.getFechaCierre());
    }

    @Test
    @DisplayName("construir (negativa): acta sin candidatos")
    void construirSinCandidatos() {
        assertThrows(IllegalStateException.class,
                () -> ActaEscrutinio.builder(MESA).firmadaPor("52123456").construir());
    }

    @Test
    @DisplayName("construir (negativa): votos negativos")
    void construirVotosNegativos() {
        assertThrows(IllegalStateException.class, () -> ActaEscrutinio.builder(MESA)
                .votosCandidato("1000001", -1).firmadaPor("52123456").construir());
        assertThrows(IllegalStateException.class, () -> ActaEscrutinio.builder(MESA)
                .votosCandidato("1000001", 5).votosNulos(-3).firmadaPor("52123456").construir());
    }

    @Test
    @DisplayName("construir (negativa): acta sin firma de jurado")
    void construirSinFirma() {
        assertThrows(IllegalStateException.class,
                () -> ActaEscrutinio.builder(MESA).votosCandidato("1000001", 5).construir());
    }

    @Test
    @DisplayName("construir (negativa): acta sin mesa o sin fecha")
    void construirSinMesaOFecha() {
        assertThrows(IllegalStateException.class, () -> ActaEscrutinio.builder(" ")
                .votosCandidato("1000001", 5).firmadaPor("52123456").construir());
        assertThrows(IllegalStateException.class, () -> ActaEscrutinio.builder(MESA)
                .votosCandidato("1000001", 5).firmadaPor("52123456").fechaCierre(null).construir());
    }

    @Test
    @DisplayName("votosCandidato (negativa): no permite consignar dos veces al mismo candidato")
    void votosCandidatoDuplicado() {
        ActaEscrutinio.Builder b = ActaEscrutinio.builder(MESA).votosCandidato("1000001", 5);
        assertThrows(IllegalArgumentException.class, () -> b.votosCandidato("1000001", 7));
        assertThrows(IllegalArgumentException.class, () -> b.votosCandidato(null, 7));
    }

    @Test
    @DisplayName("construir (límite): acta con todos los valores en cero es válida")
    void construirTodoEnCero() {
        ActaEscrutinio acta = ActaEscrutinio.builder(MESA).votosCandidato("1000001", 0)
                .firmadaPor("52123456").construir();
        assertEquals(0, acta.getTotalVotos());
    }

    // ---- getTotalVotos ----
    @Test
    @DisplayName("getTotalVotos: suma candidatos, blanco, nulos y no marcados")
    void totalVotos() {
        assertEquals(120 + 80 + 10 + 5 + 3, actaBase().getTotalVotos());
    }

    @Test
    @DisplayName("getTotalVotos (límite): valores grandes no se pierden")
    void totalVotosGrandes() {
        ActaEscrutinio acta = ActaEscrutinio.builder(MESA).votosCandidato("1000001", 1_000_000)
                .votosNulos(1).firmadaPor("52123456").construir();
        assertEquals(1_000_001, acta.getTotalVotos());
    }

    // ---- getVotosValidos ----
    @Test
    @DisplayName("getVotosValidos: candidatos + votos en blanco (sin nulos ni no marcados)")
    void votosValidos() {
        assertEquals(120 + 80 + 10, actaBase().getVotosValidos());
    }

    @Test
    @DisplayName("getVotosValidos (límite): si todo es nulo, los válidos son 0")
    void votosValidosSoloNulos() {
        ActaEscrutinio acta = ActaEscrutinio.builder(MESA).votosCandidato("1000001", 0)
                .votosNulos(50).firmadaPor("52123456").construir();
        assertEquals(0, acta.getVotosValidos());
        assertEquals(50, acta.getTotalVotos());
    }

    // ---- getVotosCandidato ----
    @Test
    @DisplayName("getVotosCandidato: devuelve los votos del candidato")
    void votosCandidato() {
        assertEquals(120, actaBase().getVotosCandidato("1000001"));
    }

    @Test
    @DisplayName("getVotosCandidato (límite): candidato que no está en el acta tiene 0")
    void votosCandidatoAusente() {
        assertEquals(0, actaBase().getVotosCandidato("9999999"));
    }

    @Test
    @DisplayName("getVotosCandidato (negativa): documento nulo")
    void votosCandidatoNulo() {
        ActaEscrutinio acta = actaBase();
        assertThrows(IllegalArgumentException.class, () -> acta.getVotosCandidato(null));
    }

    // ---- getCandidatoGanador ----
    @Test
    @DisplayName("getCandidatoGanador: el de más votos")
    void ganador() {
        assertEquals(Optional.of("1000001"), actaBase().getCandidatoGanador());
    }

    @Test
    @DisplayName("getCandidatoGanador (límite): empate en primer lugar no tiene ganador")
    void ganadorEmpate() {
        ActaEscrutinio acta = ActaEscrutinio.builder(MESA).votosCandidato("1000001", 50)
                .votosCandidato("1000002", 50).votosCandidato("1000003", 10).firmadaPor("52123456").construir();
        assertTrue(acta.getCandidatoGanador().isEmpty());
    }

    @Test
    @DisplayName("getCandidatoGanador (límite): si nadie tiene votos no hay ganador")
    void ganadorSinVotos() {
        ActaEscrutinio acta = ActaEscrutinio.builder(MESA).votosCandidato("1000001", 0)
                .votosEnBlanco(30).firmadaPor("52123456").construir();
        assertTrue(acta.getCandidatoGanador().isEmpty());
    }

    // ---- inmutabilidad ----
    @Test
    @DisplayName("getVotosPorCandidato (negativa): el acta es inmutable")
    void actaInmutable() {
        ActaEscrutinio acta = actaBase();
        assertThrows(UnsupportedOperationException.class, () -> acta.getVotosPorCandidato().put("1000001", 999));
    }
}
