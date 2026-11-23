package co.edu.javeriana.fis.electoral.fachada;

import co.edu.javeriana.fis.electoral.evento.BitacoraAuditoria;
import co.edu.javeriana.fis.electoral.evento.EventoElectoral;
import co.edu.javeriana.fis.electoral.evento.MonitorReclamaciones;
import co.edu.javeriana.fis.electoral.evento.ObservadorElectoral;
import co.edu.javeriana.fis.electoral.evento.PublicadorEventos;
import co.edu.javeriana.fis.electoral.evento.TipoEvento;
import co.edu.javeriana.fis.electoral.modelo.ActaEscrutinio;
import co.edu.javeriana.fis.electoral.modelo.Candidato;
import co.edu.javeriana.fis.electoral.modelo.Jurado;
import co.edu.javeriana.fis.electoral.modelo.PartidoPolitico;
import co.edu.javeriana.fis.electoral.modelo.ProcesoElectoral;
import co.edu.javeriana.fis.electoral.modelo.RolJurado;
import co.edu.javeriana.fis.electoral.modelo.TipoEleccion;
import co.edu.javeriana.fis.electoral.novedad.Novedad;
import co.edu.javeriana.fis.electoral.novedad.RegistradorIncidencia;
import co.edu.javeriana.fis.electoral.novedad.RegistradorNovedad;
import co.edu.javeriana.fis.electoral.novedad.RegistradorReclamacion;
import co.edu.javeriana.fis.electoral.novedad.Severidad;
import co.edu.javeriana.fis.electoral.novedad.TipoReclamante;
import co.edu.javeriana.fis.electoral.territorio.ComponenteElectoral;
import co.edu.javeriana.fis.electoral.territorio.Departamento;
import co.edu.javeriana.fis.electoral.territorio.MesaVotacion;
import co.edu.javeriana.fis.electoral.territorio.Municipio;
import co.edu.javeriana.fis.electoral.territorio.PuestoVotacion;
import co.edu.javeriana.fis.electoral.territorio.UnidadTerritorial;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Patrón <b>Facade</b> (estructural): punto de entrada único para funcionarios, jurados y
 * analistas. Oculta la complejidad interna (árbol territorial Composite, estados del proceso,
 * fábricas de novedades y publicación de eventos) detrás de operaciones de negocio simples
 * como "registrar acta" o "consultar votos de un candidato en un territorio".
 */
public class SistemaElectoralFacade {

    public static final int UMBRAL_RECLAMACIONES_POR_DEFECTO = 2;

    private final Map<String, ProcesoElectoral> procesos = new HashMap<>();
    private final Map<String, PartidoPolitico> partidos = new HashMap<>();
    private final Map<String, Jurado> jurados = new HashMap<>();
    private final Map<String, ComponenteElectoral> territorio = new HashMap<>();
    private final List<Novedad> novedades = new ArrayList<>();
    private final Map<Severidad, RegistradorNovedad> registradoresIncidencia = new EnumMap<>(Severidad.class);
    private final Map<TipoReclamante, RegistradorNovedad> registradoresReclamacion = new EnumMap<>(TipoReclamante.class);

    private final PublicadorEventos publicador = new PublicadorEventos();
    private final BitacoraAuditoria bitacora = new BitacoraAuditoria();
    private final MonitorReclamaciones monitor;

    public SistemaElectoralFacade() {
        this(UMBRAL_RECLAMACIONES_POR_DEFECTO);
    }

    public SistemaElectoralFacade(int umbralReclamaciones) {
        this.monitor = new MonitorReclamaciones(umbralReclamaciones);
        publicador.suscribir(bitacora);
        publicador.suscribir(monitor);
    }

    // ---------- Planeación ----------

    public ProcesoElectoral registrarProceso(String codigo, TipoEleccion tipo, LocalDate fecha) {
        ProcesoElectoral proceso = new ProcesoElectoral(codigo, tipo, fecha);
        if (procesos.putIfAbsent(proceso.getCodigo(), proceso) != null) {
            throw new IllegalStateException("Ya existe el proceso " + codigo);
        }
        return proceso;
    }

    public PartidoPolitico registrarPartido(String codigo, String nombre) {
        PartidoPolitico partido = new PartidoPolitico(codigo, nombre);
        if (partidos.putIfAbsent(partido.getCodigo(), partido) != null) {
            throw new IllegalStateException("Ya existe el partido " + codigo);
        }
        return partido;
    }

    /** Crea el candidato, registra el aval del partido y lo inscribe en el proceso. */
    public Candidato inscribirCandidato(String codigoProceso, String documento, String nombre, String codigoPartido) {
        ProcesoElectoral proceso = buscarProceso(codigoProceso);
        PartidoPolitico partido = buscarPartido(codigoPartido);
        Candidato candidato = new Candidato(documento, nombre, partido);
        proceso.inscribirCandidato(candidato);
        if (!partido.getCandidatosAvalados().contains(candidato)) {
            partido.avalarCandidato(candidato);
        }
        return candidato;
    }

    public Departamento registrarDepartamento(String codigo, String nombre) {
        Departamento departamento = new Departamento(codigo, nombre);
        indexar(departamento);
        return departamento;
    }

    public Municipio registrarMunicipio(String codigoDepartamento, String codigo, String nombre) {
        UnidadTerritorial departamento = buscarUnidad(codigoDepartamento);
        Municipio municipio = new Municipio(codigo, nombre);
        agregarEIndexar(departamento, municipio);
        return municipio;
    }

    public PuestoVotacion registrarPuesto(String codigoMunicipio, String codigo, String nombre, String direccion) {
        UnidadTerritorial municipio = buscarUnidad(codigoMunicipio);
        PuestoVotacion puesto = new PuestoVotacion(codigo, nombre, direccion);
        agregarEIndexar(municipio, puesto);
        return puesto;
    }

    /** Crea la siguiente mesa del puesto. Su código es CODIGOPUESTO-M#. */
    public MesaVotacion registrarMesa(String codigoPuesto, int censo) {
        UnidadTerritorial unidad = buscarUnidad(codigoPuesto);
        if (!(unidad instanceof PuestoVotacion puesto)) {
            throw new IllegalArgumentException(codigoPuesto + " no es un puesto de votación");
        }
        int numero = puesto.siguienteNumeroMesa();
        MesaVotacion mesa = new MesaVotacion(codigoPuesto + "-M" + numero, numero, censo);
        agregarEIndexar(puesto, mesa);
        return mesa;
    }

    public Jurado asignarJurado(String codigoMesa, String documento, String nombre, RolJurado rol) {
        MesaVotacion mesa = buscarMesa(codigoMesa);
        Jurado jurado = jurados.computeIfAbsent(documento, d -> new Jurado(d, nombre));
        mesa.asignarJurado(jurado, rol);
        return jurado;
    }

    // ---------- Jornada electoral ----------

    public void abrirVotacion(String codigoProceso) {
        ProcesoElectoral proceso = buscarProceso(codigoProceso);
        proceso.abrirVotacion();
        notificarCambioEstado(proceso);
    }

    public void cerrarVotacion(String codigoProceso) {
        ProcesoElectoral proceso = buscarProceso(codigoProceso);
        proceso.cerrarVotacion();
        notificarCambioEstado(proceso);
    }

    public void publicarResultados(String codigoProceso) {
        ProcesoElectoral proceso = buscarProceso(codigoProceso);
        proceso.publicarResultados();
        notificarCambioEstado(proceso);
    }

    /** Registra el acta de una mesa. El proceso debe estar en escrutinio y los candidatos inscritos. */
    public void registrarActa(String codigoProceso, ActaEscrutinio acta) {
        ProcesoElectoral proceso = buscarProceso(codigoProceso);
        if (acta == null) {
            throw new IllegalArgumentException("El acta no puede ser nula");
        }
        if (!proceso.permiteRegistroActas()) {
            throw new IllegalStateException("No se pueden registrar actas en estado " + proceso.getNombreEstado());
        }
        for (String documento : acta.getVotosPorCandidato().keySet()) {
            if (!proceso.estaInscrito(documento)) {
                throw new IllegalArgumentException("El candidato " + documento + " no está inscrito en el proceso");
            }
        }
        MesaVotacion mesa = buscarMesa(acta.getCodigoMesa());
        mesa.registrarActa(acta);
        publicador.publicar(EventoElectoral.de(TipoEvento.ACTA_REGISTRADA, mesa.getCodigo(),
                "Acta con " + acta.getTotalVotos() + " votos"));
    }

    public Novedad reportarIncidencia(String codigoMesa, String descripcion, String reportadoPor, Severidad severidad) {
        if (severidad == null) {
            throw new IllegalArgumentException("La severidad es obligatoria");
        }
        RegistradorNovedad registrador = registradoresIncidencia
                .computeIfAbsent(severidad, RegistradorIncidencia::new);
        return registrarNovedad(registrador, codigoMesa, descripcion, reportadoPor, TipoEvento.INCIDENCIA_REPORTADA);
    }

    public Novedad presentarReclamacion(String codigoMesa, String descripcion, String reclamante, TipoReclamante tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de reclamante es obligatorio");
        }
        RegistradorNovedad registrador = registradoresReclamacion
                .computeIfAbsent(tipo, RegistradorReclamacion::new);
        return registrarNovedad(registrador, codigoMesa, descripcion, reclamante, TipoEvento.RECLAMACION_PRESENTADA);
    }

    // ---------- Análisis ----------

    public int consultarVotos(String codigoTerritorio, String documentoCandidato) {
        return buscarComponente(codigoTerritorio).getVotosCandidato(documentoCandidato);
    }

    public double consultarParticipacion(String codigoTerritorio) {
        return buscarComponente(codigoTerritorio).getParticipacion();
    }

    /** Votos de cada candidato del proceso en el territorio, de mayor a menor. */
    public Map<String, Integer> resultadosPorCandidato(String codigoProceso, String codigoTerritorio) {
        ProcesoElectoral proceso = buscarProceso(codigoProceso);
        ComponenteElectoral componente = buscarComponente(codigoTerritorio);
        Map<String, Integer> resultados = new LinkedHashMap<>();
        proceso.getCandidatos().stream()
                .sorted((a, b) -> Integer.compare(componente.getVotosCandidato(b.getDocumento()),
                        componente.getVotosCandidato(a.getDocumento())))
                .forEach(c -> resultados.put(c.getDocumento(), componente.getVotosCandidato(c.getDocumento())));
        return resultados;
    }

    public boolean suscribirObservador(ObservadorElectoral observador) {
        return publicador.suscribir(observador);
    }

    public BitacoraAuditoria getBitacora() {
        return bitacora;
    }

    public MonitorReclamaciones getMonitor() {
        return monitor;
    }

    public List<Novedad> getNovedades() {
        return Collections.unmodifiableList(novedades);
    }

    // ---------- Apoyo interno ----------

    private Novedad registrarNovedad(RegistradorNovedad registrador, String codigoMesa, String descripcion,
                                     String reportadoPor, TipoEvento tipoEvento) {
        MesaVotacion mesa = buscarMesa(codigoMesa);
        Novedad novedad = registrador.registrar(mesa, descripcion, reportadoPor);
        novedades.add(novedad);
        publicador.publicar(EventoElectoral.de(tipoEvento, mesa.getCodigo(), novedad.getDescripcion()));
        return novedad;
    }

    private void notificarCambioEstado(ProcesoElectoral proceso) {
        publicador.publicar(EventoElectoral.de(TipoEvento.CAMBIO_ESTADO_PROCESO, proceso.getCodigo(),
                "Nuevo estado: " + proceso.getNombreEstado()));
    }

    private void indexar(ComponenteElectoral componente) {
        if (territorio.putIfAbsent(componente.getCodigo(), componente) != null) {
            throw new IllegalStateException("Ya existe un componente territorial con código " + componente.getCodigo());
        }
    }

    private void agregarEIndexar(UnidadTerritorial padre, ComponenteElectoral hijo) {
        if (territorio.containsKey(hijo.getCodigo())) {
            throw new IllegalStateException("Ya existe un componente territorial con código " + hijo.getCodigo());
        }
        padre.agregar(hijo);
        indexar(hijo);
    }

    private ProcesoElectoral buscarProceso(String codigo) {
        ProcesoElectoral proceso = procesos.get(codigo);
        if (proceso == null) {
            throw new NoSuchElementException("No existe el proceso " + codigo);
        }
        return proceso;
    }

    private PartidoPolitico buscarPartido(String codigo) {
        PartidoPolitico partido = partidos.get(codigo);
        if (partido == null) {
            throw new NoSuchElementException("No existe el partido " + codigo);
        }
        return partido;
    }

    private ComponenteElectoral buscarComponente(String codigo) {
        ComponenteElectoral componente = territorio.get(codigo);
        if (componente == null) {
            throw new NoSuchElementException("No existe el territorio " + codigo);
        }
        return componente;
    }

    private UnidadTerritorial buscarUnidad(String codigo) {
        ComponenteElectoral componente = buscarComponente(codigo);
        if (!(componente instanceof UnidadTerritorial unidad)) {
            throw new IllegalArgumentException(codigo + " no puede contener otros componentes");
        }
        return unidad;
    }

    private MesaVotacion buscarMesa(String codigo) {
        ComponenteElectoral componente = buscarComponente(codigo);
        if (!(componente instanceof MesaVotacion mesa)) {
            throw new IllegalArgumentException(codigo + " no es una mesa de votación");
        }
        return mesa;
    }
}
