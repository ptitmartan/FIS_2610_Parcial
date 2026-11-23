package co.edu.javeriana.fis.electoral.evento;

/**
 * Patrón <b>Observer</b> (comportamental): cualquier componente interesado en lo que pasa
 * durante la jornada (auditoría, monitoreo de reclamaciones, tableros de analistas) implementa
 * esta interfaz y se suscribe al {@link PublicadorEventos}. El sistema no necesita conocer a
 * cada interesado: solo publica el evento.
 */
public interface ObservadorElectoral {

    void actualizar(EventoElectoral evento);
}
