package co.edu.javeriana.fis.electoral.novedad;

import co.edu.javeriana.fis.electoral.modelo.ActaEscrutinio;
import co.edu.javeriana.fis.electoral.modelo.Jurado;
import co.edu.javeriana.fis.electoral.modelo.RolJurado;
import co.edu.javeriana.fis.electoral.territorio.MesaVotacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegistradorNovedad (Factory Method)")
class RegistradorNovedadTest {

    private static final String DESCRIPCION = "Corte de energía en el puesto de votación";
    private MesaVotacion mesa;

    @BeforeEach
    void setUp() {
        mesa = new MesaVotacion("11001-P1-M1", 1, 300);
    }

    private void registrarActaEnMesa() {
        mesa.asignarJurado(new Jurado("52123456", "Laura"), RolJurado.PRESIDENTE);
        mesa.registrarActa(ActaEscrutinio.builder(mesa.getCodigo()).votosCandidato("1000001", 10)
                .firmadaPor("52123456").construir());
    }

    // ---- registrar (Incidencia) ----
    @Test
    @DisplayName("registrar: el RegistradorIncidencia fabrica una Incidencia con id consecutivo")
    void registrarIncidencia() {
        RegistradorNovedad registrador = new RegistradorIncidencia(Severidad.MEDIA);
        Novedad n = registrador.registrar(mesa, DESCRIPCION, "Jurado");
        assertTrue(n instanceof Incidencia);
        assertEquals("INC-MEDIA-1", n.getId());
        assertEquals("11001-P1-M1", n.getCodigoMesa());
        assertEquals(Severidad.MEDIA, ((Incidencia) n).getSeveridad());
    }

    @Test
    @DisplayName("registrar: ids consecutivos en registros sucesivos")
    void registrarConsecutivos() {
        RegistradorNovedad registrador = new RegistradorIncidencia(Severidad.BAJA);
        registrador.registrar(mesa, DESCRIPCION, "Jurado");
        Novedad segunda = registrador.registrar(mesa, DESCRIPCION, "Jurado");
        assertEquals("INC-BAJA-2", segunda.getId());
        assertEquals(2, registrador.getCantidadRegistradas());
    }

    @Test
    @DisplayName("registrar (negativa): mesa nula")
    void registrarMesaNula() {
        RegistradorNovedad registrador = new RegistradorIncidencia(Severidad.BAJA);
        assertThrows(IllegalArgumentException.class, () -> registrador.registrar(null, DESCRIPCION, "Jurado"));
    }

    @Test
    @DisplayName("registrar (negativa): si la novedad es inválida no se consume el consecutivo")
    void registrarInvalidaNoConsumeConsecutivo() {
        RegistradorNovedad registrador = new RegistradorIncidencia(Severidad.BAJA);
        assertThrows(IllegalArgumentException.class, () -> registrador.registrar(mesa, "corta", "Jurado"));
        assertEquals(0, registrador.getCantidadRegistradas());
        assertEquals("INC-BAJA-1", registrador.registrar(mesa, DESCRIPCION, "Jurado").getId());
    }

    // ---- registrar (Reclamación) ----
    @Test
    @DisplayName("registrar: el RegistradorReclamacion fabrica una Reclamacion si la mesa tiene acta")
    void registrarReclamacion() {
        registrarActaEnMesa();
        Novedad n = new RegistradorReclamacion(TipoReclamante.TESTIGO).registrar(mesa, DESCRIPCION, "Testigo X");
        assertTrue(n instanceof Reclamacion);
        assertEquals("REC-TESTIGO-1", n.getId());
    }

    @Test
    @DisplayName("registrar (negativa): no se puede reclamar sobre una mesa sin acta")
    void registrarReclamacionSinActa() {
        RegistradorNovedad registrador = new RegistradorReclamacion(TipoReclamante.PARTIDO);
        assertThrows(IllegalStateException.class, () -> registrador.registrar(mesa, DESCRIPCION, "Partido"));
        assertEquals(0, registrador.getCantidadRegistradas());
    }

    // ---- constructores / getCantidadRegistradas ----
    @Test
    @DisplayName("constructores (negativa): severidad o tipo de reclamante nulos")
    void constructoresNulos() {
        assertThrows(IllegalArgumentException.class, () -> new RegistradorIncidencia(null));
        assertThrows(IllegalArgumentException.class, () -> new RegistradorReclamacion(null));
    }

    @Test
    @DisplayName("getCantidadRegistradas (límite): un registrador nuevo tiene 0")
    void cantidadInicial() {
        assertEquals(0, new RegistradorIncidencia(Severidad.ALTA).getCantidadRegistradas());
    }
}
