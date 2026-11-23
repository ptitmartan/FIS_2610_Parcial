package co.edu.javeriana.fis.electoral.estado;

import co.edu.javeriana.fis.electoral.modelo.Candidato;
import co.edu.javeriana.fis.electoral.modelo.PartidoPolitico;
import co.edu.javeriana.fis.electoral.modelo.ProcesoElectoral;
import co.edu.javeriana.fis.electoral.modelo.TipoEleccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Estados del proceso (State)")
class EstadoProcesoTest {

    private ProcesoElectoral proceso;

    @BeforeEach
    void setUp() {
        proceso = new ProcesoElectoral("SEN-2026", TipoEleccion.SENADO, LocalDate.of(2026, 3, 8));
    }

    @Test
    @DisplayName("EstadoProgramado: permite inscripción y no permite actas")
    void programadoPermisos() {
        EstadoProceso e = new EstadoProgramado();
        assertEquals("PROGRAMADO", e.getNombre());
        assertTrue(e.permiteInscripcionCandidatos());
        assertFalse(e.permiteRegistroActas());
    }

    @Test
    @DisplayName("EstadoProgramado.abrirVotacion: con candidatos pasa a EstadoEnVotacion")
    void programadoAbre() {
        proceso.inscribirCandidato(new Candidato("1000001", "Uno", new PartidoPolitico("PL", "Liberal")));
        assertTrue(new EstadoProgramado().abrirVotacion(proceso) instanceof EstadoEnVotacion);
    }

    @Test
    @DisplayName("EstadoProgramado (negativa): cerrar o publicar no son válidos")
    void programadoTransicionesInvalidas() {
        EstadoProceso e = new EstadoProgramado();
        assertThrows(IllegalStateException.class, () -> e.cerrarVotacion(proceso));
        assertThrows(IllegalStateException.class, () -> e.publicarResultados(proceso));
    }

    @Test
    @DisplayName("EstadoEnVotacion: cerrar pasa a escrutinio; no permite inscripción ni actas")
    void enVotacion() {
        EstadoProceso e = new EstadoEnVotacion();
        assertEquals("EN_VOTACION", e.getNombre());
        assertFalse(e.permiteInscripcionCandidatos());
        assertFalse(e.permiteRegistroActas());
        assertTrue(e.cerrarVotacion(proceso) instanceof EstadoEscrutinio);
    }

    @Test
    @DisplayName("EstadoEnVotacion (negativa): abrir o publicar no son válidos")
    void enVotacionInvalidas() {
        EstadoProceso e = new EstadoEnVotacion();
        assertThrows(IllegalStateException.class, () -> e.abrirVotacion(proceso));
        assertThrows(IllegalStateException.class, () -> e.publicarResultados(proceso));
    }

    @Test
    @DisplayName("EstadoEscrutinio: permite actas y publicar pasa a finalizado")
    void escrutinio() {
        EstadoProceso e = new EstadoEscrutinio();
        assertEquals("ESCRUTINIO", e.getNombre());
        assertTrue(e.permiteRegistroActas());
        assertTrue(e.publicarResultados(proceso) instanceof EstadoFinalizado);
    }

    @Test
    @DisplayName("EstadoEscrutinio (negativa): no se puede volver a abrir ni cerrar")
    void escrutinioInvalidas() {
        EstadoProceso e = new EstadoEscrutinio();
        assertThrows(IllegalStateException.class, () -> e.abrirVotacion(proceso));
        assertThrows(IllegalStateException.class, () -> e.cerrarVotacion(proceso));
    }

    @Test
    @DisplayName("EstadoFinalizado (límite): estado terminal, nada permitido")
    void finalizado() {
        EstadoProceso e = new EstadoFinalizado();
        assertEquals("FINALIZADO", e.getNombre());
        assertFalse(e.permiteInscripcionCandidatos());
        assertFalse(e.permiteRegistroActas());
        assertThrows(IllegalStateException.class, () -> e.abrirVotacion(proceso));
    }

    @Test
    @DisplayName("Mensaje de error indica la acción y el estado actual")
    void mensajeTransicionInvalida() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new EstadoFinalizado().publicarResultados(proceso));
        assertTrue(ex.getMessage().contains("publicar resultados"));
        assertTrue(ex.getMessage().contains("FINALIZADO"));
    }
}
