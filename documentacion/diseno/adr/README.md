# Registros de decisiones de arquitectura (ADR)

Cada decisión técnica importante del proyecto se documenta en un archivo corto, numerado y que no se reescribe: si una decisión cambia, se crea un ADR nuevo que reemplaza al anterior.

## Índice

| ADR | Decisión | Estado |
| --- | --- | --- |
| [0001](0001-plataforma-movil-kotlin-multiplatform.md) | Plataforma móvil: Kotlin Multiplatform | Propuesta |
| [0002](0002-backend-ktor.md) | Backend: Ktor | Propuesta |
| [0003](0003-base-de-datos-postgresql.md) | Base de datos: PostgreSQL | Propuesta |
| [0004](0004-modelo-multi-idioma-y-modulos.md) | Modelo multi-idioma y módulos por contexto | Propuesta |
| [0005](0005-clases-en-vivo-con-proveedor-externo.md) | Clases en vivo con proveedor externo | Propuesta |
| [0006](0006-interfaz-compose-multiplatform-android-e-ios.md) | Interfaz con Compose Multiplatform, Android e iOS desde el inicio | Propuesta |
| [0007](0007-autenticacion-jwt-y-registro-por-etapas.md) | Autenticación con JWT y registro por etapas | Propuesta |

> Los ADR 0004 y 0005 asumen la visión ampliada del proyecto (plataforma de aprendizaje con clases en vivo), que aún no está en el SRS. Antes de construir hay que actualizar el SRS a la versión 2.0.

## Estados

- **Propuesta:** escrita, pendiente de revisión del equipo.
- **Aceptada:** el equipo la aprobó; es la referencia para el código.
- **Reemplazada por ADR-XXXX:** ya no aplica.

## Formato

Cada ADR tiene estas secciones: Contexto, Decisión, Alternativas consideradas, Consecuencias y Preguntas abiertas.
