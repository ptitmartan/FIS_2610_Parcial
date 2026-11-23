package co.edu.javeriana.fis.electoral.evento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PublicadorEventos (sujeto del Observer)")
class PublicadorEventosTest {

    private PublicadorEventos publicador;
    private List<EventoElectoral> recibidos;
    private ObservadorElectoral espia;

    @BeforeEach
    void setUp() {
        publicador = new PublicadorEventos();
        recibidos = new ArrayList<>();
        espia = recibidos::add;
    }

    private EventoElectoral evento() {
        return EventoElectoral.de(TipoEvento.ACTA_REGISTRADA, "M1", "Acta");
    }

    // ---- suscribir ----
    @Test
    @DisplayName("suscribir: agrega el observador")
    void suscribir() {
        assertTrue(publicador.suscribir(espia));
        assertEquals(1, publicador.cantidadObservadores());
    }

    @Test
    @DisplayName("suscribir (límite): suscribir dos veces no duplica notificaciones")
    void suscribirDuplicado() {
        publicador.suscribir(espia);
        assertFalse(publicador.suscribir(espia));
        publicador.publicar(evento());
        assertEquals(1, recibidos.size());
    }

    @Test
    @DisplayName("suscribir (negativa): observador nulo")
    void suscribirNulo() {
        assertThrows(IllegalArgumentException.class, () -> publicador.suscribir(null));
    }

    // ---- desuscribir ----
    @Test
    @DisplayName("desuscribir: el observador deja de recibir eventos")
    void desuscribir() {
        publicador.suscribir(espia);
        assertTrue(publicador.desuscribir(espia));
        publicador.publicar(evento());
        assertTrue(recibidos.isEmpty());
    }

    @Test
    @DisplayName("desuscribir (límite): observador no suscrito devuelve false")
    void desuscribirNoSuscrito() {
        assertFalse(publicador.desuscribir(espia));
    }

    // ---- publicar ----
    @Test
    @DisplayName("publicar: notifica a todos los observadores")
    void publicarATodos() {
        List<EventoElectoral> otros = new ArrayList<>();
        publicador.suscribir(espia);
        publicador.suscribir(otros::add);
        EventoElectoral e = evento();
        assertEquals(2, publicador.publicar(e));
        assertSame(e, recibidos.get(0));
        assertSame(e, otros.get(0));
    }

    @Test
    @DisplayName("publicar (límite): sin observadores notifica a 0")
    void publicarSinObservadores() {
        assertEquals(0, publicador.publicar(evento()));
    }

    @Test
    @DisplayName("publicar (negativa): evento nulo")
    void publicarNulo() {
        assertThrows(IllegalArgumentException.class, () -> publicador.publicar(null));
    }
}
