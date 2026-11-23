#!/usr/bin/env bash
# Crea en GitHub las etiquetas, milestones e issues (historias de usuario y tareas técnicas)
# del Parcial 2 de FIS. Requiere GitHub CLI autenticado:  gh auth login
#
# Uso (desde la raíz del repositorio clonado):
#   ./scripts/crear_issues.sh                    # usa el repositorio actual
#   REPO=usuario/FIS_2610_Parcial ./scripts/crear_issues.sh
#
# Para asignar responsables, ponga los usuarios de GitHub de cada integrante:
MARTIN="${MARTIN:-}"   # usuario GitHub de Martin Rodriguez
MIGUEL="${MIGUEL:-}"   # usuario GitHub de Miguel Ovalle
DANNA="${DANNA:-}"     # usuario GitHub de Danna
JUAN="${JUAN:-}"       # usuario GitHub de Juan Calderon

set -euo pipefail
REPO_FLAG=()
if [[ -n "${REPO:-}" ]]; then REPO_FLAG=(--repo "$REPO"); fi
REPO_API="${REPO:-$(gh repo view --json nameWithOwner -q .nameWithOwner)}"

# ---------- Etiquetas ----------
crear_label() { gh label create "$1" --color "$2" --description "$3" --force "${REPO_FLAG[@]}" >/dev/null; }
crear_label "historia-usuario" "1D76DB" "Historia de usuario (Scrum)"
crear_label "tarea-tecnica"    "5319E7" "Tarea técnica del sprint"
crear_label "prioridad:alta"   "B60205" "Prioridad alta"
crear_label "prioridad:media"  "FBCA04" "Prioridad media"
crear_label "prioridad:baja"   "0E8A16" "Prioridad baja"
crear_label "patron-gof"       "C5DEF5" "Implementación de patrón de diseño GoF"
crear_label "pruebas"          "BFD4F2" "Pruebas unitarias"
crear_label "devops"           "D4C5F9" "CI/CD y automatización"
for p in 1 2 3 5 8; do crear_label "sp:$p" "EDEDED" "Estimación: $p story points"; done

# ---------- Milestones (sprints) ----------
crear_milestone() {
  gh api "repos/$REPO_API/milestones" -f title="$1" -f description="$2" -f due_on="$3" >/dev/null 2>&1 || true
}
crear_milestone "Parcial 2 - Sprint 1: Dominio y patrones" "Modelo del dominio electoral y patrones GoF" "2026-11-26T23:59:00Z"
crear_milestone "Parcial 2 - Sprint 2: Calidad y entrega"  "Pruebas, pipeline CI/CD e informe"          "2026-11-30T23:59:00Z"
S1="Parcial 2 - Sprint 1: Dominio y patrones"
S2="Parcial 2 - Sprint 2: Calidad y entrega"

# ---------- Issues ----------
crear_issue() { # titulo, cuerpo, labels, milestone, asignado
  local args=(--title "$1" --body "$2" --label "$3" --milestone "$4" "${REPO_FLAG[@]}")
  if [[ -n "$5" ]]; then args+=(--assignee "$5"); fi
  gh issue create "${args[@]}"
}

crear_issue "HU-01 Registrar procesos electorales" \
"**Como** funcionario de la autoridad electoral **quiero** registrar un proceso con su tipo, fecha y estado **para** organizar el calendario electoral.

**Criterios de aceptación**
- El proceso inicia en estado PROGRAMADO.
- Solo avanza PROGRAMADO → EN_VOTACION → ESCRUTINIO → FINALIZADO (patrón State).
- No se puede abrir la votación sin candidatos inscritos.
- No se aceptan códigos de proceso duplicados." \
"historia-usuario,prioridad:alta,sp:5,patron-gof" "$S1" "$MARTIN"

crear_issue "HU-02 Organizar la distribución territorial" \
"**Como** funcionario **quiero** registrar departamentos, municipios, puestos y mesas **para** saber dónde se vota y con qué censo.

**Criterios de aceptación**
- Jerarquía Departamento → Municipio → Puesto → Mesa (patrón Composite).
- Censo por mesa entre 1 y 1000; las mesas se numeran automáticamente por puesto.
- No se permiten códigos territoriales repetidos ni hijos de tipo incorrecto." \
"historia-usuario,prioridad:alta,sp:8,patron-gof" "$S1" "$MIGUEL"

crear_issue "HU-03 Inscribir candidatos y partidos" \
"**Como** responsable del proceso **quiero** registrar candidatos y relacionarlos con el partido que los avala **para** conocer quién participa y bajo qué organización.

**Criterios de aceptación**
- Documento de 6 a 10 dígitos.
- Un candidato no puede inscribirse dos veces en el mismo proceso.
- Solo se inscriben candidatos mientras el proceso está PROGRAMADO." \
"historia-usuario,prioridad:alta,sp:3" "$S1" "$DANNA"

crear_issue "HU-04 Asignar jurados de votación" \
"**Como** funcionario **quiero** asignar jurados a cada mesa con su función **para** dejar constancia de quién instala la mesa y registra resultados.

**Criterios de aceptación**
- Máximo 6 jurados por mesa; un solo presidente y un solo vicepresidente.
- Un ciudadano solo puede ser jurado en una mesa." \
"historia-usuario,prioridad:media,sp:3" "$S1" "$JUAN"

crear_issue "HU-05 Registrar el acta de escrutinio de una mesa" \
"**Como** jurado **quiero** registrar el acta con los votos de cada candidato, blancos, nulos y no marcados **para** consignar los resultados al cierre de la jornada.

**Criterios de aceptación**
- El acta se arma con el patrón Builder y es inmutable.
- Solo se registra en estado ESCRUTINIO, firmada por un jurado de la mesa, una sola vez y sin superar el censo.
- Solo incluye candidatos inscritos en el proceso." \
"historia-usuario,prioridad:alta,sp:5,patron-gof" "$S1" "$MARTIN"

crear_issue "HU-06 Reportar incidencias y reclamaciones" \
"**Como** jurado, partido o testigo **quiero** registrar incidencias y reclamaciones **para** que la autoridad las atienda y quede trazabilidad.

**Criterios de aceptación**
- Las novedades se crean con Factory Method (Incidencia / Reclamación).
- Solo se reclama sobre mesas con acta.
- Cada novedad queda registrada en la bitácora de auditoría (Observer)." \
"historia-usuario,prioridad:media,sp:5,patron-gof" "$S1" "$DANNA"

crear_issue "HU-07 Analizar resultados por territorio" \
"**Como** analista **quiero** consultar votos, participación y resultados por candidato en cualquier territorio **para** comparar regiones y detectar mesas a revisar.

**Criterios de aceptación**
- Consultas a nivel de mesa, puesto, municipio o departamento (Composite + Facade).
- Resultados ordenados de mayor a menor.
- Mesas con reclamaciones ≥ umbral quedan marcadas para revisión." \
"historia-usuario,prioridad:media,sp:5,patron-gof" "$S1" "$MIGUEL"

crear_issue "T-01 Pruebas unitarias de todas las entidades" \
"Mínimo dos pruebas por método, incluyendo una negativa y una de caso límite. Evidencia de ejecución en el informe." \
"tarea-tecnica,pruebas,prioridad:alta,sp:8" "$S2" "$JUAN"

crear_issue "T-02 Pipeline CI/CD en GitHub Actions" \
"Pipeline que se activa con push a la rama del equipo: compilación → pruebas → cobertura JaCoCo → empaquetado." \
"tarea-tecnica,devops,prioridad:alta,sp:3" "$S2" "$MARTIN"

crear_issue "T-03 Informe de resultados del Parcial 2" \
"Informe con patrones (justificación + código), pruebas, pipeline, gestión Scrum y evidencias de Git." \
"tarea-tecnica,prioridad:media,sp:3" "$S2" "$DANNA"

echo "Issues creados en $REPO_API"
