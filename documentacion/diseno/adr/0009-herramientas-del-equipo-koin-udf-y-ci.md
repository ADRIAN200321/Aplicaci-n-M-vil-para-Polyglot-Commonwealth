# ADR-0009: Herramientas del equipo: Koin, MVVM unidireccional y CI

- **Estado:** Propuesta
- **Fecha:** 2026-10-30
- **Autor:** Juan Santiago Guerrero

## Contexto

Tres personas escriben código en siete módulos que deben respetar la regla de dependencias de Clean Architecture. Las reglas escritas en `CLAUDE.md` solo funcionan si algo las hace cumplir. Hace falta decidir la inyección de dependencias, el patrón de presentación y la integración continua (CI).

## Decisión

### 1. Inyección de dependencias: Koin

- Todas las clases reciben sus dependencias por el **constructor**.
- El contenedor de Koin se usa **solo en el punto de arranque** (módulo `app` en el cliente y arranque del servidor en el backend).
- `domain` no importa Koin. Ninguna clase llama al contenedor desde dentro (`get()` o `inject()`), porque eso es un localizador de servicios y rompe la inversión de dependencias.
- Una prueba en CI **verifica el grafo completo** y falla si falta alguna dependencia, ya que Koin resuelve en tiempo de ejecución.

### 2. Presentación: MVVM con flujo unidireccional

- Cada pantalla tiene un `UiState` inmutable (clase de datos o `sealed`).
- El usuario envía acciones como una jerarquía `sealed` a través de `onAction(...)`.
- Los efectos de una sola vez (navegar, mostrar un aviso) salen por un `Flow` aparte, no dentro del estado.
- Se usa el ViewModel multiplataforma de AndroidX. No se adoptan bibliotecas de MVI.
- El ViewModel solo llama a casos de uso.

### 3. Integración continua en GitHub Actions

Cada Pull Request ejecuta:

| Trabajo | Qué comprueba | Dónde corre |
| --- | --- | --- |
| Estilo | ktlint (formato) y detekt (malas prácticas y complejidad) | Linux |
| Pruebas unitarias | Dominio, datos y ViewModels | Linux |
| Arquitectura | Prueba con Konsist que falla si una capa rompe la regla de dependencias | Linux |
| Grafo de Koin | Verifica que todas las dependencias se pueden resolver | Linux |
| Backend | Pruebas con una PostgreSQL real en contenedor temporal (Testcontainers) | Linux |
| Android | Compila la aplicación | Linux |
| iOS | Compila la aplicación (ADR-0006) | macOS |

- No se puede fusionar a `main` si un trabajo falla, y se exige al menos una revisión.
- Si los minutos de macOS fueran un límite, el trabajo de iOS corre solo en Pull Requests hacia `main`.
- Las versiones de las bibliotecas se centralizan en un catálogo de Gradle (`libs.versions.toml`).
- Dependabot avisa de actualizaciones de seguridad.

## Alternativas consideradas

| Alternativa | Por qué no |
| --- | --- |
| Kodein | Similar a Koin, con menos comunidad. |
| kotlin-inject | Detecta errores al compilar, pero es menos conocida y con curva más empinada. |
| Inyección manual sin framework | Sin dependencias, pero el ensamblado crece mucho con siete módulos. |
| MVI con una biblioteca (Orbit, MVIKotlin) | Añade otra dependencia que mantener; la estructura propuesta da el mismo beneficio. |
| ViewModel sin estructura de acciones y estado único | Cada pantalla se organiza distinto y las pruebas se vuelven irregulares. |

## Consecuencias

- Los errores de configuración de Koin aparecen en ejecución; la prueba del grafo es obligatoria para evitarlo.
- La estructura de acciones y estado agrega algo de código repetitivo por pantalla, a cambio de pruebas simples: dada una acción, se comprueba el estado resultante.
- Las reglas de Konsist y detekt hay que escribirlas y mantenerlas; sin ellas la regla de dependencias depende solo de la revisión humana.
- El tiempo de CI aumenta con el trabajo de iOS; se mitiga con caché de Gradle y de Kotlin/Native.

## Preguntas abiertas

- ¿Los minutos de GitHub Actions en macOS son gratuitos para este repositorio? Hay que verificarlo.
- ¿Qué reglas de detekt se activan al inicio y con qué umbrales?
- ¿Se exige un porcentaje mínimo de cobertura de pruebas en el dominio?
