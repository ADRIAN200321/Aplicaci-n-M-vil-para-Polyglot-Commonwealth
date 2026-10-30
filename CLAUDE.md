# Polyglot Project: reglas para agentes y colaboradores

Aplicación móvil de **Polyglot Commonwealth Language & Culture** (academia de idiomas de Ocaña). Proyecto formativo del SENA (ADSO). Lee el [README](README.md) y los [ADR](documentacion/diseno/adr/README.md) antes de proponer cambios.

## Estado de las decisiones

Las decisiones de arquitectura viven en `documentacion/diseno/adr/`. Mientras un ADR esté en estado **Propuesta**, es la dirección a seguir, pero no se amplía ni se contradice sin avisar.

- Kotlin Multiplatform, Ktor y PostgreSQL (ADR-0001 a 0003).
- Dominio multi-idioma y módulos por contexto (ADR-0004).
- Clases en vivo con proveedor externo (ADR-0005).
- Interfaz con Compose Multiplatform, Android e iOS desde el inicio (ADR-0006). El código común no usa APIs de Android ni de iOS.
- Autenticación con JWT y token de refresco, Argon2id y registro por etapas (ADR-0007).
- Docker, Exposed, Flyway y SQLDelight, con despliegue por fases para gastar poco al inicio (ADR-0008).
- Koin, MVVM con flujo unidireccional y CI en GitHub Actions (ADR-0009).

**No elijas por tu cuenta** lo que los ADR dejan abierto: servicio de correo, proveedor de video, proveedor del servidor. Si el trabajo lo necesita, propón un ADR nuevo y pregunta.

## Arquitectura

Clean Architecture, un módulo Gradle por contexto (`identidad`, `catalogo`, `aprendizaje`, `clases-en-vivo`, `matricula`, `contenido-admin`, `notificaciones`). Cada módulo se divide en tres capas:

```
presentation → domain ← data
```

- **`domain`**: Kotlin puro. Entidades, casos de uso e interfaces de repositorio. **No importa** Android, iOS, Ktor, SQL, JSON ni ninguna biblioteca de framework.
- **`data`**: implementa las interfaces del dominio (API, base de datos, mappers). Nunca expone sus modelos (DTO, entidades de base de datos) fuera de la capa.
- **`presentation`**: pantallas y ViewModels. Solo habla con casos de uso, nunca con repositorios ni con `data`.
- Un módulo no depende de las capas internas de otro. Se comunican por interfaces públicas del dominio.
- Dependencias siempre hacia el dominio. Si necesitas lo contrario, falta una interfaz.
- Inyección por **constructor**. Koin se usa solo en el punto de arranque (`app`); ninguna clase llama al contenedor (`get()`, `inject()`) desde dentro.
- Cada pantalla tiene un `UiState` inmutable y recibe las acciones del usuario con `onAction(...)` (jerarquía `sealed`). Los efectos de una sola vez salen por un `Flow` aparte.

## Principios SOLID en este proyecto

| Principio | Cómo se aplica aquí |
| --- | --- |
| Responsabilidad única | Un caso de uso por acción (`EnviarFichaInscripcion`, `MatricularAspirante`). Una clase, una razón para cambiar. |
| Abierto/cerrado | Los tipos de ejercicio implementan una interfaz común. Agregar uno no modifica los existentes. |
| Sustitución de Liskov | Toda implementación respeta el contrato de su interfaz, incluidos errores y casos vacíos. |
| Segregación de interfaces | Interfaces pequeñas y específicas por rol (lectura y escritura separadas si hace falta), no una interfaz gigante por módulo. |
| Inversión de dependencias | El dominio define las interfaces (`VideoSessionGateway`, repositorios); `data` las implementa. Se inyectan, no se instancian dentro de las clases. |

Las reglas de negocio viven en `domain`. Ejemplo: "un menor exige representante legal" se valida allí, no en la pantalla ni en la ruta del backend.

## Lenguaje de dominio

- Idioma que se **aprende** (`Idioma`) e idioma de la **interfaz** (`IdiomaInterfaz`) son tipos distintos. No los mezcles.
- Ninguna regla de negocio nombra un idioma concreto. Agregar uno es agregar contenido.
- Nombres del dominio en español, como en el SRS (`Inscripcion`, `Estudiante`, `Matricula`). Nombres técnicos y de capas (`Repository`, `UseCase`, `ViewModel`) en inglés, como es habitual en Kotlin.

## Buenas prácticas de código

- Kotlin idiomático: `val` sobre `var`, clases de datos y `sealed` para estados, sin `!!` ni excepciones para control de flujo.
- Errores esperados como resultados tipados (`Result` o jerarquía `sealed`), no como excepciones sueltas.
- Funciones cortas y con nombres que dicen qué hacen. Comentarios solo para explicar el porqué.
- Sin código muerto, sin `TODO` sin responsable, sin valores mágicos: constantes con nombre.
- Concurrencia con corrutinas y `Flow`; nada de hilos manuales.
- Dependencias nuevas solo con justificación; que sean compatibles con KMP cuando vayan en código común.

## Pruebas

- Todo caso de uso y toda regla de dominio lleva pruebas unitarias (Given/When/Then en el nombre o en la estructura).
- Las pruebas de dominio no usan base de datos, red ni Android: se prueban con dobles de las interfaces.
- Un error corregido lleva una prueba que lo reproduce.
- Se usan **datos ficticios**, nunca datos reales de estudiantes (RNF-05).
- Cuando exista el proyecto Gradle se agregará una prueba de arquitectura que falle si una capa rompe la regla de dependencias.

## Seguridad y datos personales

- No se suben contraseñas, claves, tokens ni archivos `.env`. Las credenciales van en variables de entorno y se excluyen con `.gitignore`.
- Contraseñas siempre cifradas (hash con sal), nunca en texto plano ni en registros.
- El rol se comprueba **en el servidor** en cada operación de gestión, no solo en la app.
- Los datos personales se tratan según la Ley 1581 de 2012: consentimiento previo y datos de menores con su representante legal. No registres datos personales en logs.

## Git y colaboración

- Nunca se trabaja directo en `main`. Una rama por tarea, con el formato `<Nombre>/<Iniciales>_<tarea>`. Ejemplo: `Santiago/SG_esqueleto-modulos`. La tarea va en minúsculas y con guiones.
- Commits con **Conventional Commits** en español: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`.
- Commits pequeños, uno por cambio lógico.
- **Prohibido** agregar `Co-Authored-By` de Claude o de cualquier IA, y cualquier línea de atribución como "Generated with Claude Code", en commits, PR y documentos. La autoría es del equipo humano.
- Todo cambio entra por Pull Request con al menos una revisión de un compañero.
- No se reescribe historia compartida (`push --force`) sin acuerdo del equipo.
- Antes de empezar, `git pull` de `main` y de tu rama.

## Documentación

- Una decisión de arquitectura nueva es un ADR nuevo en `documentacion/diseno/adr/`. Los ADR no se reescriben: si cambian, se crea otro que reemplaza al anterior.
- Si un cambio contradice el SRS o un ADR, se detiene y se pregunta.
- El README se mantiene al día con cómo ejecutar el proyecto cuando exista.

## Qué hace un agente antes de dar algo por terminado

1. El cambio respeta la regla de dependencias y no mezcla responsabilidades.
2. Hay pruebas que pasan y cubren el comportamiento nuevo.
3. No hay secretos, datos reales ni atribución a IA en el commit.
4. La documentación afectada está actualizada.
5. Se informa con honestidad qué se verificó y qué no.

## Comandos

Aún no hay proyecto Gradle ni scripts de compilación. Esta sección se completa cuando se cree el esqueleto de módulos.
