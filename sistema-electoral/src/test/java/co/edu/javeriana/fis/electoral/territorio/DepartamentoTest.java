package co.edu.javeriana.fis.electoral.territorio;

import co.edu.javeriana.fis.electoral.modelo.ActaEscrutinio;
import co.edu.javeriana.fis.electoral.modelo.Jurado;
import co.edu.javeriana.fis.electoral.modelo.RolJurado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Departamento (raíz del Composite)")
class DepartamentoTest {

    private Departamento antioquia;

    @BeforeEach
    void setUp() {
        antioquia = new Departamento("05", "Antioquia");
    }

    /** Arma Antioquia → Medellín/Envigado → un puesto con una mesa cada uno, con votos. */
    private void armarArbol() {
        antioquia.agregar(municipioConMesa("05001", "Medellín", 600, 300, 200, "1000010"));
        antioquia.agregar(municipioConMesa("05266", "Envigado", 100, 50, 10, "1000011"));
    }

    private Municipio municipioConMesa(String cod, String nombre, int censo, int votosA, int votosB, String jurado) {
        Municipio m = new Municipio(cod, nombre);
        PuestoVotacion p = new PuestoVotacion(cod + "-P1", "Puesto", "Calle 1");
        MesaVotacion mesa = new MesaVotacion(cod + "-P1-M1", 1, censo);
        mesa.asignarJurado(new Jurado(jurado, "J"), RolJurado.PRESIDENTE);
        mesa.registrarActa(ActaEscrutinio.builder(mesa.getCodigo())
                .votosCandidato("1000001", votosA).votosCandidato("1000002", votosB)
                .firmadaPor(jurado).construir());
        p.agregar(mesa);
        m.agregar(p);
        return m;
    }

    @Test
    @DisplayName("constructor (negativa): código vacío")
    void constructorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Departamento("", "Antioquia"));
    }

    @Test
    @DisplayName("agregar: acepta municipios")
    void agregarMunicipio() {
        antioquia.agregar(new Municipio("05001", "Medellín"));
        assertNotNull(antioquia.buscarHijo("05001"));
    }

    @Test
    @DisplayName("agregar (negativa): no acepta puestos directamente")
    void agregarPuestoDirecto() {
        assertThrows(IllegalArgumentException.class,
                () -> antioquia.agregar(new PuestoVotacion("P1", "Colegio", "Calle 1")));
    }

    @Test
    @DisplayName("getVotosCandidato: agrega los votos de todos los niveles")
    void votosAgregados() {
        armarArbol();
        assertEquals(350, antioquia.getVotosCandidato("1000001"));
        assertEquals(210, antioquia.getVotosCandidato("1000002"));
    }

    @Test
    @DisplayName("getVotosCandidato (límite): candidato sin votos en ninguna mesa da 0")
    void votosCandidatoInexistente() {
        armarArbol();
        assertEquals(0, antioquia.getVotosCandidato("9999999"));
    }

    @Test
    @DisplayName("getTotalVotos / getCenso / getParticipacion: totales del departamento")
    void totalesDepartamento() {
        armarArbol();
        assertEquals(560, antioquia.getTotalVotos());
        assertEquals(700, antioquia.getCenso());
        assertEquals(0.8, antioquia.getParticipacion(), 0.0001);
        assertEquals(2, antioquia.contarMesas());
    }

    @Test
    @DisplayName("getCenso (límite): departamento vacío tiene censo 0")
    void departamentoVacio() {
        assertEquals(0, antioquia.getCenso());
        assertEquals(0, antioquia.getTotalVotos());
    }

    @Test
    @DisplayName("remover: al quitar un municipio los totales se recalculan")
    void removerRecalcula() {
        armarArbol();
        assertTrue(antioquia.remover("05266"));
        assertEquals(500, antioquia.getTotalVotos());
    }
}
