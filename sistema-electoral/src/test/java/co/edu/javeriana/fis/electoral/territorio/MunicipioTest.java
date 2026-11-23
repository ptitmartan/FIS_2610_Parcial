package co.edu.javeriana.fis.electoral.territorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Municipio (compuesto de puestos)")
class MunicipioTest {

    private Municipio municipio;

    @BeforeEach
    void setUp() {
        municipio = new Municipio("11001", "Bogotá D.C.");
    }

    @Test
    @DisplayName("constructor: guarda código y nombre sin espacios sobrantes")
    void constructorValido() {
        Municipio m = new Municipio(" 05001 ", " Medellín ");
        assertEquals("05001", m.getCodigo());
        assertEquals("Medellín", m.getNombre());
    }

    @Test
    @DisplayName("constructor (negativa): nombre vacío")
    void constructorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Municipio("05001", ""));
    }

    @Test
    @DisplayName("agregar: acepta puestos de votación")
    void agregarPuesto() {
        municipio.agregar(new PuestoVotacion("P1", "Colegio", "Calle 1"));
        assertEquals(1, municipio.getHijos().size());
    }

    @Test
    @DisplayName("agregar (negativa): no acepta mesas sueltas ni departamentos")
    void agregarInvalido() {
        assertThrows(IllegalArgumentException.class, () -> municipio.agregar(new MesaVotacion("M1", 1, 100)));
        assertThrows(IllegalArgumentException.class, () -> municipio.agregar(new Departamento("11", "Bogotá")));
    }

    @Test
    @DisplayName("contarMesas / getCenso: suma las mesas de todos sus puestos")
    void agregadosAnidados() {
        PuestoVotacion p1 = new PuestoVotacion("P1", "Colegio", "Calle 1");
        PuestoVotacion p2 = new PuestoVotacion("P2", "Coliseo", "Calle 2");
        p1.agregar(new MesaVotacion("P1-M1", 1, 300));
        p1.agregar(new MesaVotacion("P1-M2", 2, 250));
        p2.agregar(new MesaVotacion("P2-M1", 1, 100));
        municipio.agregar(p1);
        municipio.agregar(p2);
        assertEquals(3, municipio.contarMesas());
        assertEquals(650, municipio.getCenso());
    }

    @Test
    @DisplayName("contarMesas (límite): puestos sin mesas dan 0 y la participación no divide por cero")
    void puestosVacios() {
        municipio.agregar(new PuestoVotacion("P1", "Colegio", "Calle 1"));
        assertEquals(0, municipio.contarMesas());
        assertEquals(0.0, municipio.getParticipacion(), 0.0001);
    }

    @Test
    @DisplayName("getHijos (negativa): la lista no se puede modificar desde fuera")
    void hijosInmodificable() {
        assertThrows(UnsupportedOperationException.class,
                () -> municipio.getHijos().add(new PuestoVotacion("P9", "X", "Y")));
    }
}
