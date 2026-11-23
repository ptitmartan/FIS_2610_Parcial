package co.edu.javeriana.fis.electoral.modelo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProcesoElectoral (contexto del State)")
class ProcesoElectoralTest {

    private ProcesoElectoral proceso;
    private Candidato candidato;

    @BeforeEach
    void setUp() {
        proceso = new ProcesoElectoral("PRES-2026", TipoEleccion.PRESIDENCIAL, LocalDate.of(2026, 5, 31));
        PartidoPolitico partido = new PartidoPolitico("PL", "Partido Liberal");
        candidato = new Candidato("1012345678", "Ana Gómez", partido);
    }

    private void llevarAEscrutinio() {
        proceso.inscribirCandidato(candidato);
        proceso.abrirVotacion();
        proceso.cerrarVotacion();
    }

    // ---- constructor ----
    @Test
    @DisplayName("constructor: inicia en estado PROGRAMADO y sin candidatos")
    void constructorValido() {
        assertEquals("PROGRAMADO", proceso.getNombreEstado());
        assertEquals(TipoEleccion.PRESIDENCIAL, proceso.getTipo());
        assertTrue(proceso.getCandidatos().isEmpty());
    }

    @Test
    @DisplayName("constructor (negativa): tipo, fecha o código faltantes")
    void constructorInvalido() {
        LocalDate f = LocalDate.of(2026, 3, 8);
        assertThrows(IllegalArgumentException.class, () -> new ProcesoElectoral("X", null, f));
        assertThrows(IllegalArgumentException.class, () -> new ProcesoElectoral("X", TipoEleccion.SENADO, null));
        assertThrows(IllegalArgumentException.class, () -> new ProcesoElectoral("", TipoEleccion.SENADO, f));
    }

    // ---- inscribirCandidato ----
    @Test
    @DisplayName("inscribirCandidato: agrega el candidato en estado PROGRAMADO")
    void inscribirValido() {
        proceso.inscribirCandidato(candidato);
        assertEquals(1, proceso.getCandidatos().size());
    }

    @Test
    @DisplayName("inscribirCandidato (negativa): candidato nulo o repetido")
    void inscribirNuloODuplicado() {
        assertThrows(IllegalArgumentException.class, () -> proceso.inscribirCandidato(null));
        proceso.inscribirCandidato(candidato);
        assertThrows(IllegalStateException.class, () -> proceso.inscribirCandidato(candidato));
    }

    @Test
    @DisplayName("inscribirCandidato (negativa): no se permite una vez abierta la votación")
    void inscribirDespuesDeAbrir() {
        proceso.inscribirCandidato(candidato);
        proceso.abrirVotacion();
        Candidato tardio = new Candidato("1099999999", "Tardío", candidato.getPartido());
        assertThrows(IllegalStateException.class, () -> proceso.inscribirCandidato(tardio));
    }

    // ---- estaInscrito ----
    @Test
    @DisplayName("estaInscrito: true para un candidato inscrito")
    void estaInscritoTrue() {
        proceso.inscribirCandidato(candidato);
        assertTrue(proceso.estaInscrito("1012345678"));
    }

    @Test
    @DisplayName("estaInscrito (límite): false con lista vacía o documento nulo")
    void estaInscritoFalse() {
        assertFalse(proceso.estaInscrito("1012345678"));
        assertFalse(proceso.estaInscrito(null));
    }

    // ---- abrirVotacion ----
    @Test
    @DisplayName("abrirVotacion: PROGRAMADO → EN_VOTACION")
    void abrirVotacion() {
        proceso.inscribirCandidato(candidato);
        proceso.abrirVotacion();
        assertEquals("EN_VOTACION", proceso.getNombreEstado());
    }

    @Test
    @DisplayName("abrirVotacion (límite): sin candidatos inscritos no se puede abrir")
    void abrirSinCandidatos() {
        assertThrows(IllegalStateException.class, proceso::abrirVotacion);
        assertEquals("PROGRAMADO", proceso.getNombreEstado());
    }

    @Test
    @DisplayName("abrirVotacion (negativa): no se puede abrir dos veces")
    void abrirDosVeces() {
        proceso.inscribirCandidato(candidato);
        proceso.abrirVotacion();
        assertThrows(IllegalStateException.class, proceso::abrirVotacion);
    }

    // ---- cerrarVotacion ----
    @Test
    @DisplayName("cerrarVotacion: EN_VOTACION → ESCRUTINIO")
    void cerrarVotacion() {
        llevarAEscrutinio();
        assertEquals("ESCRUTINIO", proceso.getNombreEstado());
    }

    @Test
    @DisplayName("cerrarVotacion (negativa): no se puede cerrar si nunca se abrió")
    void cerrarSinAbrir() {
        assertThrows(IllegalStateException.class, proceso::cerrarVotacion);
    }

    // ---- publicarResultados ----
    @Test
    @DisplayName("publicarResultados: ESCRUTINIO → FINALIZADO")
    void publicarResultados() {
        llevarAEscrutinio();
        proceso.publicarResultados();
        assertEquals("FINALIZADO", proceso.getNombreEstado());
    }

    @Test
    @DisplayName("publicarResultados (negativa): no se puede publicar durante la votación")
    void publicarDuranteVotacion() {
        proceso.inscribirCandidato(candidato);
        proceso.abrirVotacion();
        assertThrows(IllegalStateException.class, proceso::publicarResultados);
    }

    @Test
    @DisplayName("publicarResultados (límite): en FINALIZADO ninguna transición es posible")
    void finalizadoEsTerminal() {
        llevarAEscrutinio();
        proceso.publicarResultados();
        assertThrows(IllegalStateException.class, proceso::abrirVotacion);
        assertThrows(IllegalStateException.class, proceso::cerrarVotacion);
        assertThrows(IllegalStateException.class, proceso::publicarResultados);
    }

    // ---- permiteRegistroActas ----
    @Test
    @DisplayName("permiteRegistroActas: solo en ESCRUTINIO")
    void permiteActasEnEscrutinio() {
        llevarAEscrutinio();
        assertTrue(proceso.permiteRegistroActas());
    }

    @Test
    @DisplayName("permiteRegistroActas (límite): false antes y después del escrutinio")
    void noPermiteActasFueraDeEscrutinio() {
        assertFalse(proceso.permiteRegistroActas());
        llevarAEscrutinio();
        proceso.publicarResultados();
        assertFalse(proceso.permiteRegistroActas());
    }

    // ---- getCandidatos ----
    @Test
    @DisplayName("getCandidatos (negativa): la lista no se puede modificar desde fuera")
    void candidatosInmodificable() {
        assertThrows(UnsupportedOperationException.class, () -> proceso.getCandidatos().add(candidato));
    }
}
