# Sistema de Información Electoral — FIS 2610, Parcial 2 (práctica)

Sistema en Java 17 para gestionar el proceso electoral colombiano de 2026: procesos, territorio
(departamentos, municipios, puestos y mesas), partidos y candidatos, jurados, actas de escrutinio,
incidencias y reclamaciones, y análisis de resultados por territorio.

**Equipo:** Martin Rodriguez, Miguel Ovalle, Danna, Juan Calderon.

## Cómo ejecutarlo

```bash
cd sistema-electoral
mvn test        # 213 pruebas unitarias (JUnit 5)
mvn verify      # pruebas + reporte de cobertura en target/site/jacoco/index.html
```

## Estructura

```
sistema-electoral/
├── pom.xml
└── src/
    ├── main/java/co/edu/javeriana/fis/electoral/
    │   ├── modelo/      ProcesoElectoral, PartidoPolitico, Candidato, Jurado, ActaEscrutinio (Builder)
    │   ├── estado/      EstadoProceso y sus 4 estados (State)
    │   ├── territorio/  ComponenteElectoral, UnidadTerritorial, Departamento, Municipio,
    │   │                PuestoVotacion, MesaVotacion (Composite)
    │   ├── novedad/     Novedad, Incidencia, Reclamacion, RegistradorNovedad (Factory Method)
    │   ├── evento/      PublicadorEventos, BitacoraAuditoria, MonitorReclamaciones (Observer)
    │   └── fachada/     SistemaElectoralFacade (Facade)
    └── test/java/...    una clase de prueba por entidad
```

## Patrones GoF implementados

| Categoría      | Patrón         | Dónde                                   | Problema que resuelve |
|----------------|----------------|-----------------------------------------|-----------------------|
| Creacional     | Builder        | `ActaEscrutinio.Builder`                | Armar un acta con muchos datos paso a paso y validarla completa antes de crearla (inmutable). |
| Creacional     | Factory Method | `RegistradorNovedad` y subclases       | Mismo flujo de registro para incidencias y reclamaciones; cada subclase decide qué objeto crear. |
| Estructural    | Composite      | `ComponenteElectoral` y territorio      | Consultar votos, censo y participación igual en una mesa o en un departamento entero. |
| Estructural    | Facade         | `SistemaElectoralFacade`                | Un punto de entrada simple que oculta estados, árbol territorial, fábricas y eventos. |
| Comportamental | State          | `EstadoProceso` y sus estados          | Reglas de cada etapa del calendario sin `if/switch` sobre el estado. |
| Comportamental | Observer       | `PublicadorEventos` y observadores      | Auditoría y monitoreo de reclamaciones sin acoplar el sistema a cada interesado. |

## Pipeline CI/CD

`.github/workflows/ci-sistema-electoral.yml` (en la raíz del repositorio) se activa con cada push a la
rama `grupo-electoral`: **Compilación → Pruebas unitarias → Cobertura (JaCoCo) → Empaquetado (JAR)**.
Cada etapa depende de la anterior, así que un error detiene el pipeline antes de generar el entregable.

## Convención de commits

[Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/): `tipo(alcance): descripción`,
con `feat`, `test`, `ci`, `docs`, `refactor`, `fix`, `build`, y referencia a la historia de usuario en el pie
(`Refs: HU-05`).
