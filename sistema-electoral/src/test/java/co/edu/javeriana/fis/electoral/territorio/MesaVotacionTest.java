package co.edu.javeriana.fis.electoral.territorio;

import co.edu.javeriana.fis.electoral.modelo.ActaEscrutinio;
import co.edu.javeriana.fis.electoral.modelo.Jurado;
import co.edu.javeriana.fis.electoral.modelo.RolJurado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MesaVotacion (hoja del Composite)")
class MesaVotacionTest {

    private static final String CODIGO = "11001-P1-M1";
    private MesaVotacion mesa;
    private Jurado presidente;

    @BeforeEach
    void setUp() {
        mesa = new MesaVotacion(CODIGO, 1, 300);
        presidente = new Jurado("52123456", "Laura Pérez");
    }

    private ActaEscrutinio acta(int votosA, int votosB, String firmante) {
        return ActaEscrutinio.builder(CODIGO).votosCandidato("1000001", votosA)
                .votosCandidato("1000002", votosB).firmadaPor(firmante).construir();
    }

    // ---- constructor ----
    @Test
    @DisplayName("constructor: crea la mesa con número y censo")
    void constructorValido() {
        assertEquals(1, mesa.getNumero());
        assertEquals(300, mesa.getCenso());
        assertEquals("Mesa 1", mesa.getNombre());
    }

    @Test
    @DisplayName("constructor (límite): censo 1 y censo máximo son válidos")
    void constructorLimitesCenso() {
        assertDoesNotThrow(() -> new MesaVotacion("M-A", 1, 1));
        assertDoesNotThrow(() -> new MesaVotacion("M-B", 1, MesaVotacion.CENSO_MAXIMO));
    }

    @Test
    @DisplayName("constructor (negativa): censo 0, censo > máximo o número 0")
    void constructorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new MesaVotacion("M", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new MesaVotacion("M", 1, MesaVotacion.CENSO_MAXIMO + 1));
        assertThrows(IllegalArgumentException.class, () -> new MesaVotacion("M", 0, 100));
        assertThrows(IllegalArgumentException.class, () -> new MesaVotacion(" ", 1, 100));
    }

    // ---- asignarJurado ----
    @Test
    @DisplayName("asignarJurado: registra el jurado con su rol y le asigna la mesa")
    void asignarJuradoValido() {
        mesa.asignarJurado(presidente, RolJurado.PRESIDENTE);
        assertEquals(RolJurado.PRESIDENTE, mesa.getRolDe(presidente));
        assertEquals(CODIGO, presidente.getCodigoMesaAsignada());
    }

    @Test
    @DisplayName("asignarJurado (negativa): jurado o rol nulos")
    void asignarJuradoNulo() {
        assertThrows(IllegalArgumentException.class, () -> mesa.asignarJurado(null, RolJurado.VOCAL));
        assertThrows(IllegalArgumentException.class, () -> mesa.asignarJurado(presidente, null));
    }

    @Test
    @DisplayName("asignarJurado (negativa): dos presidentes en la misma mesa")
    void asignarDosPresidentes() {
        mesa.asignarJurado(presidente, RolJurado.PRESIDENTE);
        Jurado otro = new Jurado("52999999", "Otro");
        assertThrows(IllegalStateException.class, () -> mesa.asignarJurado(otro, RolJurado.PRESIDENTE));
        assertFalse(otro.estaAsignado());
    }

    @Test
    @DisplayName("asignarJurado (negativa): el mismo jurado dos veces")
    void asignarJuradoRepetido() {
        mesa.asignarJurado(presidente, RolJurado.VOCAL);
        assertThrows(IllegalStateException.class, () -> mesa.asignarJurado(presidente, RolJurado.VOCAL));
    }

    @Test
    @DisplayName("asignarJurado (negativa): jurado que ya sirve en otra mesa")
    void asignarJuradoDeOtraMesa() {
        MesaVotacion otraMesa = new MesaVotacion("11001-P1-M2", 2, 300);
        otraMesa.asignarJurado(presidente, RolJurado.PRESIDENTE);
        assertThrows(IllegalStateException.class, () -> mesa.asignarJurado(presidente, RolJurado.PRESIDENTE));
        assertNull(mesa.getRolDe(presidente));
    }

    @Test
    @DisplayName("asignarJurado (límite): acepta exactamente 6 jurados y rechaza el séptimo")
    void asignarMaximoJurados() {
        mesa.asignarJurado(presidente, RolJurado.PRESIDENTE);
        mesa.asignarJurado(new Jurado("1000002", "J2"), RolJurado.VICEPRESIDENTE);
        for (int i = 3; i <= MesaVotacion.MAX_JURADOS; i++) {
            mesa.asignarJurado(new Jurado("100000" + i, "J" + i), RolJurado.VOCAL);
        }
        assertEquals(MesaVotacion.MAX_JURADOS, mesa.getJurados().size());
        assertThrows(IllegalStateException.class, () -> mesa.asignarJurado(new Jurado("1000007", "J7"), RolJurado.VOCAL));
    }

    // ---- getRolDe ----
    @Test
    @DisplayName("getRolDe: devuelve el rol de un jurado de la mesa")
    void getRolDe() {
        Jurado vocal = new Jurado("1000003", "Vocal");
        mesa.asignarJurado(vocal, RolJurado.VOCAL);
        assertEquals(RolJurado.VOCAL, mesa.getRolDe(vocal));
    }

    @Test
    @DisplayName("getRolDe (límite): jurado que no pertenece a la mesa devuelve null")
    void getRolDeAjeno() {
        assertNull(mesa.getRolDe(presidente));
    }

    // ---- registrarActa ----
    @Test
    @DisplayName("registrarActa: guarda el acta firmada por un jurado de la mesa")
    void registrarActaValida() {
        mesa.asignarJurado(presidente, RolJurado.PRESIDENTE);
        mesa.registrarActa(acta(150, 100, "52123456"));
        assertTrue(mesa.tieneActa());
        assertEquals(250, mesa.getTotalVotos());
    }

    @Test
    @DisplayName("registrarActa (negativa): acta nula o de otra mesa")
    void registrarActaNulaODeOtraMesa() {
        assertThrows(IllegalArgumentException.class, () -> mesa.registrarActa(null));
        ActaEscrutinio ajena = ActaEscrutinio.builder("OTRA").votosCandidato("1000001", 1)
                .firmadaPor("52123456").construir();
        assertThrows(IllegalArgumentException.class, () -> mesa.registrarActa(ajena));
    }

    @Test
    @DisplayName("registrarActa (negativa): firmada por alguien que no es jurado de la mesa")
    void registrarActaFirmanteAjeno() {
        mesa.asignarJurado(presidente, RolJurado.PRESIDENTE);
        assertThrows(IllegalStateException.class, () -> mesa.registrarActa(acta(10, 10, "79000000")));
        assertFalse(mesa.tieneActa());
    }

    @Test
    @DisplayName("registrarActa (negativa): no se puede registrar dos actas")
    void registrarActaDosVeces() {
        mesa.asignarJurado(presidente, RolJurado.PRESIDENTE);
        mesa.registrarActa(acta(10, 10, "52123456"));
        assertThrows(IllegalStateException.class, () -> mesa.registrarActa(acta(20, 20, "52123456")));
        assertEquals(20, mesa.getTotalVotos());
    }

    @Test
    @DisplayName("registrarActa (límite): votos = censo se acepta; censo + 1 se rechaza")
    void registrarActaLimiteCenso() {
        mesa.asignarJurado(presidente, RolJurado.PRESIDENTE);
        assertThrows(IllegalStateException.class, () -> mesa.registrarActa(acta(200, 101, "52123456")));
        mesa.registrarActa(acta(200, 100, "52123456"));
        assertEquals(300, mesa.getTotalVotos());
    }

    // ---- tieneActa / getTotalVotos / getVotosCandidato ----
    @Test
    @DisplayName("tieneActa / getTotalVotos (límite): mesa sin acta tiene 0 votos")
    void sinActa() {
        assertFalse(mesa.tieneActa());
        assertEquals(0, mesa.getTotalVotos());
        assertNull(mesa.getActa());
    }

    @Test
    @DisplayName("getVotosCandidato: lee los votos desde el acta")
    void votosCandidatoConActa() {
        mesa.asignarJurado(presidente, RolJurado.PRESIDENTE);
        mesa.registrarActa(acta(150, 100, "52123456"));
        assertEquals(150, mesa.getVotosCandidato("1000001"));
        assertEquals(0, mesa.getVotosCandidato("9999999"));
    }

    @Test
    @DisplayName("getVotosCandidato (límite): sin acta devuelve 0")
    void votosCandidatoSinActa() {
        assertEquals(0, mesa.getVotosCandidato("1000001"));
    }

    // ---- getParticipacion / contarMesas ----
    @Test
    @DisplayName("getParticipacion: votos / censo")
    void participacion() {
        mesa.asignarJurado(presidente, RolJurado.PRESIDENTE);
        mesa.registrarActa(acta(100, 50, "52123456"));
        assertEquals(0.5, mesa.getParticipacion(), 0.0001);
    }

    @Test
    @DisplayName("getParticipacion (límite): 0 sin acta y 1.0 con participación total")
    void participacionLimites() {
        assertEquals(0.0, mesa.getParticipacion(), 0.0001);
        mesa.asignarJurado(presidente, RolJurado.PRESIDENTE);
        mesa.registrarActa(acta(300, 0, "52123456"));
        assertEquals(1.0, mesa.getParticipacion(), 0.0001);
    }

    @Test
    @DisplayName("contarMesas: una mesa cuenta como 1")
    void contarMesas() {
        assertEquals(1, mesa.contarMesas());
    }

    @Test
    @DisplayName("getJurados (negativa): el mapa de jurados no se puede modificar desde fuera")
    void juradosInmodificable() {
        assertThrows(UnsupportedOperationException.class, () -> mesa.getJurados().put(presidente, RolJurado.VOCAL));
    }
}
