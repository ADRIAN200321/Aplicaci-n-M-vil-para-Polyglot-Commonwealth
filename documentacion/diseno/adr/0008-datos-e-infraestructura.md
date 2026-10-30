# ADR-0008: Datos e infraestructura: Docker, Exposed, Flyway y SQLDelight

- **Estado:** Propuesta
- **Fecha:** 2026-10-30
- **Autor:** Juan Santiago Guerrero

## Contexto

El backend en Ktor corre en la JVM y el hosting compartido actual de la academia no la ejecuta. El equipo asume los costos técnicos al inicio y la academia los asumirá después, por lo que conviene gastar poco ahora y poder entregar el sistema sin rehacerlo.

La app necesita guardar algo de información en el dispositivo: el borrador de la ficha para no perder lo escrito si falla la conexión (CU-01, flujo 5a, y RNF-11) y datos que se consultan seguido.

## Decisión

### 1. Docker y configuración por variables de entorno

El backend y PostgreSQL se empaquetan con Docker. Toda la configuración (conexión a la base, claves, correo) se lee de variables de entorno. El repositorio incluye un `docker-compose.yml` para desarrollo, sin secretos.

### 2. Despliegue por fases

| Fase | Dónde corre | Costo |
| --- | --- | --- |
| 0. Desarrollo | Computadores del equipo con `docker-compose` | Ninguno |
| 1. Pruebas con otras personas | Un VPS pequeño, con Ktor, PostgreSQL y un proxy inverso con HTTPS automático (por ejemplo Caddy) | Bajo y fijo, a cargo del equipo |
| 2. Producción | El mismo despliegue en una cuenta a nombre de la academia | A cargo de la academia |

- El entorno de pruebas es independiente del de producción: nunca se prueba sobre datos reales.
- El despliegue se automatiza con GitHub Actions al fusionar a `main`.
- Copias de seguridad automáticas y diarias de la base de datos, guardadas fuera del servidor.
- Firewall activo y actualizaciones de seguridad del sistema.

### 3. Acceso a datos en el backend: Exposed

Ktor accede a PostgreSQL con **Exposed**. Las tablas y los modelos de Exposed viven solo en `data` y nunca salen de esa capa. El dominio solo conoce las interfaces de repositorio.

### 4. Esquema con migraciones: Flyway

El esquema se maneja con migraciones SQL versionadas en `database/migrations/`, revisadas en Pull Request. No se crean ni modifican tablas de forma automática desde el código.

### 5. Almacenamiento local en la app: SQLDelight

La app usa **SQLDelight** para el almacenamiento local. Alcance inicial:

- El borrador de la ficha de inscripción.
- Una caché de lectura del catálogo y del perfil.

El modo sin conexión completo queda para una fase posterior. Los tokens de sesión **no** se guardan aquí: usan el almacenamiento seguro del ADR-0007.

### 6. Costos y cuentas

- Los costos se retrasan hasta que haga falta: la cuenta de Apple Developer se paga cuando haya que probar en iPhone reales o publicar, y la asume la academia.
- Las cuentas que son del negocio (dominio, Apple Developer, Google Play) se registran **a nombre de la academia**. Los costos de infraestructura se quedan con el equipo hasta el traspaso.

## Alternativas consideradas

| Alternativa | Por qué no |
| --- | --- |
| Plataforma administrada (Railway, Render, Fly.io) | Menos operación, pero el costo sube con el uso y los servidores suelen estar fuera de Colombia. Se puede usar más adelante, porque el despliegue con Docker es portable. |
| Nube grande (AWS, Google Cloud) | Excesiva para el tamaño del proyecto. |
| jOOQ | Exige configurar generación de código y está más orientada a Java. |
| Room para multiplataforma | Viable, pero más reciente que SQLDelight en iOS. |
| Creación automática de tablas desde el código | Los cambios de esquema quedan sin revisión ni historial. |

## Consecuencias

- El traspaso a la academia consiste en volver a desplegar con Docker en su cuenta y mover la base con una copia (`pg_dump`).
- El equipo asume la operación del servidor (copias, actualizaciones, seguridad) mientras dure la fase 1.
- Dos bases de datos distintas conviven: PostgreSQL en el servidor y SQLite (vía SQLDelight) en el dispositivo. Ninguna de las dos se expone fuera de su capa `data`.
- Cada cambio de esquema exige una migración nueva.

## Preguntas abiertas

Pendientes de verificar con precios y condiciones vigentes:

- ¿Qué proveedor y plan de VPS se usa en la fase 1, y cuánto cuesta?
- ¿Dónde queda físicamente el servidor? La Ley 1581 pone condiciones a la transferencia de datos personales a otros países; hay que confirmarlo con la academia.
- ¿Cuánto tiempo se conservan las copias de seguridad y cómo se prueban las restauraciones?
- ¿Quién administra el servidor después del traspaso?
- ¿Los minutos de GitHub Actions en macOS son gratuitos para este repositorio?
- ¿Cuánto cuestan hoy la cuenta de Apple Developer y la de Google Play?
- ¿Qué proveedor de correo tiene un nivel gratuito suficiente para el volumen esperado?
