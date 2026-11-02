# ADR-0012: Sistema nuevo, sin migración y con panel de administración dentro de la app

- **Estado:** Propuesta
- **Fecha:** 2026-11-02
- **Autor:** Juan Santiago Guerrero
- **Reemplaza a:** [ADR-0002](0002-backend-ktor.md)

## Contexto

El ADR-0002 eligió Ktor para el backend y dejó la relación con el panel de la academia como una **convivencia por etapas**: el código heredado se aislaba detrás de una interfaz (`InscripcionesPanelGateway`) con un adaptador que entregaba las inscripciones al panel actual, y durante la transición habría dos fuentes de datos.

Desde entonces el alcance creció (ADR-0004 y ADR-0005): ya no es una app que informa e inscribe y alimenta un panel, sino una plataforma de aprendizaje con identidad propia, catálogo, progreso y clases en vivo. Mantener la convivencia obligaría a sostener un adaptador hacia un sistema que no controlamos, cuyo código, base de datos y plan de alojamiento no conocemos (preguntas abiertas del ADR-0002), y a conciliar dos fuentes de datos con modelos distintos.

## Decisión

### 1. Sistema nuevo, sin reutilizar el código de la academia

El backend, la base de datos y la app se construyen desde cero. No se reutiliza código del sitio web, del script PHP de inscripción ni del panel actual, ni su esquema de datos, ni su inicio de sesión. No hay adaptador ni `InscripcionesPanelGateway`.

De la decisión del ADR-0002 **se conserva** el backend con **Ktor** como API REST, con la estructura rutas → casos de uso → repositorios y la verificación de rol en el servidor en cada operación (RNF-02).

### 2. Sin migración de datos

El sistema nuevo arranca con la base de datos vacía. Las inscripciones, los estudiantes, los certificados y los ciclos de pago que hoy están en el panel actual no se importan.

- Lo que la academia necesite de esos datos lo conserva ella en su sistema actual, bajo su política de tratamiento de datos personales.
- Un estudiante que ya estudia en la academia entra al sistema nuevo por una de dos vías: se registra él mismo o el Administrador lo da de alta manualmente (RF-33).
- Los certificados del sistema nuevo parten de su propia numeración consecutiva (RNF-12). Cómo se distinguen de los emitidos por el panel actual lo decide la academia.

### 3. Panel de administración dentro de la app, por rol

No hay un panel web aparte. La gestión (inscripciones, estudiantes, certificados, ciclos de pago, contenido y multimedia) es una sección de la **misma app**, visible solo para el rol **Administrador**.

- La sección se distingue por el rol de la cuenta, pero **la autorización real se comprueba en el servidor** en cada operación de gestión; ocultar pantallas no es una medida de seguridad (RF-20, RNF-02, ADR-0007).
- Cada contexto aporta sus pantallas de gestión en su propio módulo `presentation`. `contenido-admin` agrupa la carga de lecciones y multimedia (ADR-0004).
- Los PDF de fichas y certificados los genera el servidor.
- Las cuentas de Administrador se crean como describe el ADR-0007: la primera por script de arranque y las demás las crea otro Administrador. Ya no existe el "mientras el panel actual siga en uso" de ese ADR.

## Alternativas consideradas

| Alternativa | Por qué no |
| --- | --- |
| Convivencia por etapas con el panel actual (ADR-0002) | Ata el sistema nuevo a un código y una base que no controlamos y obliga a sostener dos fuentes de datos y un adaptador con reintentos. |
| Migrar los datos del panel actual | Exige acceso a su base, entender un esquema sin documentar y depurar datos personales de menores antes de tratarlos en el sistema nuevo. El costo supera el beneficio para el alcance actual. |
| Panel web nuevo, separado de la app | Es un tercer destino de compilación y una segunda interfaz que mantener con tres integrantes. Como la lógica está en `domain` y los casos de uso se exponen por la API, se puede agregar después sin tocar lo ya construido. |

## Consecuencias

- Desaparece el adaptador hacia el panel y la lógica de reintentos del ADR-0002; la ficha llega a la base nueva y se gestiona desde la app.
- La academia depende de la app para administrar: no hay una segunda vía de gestión. Las copias de seguridad diarias del ADR-0008 pasan a ser críticas.
- Las listas largas (RNF-09: cientos de registros con búsqueda y filtro) y los formularios de gestión deben diseñarse pensando en pantallas de teléfono y de tableta.
- El contenido de la sección de gestión crece con cada módulo y debe seguir la regla de dependencias: la sección no importa `data` de otro contexto.
- Quedan sin efecto estas referencias en otros ADR, que no se reescriben: ADR-0003 (alternativa "conservar el motor que ya usa el panel" y su pregunta abierta), ADR-0004 (contenido cargado "desde el panel") y ADR-0007 (inicio de sesión del panel y la alternativa "reutilizar el inicio de sesión del panel actual").
- El SRS debe actualizarse (versión 2.0): los requisitos RF-10 y RF-21 a RF-37 y el caso de uso CU-01 dejan de hablar de un panel existente y pasan a describir la sección de gestión de la app.

## Preguntas abiertas

Pendientes de confirmar con la academia:

- ¿Acepta reemplazar su panel actual por la sección de gestión de la app?
- ¿Qué se hace con los datos que quedan en el panel actual y hasta cuándo se mantiene?
- ¿Qué ocurre con el formulario de inscripción del sitio web actual mientras el sistema nuevo no esté en producción?
