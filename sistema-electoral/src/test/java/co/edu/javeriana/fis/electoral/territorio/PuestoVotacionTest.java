package co.edu.javeriana.fis.electoral.territorio;

import co.edu.javeriana.fis.electoral.modelo.ActaEscrutinio;
import co.edu.javeriana.fis.electoral.modelo.Jurado;
import co.edu.javeriana.fis.electoral.modelo.RolJurado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PuestoVotacion (compuesto de mesas)")
class PuestoVotacionTest {

    private PuestoVotacion puesto;

    @BeforeEach
    void setUp() {
        puesto = new PuestoVotacion("11001-P1", "Colegio San Bartolomé", "Cra 7 # 9-96");
    }

    private MesaVotacion mesaConVotos(String codigo, int numero, int censo, String juradoDoc, int votos) {
        MesaVotacion m = new MesaVotacion(codigo, numero, censo);
        m.asignarJurado(new Jurado(juradoDoc, "Jurado"), RolJurado.PRESIDENTE);
        m.registrarActa(ActaEscrutinio.builder(codigo).votosCandidato("1000001", votos)
                .firmadaPor(juradoDoc).construir());
        return m;
    }

    // ---- constructor ----
    @Test
    @DisplayName("constructor: guarda nombre y dirección")
    void constructorValido() {
        assertEquals("Colegio San Bartolomé", puesto.getNombre());
        assertEquals("Cra 7 # 9-96", puesto.getDireccion());
    }

    @Test
    @DisplayName("constructor (negativa): dirección o código vacíos")
    void constructorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new PuestoVotacion("P", "Puesto", " "));
        assertThrows(IllegalArgumentException.class, () -> new PuestoVotacion(null, "Puesto", "Calle 1"));
    }

    // ---- agregar ----
    @Test
    @DisplayName("agregar: acepta mesas de votación")
    void agregarMesa() {
        puesto.agregar(new MesaVotacion("11001-P1-M1", 1, 300));
        assertEquals(1, puesto.getHijos().size());
    }

    @Test
    @DisplayName("agregar (negativa): no acepta municipios dentro de un puesto ni nulos")
    void agregarTipoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> puesto.agregar(new Municipio("11001", "Bogotá")));
        assertThrows(IllegalArgumentException.class, () -> puesto.agregar(null));
    }

    @Test
    @DisplayName("agregar (negativa): no acepta dos mesas con el mismo código")
    void agregarDuplicado() {
        puesto.agregar(new MesaVotacion("11001-P1-M1", 1, 300));
        assertThrows(IllegalStateException.class, () -> puesto.agregar(new MesaVotacion("11001-P1-M1", 2, 100)));
    }

    // ---- remover / buscarHijo ----
    @Test
    @DisplayName("remover: quita la mesa existente")
    void removerExistente() {
        puesto.agregar(new MesaVotacion("11001-P1-M1", 1, 300));
        assertTrue(puesto.remover("11001-P1-M1"));
        assertTrue(puesto.getHijos().isEmpty());
    }

    @Test
    @DisplayName("remover (límite): código inexistente devuelve false")
    void removerInexistente() {
        assertFalse(puesto.remover("NO-EXISTE"));
    }

    @Test
    @DisplayName("buscarHijo: encuentra por código o devuelve null")
    void buscarHijo() {
        MesaVotacion m = new MesaVotacion("11001-P1-M1", 1, 300);
        puesto.agregar(m);
        assertSame(m, puesto.buscarHijo("11001-P1-M1"));
        assertNull(puesto.buscarHijo("X"));
    }

    // ---- siguienteNumeroMesa ----
    @Test
    @DisplayName("siguienteNumeroMesa (límite): puesto vacío empieza en 1")
    void siguienteNumeroVacio() {
        assertEquals(1, puesto.siguienteNumeroMesa());
    }

    @Test
    @DisplayName("siguienteNumeroMesa: máximo número existente + 1")
    void siguienteNumero() {
        puesto.agregar(new MesaVotacion("A", 1, 10));
        puesto.agregar(new MesaVotacion("B", 4, 10));
        assertEquals(5, puesto.siguienteNumeroMesa());
    }

    // ---- agregados del Composite ----
    @Test
    @DisplayName("getCenso / contarMesas (límite): puesto sin mesas tiene 0")
    void agregadosVacios() {
        assertEquals(0, puesto.getCenso());
        assertEquals(0, puesto.contarMesas());
        assertEquals(0.0, puesto.getParticipacion(), 0.0001);
    }

    @Test
    @DisplayName("getCenso / getTotalVotos / getVotosCandidato: suman sus mesas")
    void agregadosConMesas() {
        puesto.agregar(mesaConVotos("A", 1, 300, "1000010", 120));
        puesto.agregar(mesaConVotos("B", 2, 200, "1000011", 80));
        assertEquals(500, puesto.getCenso());
        assertEquals(200, puesto.getTotalVotos());
        assertEquals(200, puesto.getVotosCandidato("1000001"));
        assertEquals(2, puesto.contarMesas());
        assertEquals(0.4, puesto.getParticipacion(), 0.0001);
    }
}
