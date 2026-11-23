package co.edu.javeriana.fis.electoral.fachada;

import co.edu.javeriana.fis.electoral.evento.EventoElectoral;
import co.edu.javeriana.fis.electoral.evento.TipoEvento;
import co.edu.javeriana.fis.electoral.modelo.ActaEscrutinio;
import co.edu.javeriana.fis.electoral.modelo.Candidato;
import co.edu.javeriana.fis.electoral.modelo.Jurado;
import co.edu.javeriana.fis.electoral.modelo.RolJurado;
import co.edu.javeriana.fis.electoral.modelo.TipoEleccion;
import co.edu.javeriana.fis.electoral.novedad.Incidencia;
import co.edu.javeriana.fis.electoral.novedad.Novedad;
import co.edu.javeriana.fis.electoral.novedad.Reclamacion;
import co.edu.javeriana.fis.electoral.novedad.Severidad;
import co.edu.javeriana.fis.electoral.novedad.TipoReclamante;
import co.edu.javeriana.fis.electoral.territorio.MesaVotacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SistemaElectoralFacade (Facade)")
class SistemaElectoralFacadeTest {

    private static final String PROCESO = "PRES-2026";
    private static final String ANA = "1012345678";
    private static final String LUIS = "1098765432";
    private static final String DESCRIPCION = "El acta presenta tachones en la casilla de votos";

    private SistemaElectoralFacade sistema;
    private MesaVotacion mesa1;
    private MesaVotacion mesa2;

    /** Escenario: Cundinamarca → Bogotá → Colegio con dos mesas, dos candidatos inscritos. */
    @BeforeEach
    void setUp() {
        sistema = new SistemaElectoralFacade();
        sistema.registrarProceso(PROCESO, TipoEleccion.PRESIDENCIAL, LocalDate.of(2026, 5, 31));
        sistema.registrarPartido("PL", "Partido Liberal");
        sistema.registrarPartido("PC", "Partido Conservador");
        sistema.inscribirCandidato(PROCESO, ANA, "Ana Gómez", "PL");
        sistema.inscribirCandidato(PROCESO, LUIS, "Luis Rojas", "PC");
        sistema.registrarDepartamento("25", "Cundinamarca");
        sistema.registrarMunicipio("25", "11001", "Bogotá");
        sistema.registrarPuesto("11001", "11001-P1", "Colegio San Bartolomé", "Cra 7 # 9-96");
        mesa1 = sistema.registrarMesa("11001-P1", 300);
        mesa2 = sistema.registrarMesa("11001-P1", 200);
        sistema.asignarJurado(mesa1.getCodigo(), "52000001", "Jurado Uno", RolJurado.PRESIDENTE);
        sistema.asignarJurado(mesa2.getCodigo(), "52000002", "Jurado Dos", RolJurado.PRESIDENTE);
    }

    private void llevarAEscrutinio() {
        sistema.abrirVotacion(PROCESO);
        sistema.cerrarVotacion(PROCESO);
    }

    private ActaEscrutinio acta(MesaVotacion mesa, String jurado, int votosAna, int votosLuis) {
        return ActaEscrutinio.builder(mesa.getCodigo()).votosCandidato(ANA, votosAna)
                .votosCandidato(LUIS, votosLuis).firmadaPor(jurado).construir();
    }

    private void registrarAmbasActas() {
        llevarAEscrutinio();
        sistema.registrarActa(PROCESO, acta(mesa1, "52000001", 100, 140));
        sistema.registrarActa(PROCESO, acta(mesa2, "52000002", 40, 100));
    }

    // ---- registrarProceso / registrarPartido ----
    @Test
    @DisplayName("registrarProceso: crea el proceso en estado PROGRAMADO")
    void registrarProceso() {
        assertEquals("PROGRAMADO",
                sistema.registrarProceso("SEN-2026", TipoEleccion.SENADO, LocalDate.of(2026, 3, 8)).getNombreEstado());
    }

    @Test
    @DisplayName("registrarProceso (negativa): código repetido")
    void registrarProcesoDuplicado() {
        assertThrows(IllegalStateException.class,
                () -> sistema.registrarProceso(PROCESO, TipoEleccion.PRESIDENCIAL, LocalDate.of(2026, 6, 21)));
    }

    @Test
    @DisplayName("registrarPartido: crea el partido")
    void registrarPartido() {
        assertEquals("Partido Verde", sistema.registrarPartido("PV", "Partido Verde").getNombre());
    }

    @Test
    @DisplayName("registrarPartido (negativa): código repetido")
    void registrarPartidoDuplicado() {
        assertThrows(IllegalStateException.class, () -> sistema.registrarPartido("PL", "Otro"));
    }

    // ---- inscribirCandidato ----
    @Test
    @DisplayName("inscribirCandidato: lo inscribe en el proceso y queda avalado por el partido")
    void inscribirCandidato() {
        sistema.registrarPartido("PV", "Partido Verde");
        Candidato c = sistema.inscribirCandidato(PROCESO, "1000000001", "Sofía Díaz", "PV");
        assertEquals("PV", c.getPartido().getCodigo());
        assertEquals(1, c.getPartido().cantidadCandidatos());
    }

    @Test
    @DisplayName("inscribirCandidato (negativa): proceso o partido inexistentes")
    void inscribirConReferenciasInexistentes() {
        assertThrows(NoSuchElementException.class, () -> sistema.inscribirCandidato("NO", "1000000001", "X", "PL"));
        assertThrows(NoSuchElementException.class, () -> sistema.inscribirCandidato(PROCESO, "1000000001", "X", "NO"));
    }

    @Test
    @DisplayName("inscribirCandidato (límite): el mismo candidato en dos procesos no duplica el aval")
    void inscribirEnDosProcesos() {
        sistema.registrarProceso("PRES-2026-V2", TipoEleccion.PRESIDENCIAL, LocalDate.of(2026, 6, 21));
        Candidato c = sistema.inscribirCandidato("PRES-2026-V2", ANA, "Ana Gómez", "PL");
        assertEquals(1, c.getPartido().cantidadCandidatos());
    }

    // ---- registro territorial ----
    @Test
    @DisplayName("registrarMesa: numera las mesas del puesto en orden y arma el código")
    void registrarMesaNumeracion() {
        assertEquals(1, mesa1.getNumero());
        assertEquals("11001-P1-M2", mesa2.getCodigo());
        assertEquals(3, sistema.registrarMesa("11001-P1", 50).getNumero());
    }

    @Test
    @DisplayName("registrarMesa (negativa): no se puede crear una mesa en un municipio")
    void registrarMesaEnMunicipio() {
        assertThrows(IllegalArgumentException.class, () -> sistema.registrarMesa("11001", 100));
    }

    @Test
    @DisplayName("registrarMunicipio/Puesto (negativa): padre inexistente o código duplicado")
    void registroTerritorialInvalido() {
        assertThrows(NoSuchElementException.class, () -> sistema.registrarMunicipio("99", "99001", "X"));
        assertThrows(IllegalStateException.class, () -> sistema.registrarPuesto("11001", "11001-P1", "X", "Y"));
        assertThrows(IllegalStateException.class, () -> sistema.registrarDepartamento("25", "Repetido"));
    }

    @Test
    @DisplayName("registrarPuesto (negativa): no se puede colgar un puesto de una mesa")
    void registrarPuestoEnMesa() {
        assertThrows(IllegalArgumentException.class,
                () -> sistema.registrarPuesto(mesa1.getCodigo(), "P-X", "X", "Calle 1"));
    }

    // ---- asignarJurado ----
    @Test
    @DisplayName("asignarJurado: registra el jurado en la mesa con su rol")
    void asignarJurado() {
        Jurado j = sistema.asignarJurado(mesa1.getCodigo(), "52000003", "Vocal", RolJurado.VOCAL);
        assertEquals(RolJurado.VOCAL, mesa1.getRolDe(j));
    }

    @Test
    @DisplayName("asignarJurado (negativa): el mismo ciudadano no puede ser jurado en dos mesas")
    void asignarJuradoEnDosMesas() {
        assertThrows(IllegalStateException.class,
                () -> sistema.asignarJurado(mesa2.getCodigo(), "52000001", "Jurado Uno", RolJurado.VOCAL));
    }

    @Test
    @DisplayName("asignarJurado (negativa): mesa inexistente")
    void asignarJuradoMesaInexistente() {
        assertThrows(NoSuchElementException.class,
                () -> sistema.asignarJurado("NO-EXISTE", "52000009", "X", RolJurado.VOCAL));
    }

    // ---- abrir / cerrar / publicar ----
    @Test
    @DisplayName("abrir/cerrar/publicar: recorren el ciclo y dejan rastro en la bitácora")
    void cicloCompletoConBitacora() {
        llevarAEscrutinio();
        sistema.publicarResultados(PROCESO);
        List<EventoElectoral> cambios = sistema.getBitacora().filtrarPorTipo(TipoEvento.CAMBIO_ESTADO_PROCESO);
        assertEquals(3, cambios.size());
        assertEquals("Nuevo estado: FINALIZADO", cambios.get(2).detalle());
    }

    @Test
    @DisplayName("cerrarVotacion (negativa): transición inválida no genera evento")
    void transicionInvalidaSinEvento() {
        assertThrows(IllegalStateException.class, () -> sistema.cerrarVotacion(PROCESO));
        assertEquals(0, sistema.getBitacora().cantidadRegistros());
    }

    @Test
    @DisplayName("publicarResultados (negativa): proceso inexistente")
    void publicarProcesoInexistente() {
        assertThrows(NoSuchElementException.class, () -> sistema.publicarResultados("NO"));
    }

    // ---- registrarActa ----
    @Test
    @DisplayName("registrarActa: guarda el acta en la mesa y publica ACTA_REGISTRADA")
    void registrarActa() {
        llevarAEscrutinio();
        sistema.registrarActa(PROCESO, acta(mesa1, "52000001", 150, 90));
        assertTrue(mesa1.tieneActa());
        assertEquals(1, sistema.getBitacora().filtrarPorTipo(TipoEvento.ACTA_REGISTRADA).size());
    }

    @Test
    @DisplayName("registrarActa (negativa): no se permite durante la votación")
    void registrarActaEnVotacion() {
        sistema.abrirVotacion(PROCESO);
        ActaEscrutinio a = acta(mesa1, "52000001", 10, 10);
        assertThrows(IllegalStateException.class, () -> sistema.registrarActa(PROCESO, a));
        assertFalse(mesa1.tieneActa());
    }

    @Test
    @DisplayName("registrarActa (negativa): candidato no inscrito en el proceso o acta nula")
    void registrarActaCandidatoNoInscrito() {
        llevarAEscrutinio();
        ActaEscrutinio a = ActaEscrutinio.builder(mesa1.getCodigo()).votosCandidato("1111111", 10)
                .firmadaPor("52000001").construir();
        assertThrows(IllegalArgumentException.class, () -> sistema.registrarActa(PROCESO, a));
        assertThrows(IllegalArgumentException.class, () -> sistema.registrarActa(PROCESO, null));
    }

    // ---- reportarIncidencia ----
    @Test
    @DisplayName("reportarIncidencia: crea una Incidencia y la publica")
    void reportarIncidencia() {
        Novedad n = sistema.reportarIncidencia(mesa1.getCodigo(), "Llegó incompleto el kit electoral",
                "Jurado Uno", Severidad.ALTA);
        assertTrue(n instanceof Incidencia);
        assertEquals(1, sistema.getNovedades().size());
        assertEquals(1, sistema.getBitacora().filtrarPorTipo(TipoEvento.INCIDENCIA_REPORTADA).size());
    }

    @Test
    @DisplayName("reportarIncidencia (negativa): severidad nula o mesa inexistente")
    void reportarIncidenciaInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> sistema.reportarIncidencia(mesa1.getCodigo(), DESCRIPCION, "J", null));
        assertThrows(NoSuchElementException.class,
                () -> sistema.reportarIncidencia("NO", DESCRIPCION, "J", Severidad.BAJA));
        assertTrue(sistema.getNovedades().isEmpty());
    }

    @Test
    @DisplayName("reportarIncidencia (límite): ids distintos para severidades distintas")
    void idsPorSeveridad() {
        Novedad a = sistema.reportarIncidencia(mesa1.getCodigo(), DESCRIPCION, "J", Severidad.ALTA);
        Novedad b = sistema.reportarIncidencia(mesa1.getCodigo(), DESCRIPCION, "J", Severidad.BAJA);
        assertNotEquals(a.getId(), b.getId());
    }

    // ---- presentarReclamacion ----
    @Test
    @DisplayName("presentarReclamacion: crea una Reclamacion y el monitor la cuenta")
    void presentarReclamacion() {
        registrarAmbasActas();
        Novedad n = sistema.presentarReclamacion(mesa1.getCodigo(), DESCRIPCION, "Testigo PL", TipoReclamante.TESTIGO);
        assertTrue(n instanceof Reclamacion);
        assertEquals(1, sistema.getMonitor().getReclamaciones(mesa1.getCodigo()));
    }

    @Test
    @DisplayName("presentarReclamacion (negativa): mesa sin acta")
    void reclamacionSinActa() {
        assertThrows(IllegalStateException.class,
                () -> sistema.presentarReclamacion(mesa1.getCodigo(), DESCRIPCION, "PL", TipoReclamante.PARTIDO));
        assertEquals(0, sistema.getMonitor().getReclamaciones(mesa1.getCodigo()));
    }

    @Test
    @DisplayName("presentarReclamacion (límite): al llegar al umbral la mesa queda en revisión")
    void reclamacionesHastaUmbral() {
        registrarAmbasActas();
        sistema.presentarReclamacion(mesa2.getCodigo(), DESCRIPCION, "PL", TipoReclamante.PARTIDO);
        assertTrue(sistema.getMonitor().mesasEnRevision().isEmpty());
        sistema.presentarReclamacion(mesa2.getCodigo(), DESCRIPCION, "Testigo", TipoReclamante.TESTIGO);
        assertEquals(List.of(mesa2.getCodigo()), sistema.getMonitor().mesasEnRevision());
    }

    // ---- consultas ----
    @Test
    @DisplayName("consultarVotos: suma a nivel de mesa, puesto y departamento")
    void consultarVotosPorNivel() {
        registrarAmbasActas();
        assertEquals(100, sistema.consultarVotos(mesa1.getCodigo(), ANA));
        assertEquals(140, sistema.consultarVotos("11001-P1", ANA));
        assertEquals(240, sistema.consultarVotos("25", LUIS));
    }

    @Test
    @DisplayName("consultarVotos (negativa): territorio inexistente")
    void consultarVotosTerritorioInexistente() {
        assertThrows(NoSuchElementException.class, () -> sistema.consultarVotos("99", ANA));
    }

    @Test
    @DisplayName("consultarParticipacion: votos / censo del territorio")
    void consultarParticipacion() {
        registrarAmbasActas();
        assertEquals(380.0 / 500.0, sistema.consultarParticipacion("11001"), 0.0001);
    }

    @Test
    @DisplayName("consultarParticipacion (límite): sin actas la participación es 0")
    void consultarParticipacionSinActas() {
        assertEquals(0.0, sistema.consultarParticipacion("25"), 0.0001);
    }

    @Test
    @DisplayName("resultadosPorCandidato: ordenados de mayor a menor votación")
    void resultadosOrdenados() {
        registrarAmbasActas();
        Map<String, Integer> r = sistema.resultadosPorCandidato(PROCESO, "25");
        assertEquals(List.of(LUIS, ANA), new ArrayList<>(r.keySet()));
        assertEquals(Integer.valueOf(240), r.get(LUIS));
        assertEquals(Integer.valueOf(140), r.get(ANA));
    }

    @Test
    @DisplayName("resultadosPorCandidato (límite): sin actas todos los candidatos tienen 0")
    void resultadosSinActas() {
        Map<String, Integer> r = sistema.resultadosPorCandidato(PROCESO, mesa1.getCodigo());
        assertEquals(2, r.size());
        assertTrue(r.values().stream().allMatch(v -> v == 0));
    }

    @Test
    @DisplayName("resultadosPorCandidato (negativa): proceso inexistente")
    void resultadosProcesoInexistente() {
        assertThrows(NoSuchElementException.class, () -> sistema.resultadosPorCandidato("NO", "25"));
    }

    // ---- suscribirObservador ----
    @Test
    @DisplayName("suscribirObservador: un observador externo recibe los eventos")
    void observadorExterno() {
        List<EventoElectoral> recibidos = new ArrayList<>();
        assertTrue(sistema.suscribirObservador(recibidos::add));
        sistema.reportarIncidencia(mesa1.getCodigo(), DESCRIPCION, "J", Severidad.BAJA);
        assertEquals(1, recibidos.size());
    }

    @Test
    @DisplayName("suscribirObservador (negativa): observador nulo")
    void observadorNulo() {
        assertThrows(IllegalArgumentException.class, () -> sistema.suscribirObservador(null));
    }

    @Test
    @DisplayName("constructor (negativa): umbral de reclamaciones inválido")
    void constructorUmbralInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new SistemaElectoralFacade(0));
    }
}
