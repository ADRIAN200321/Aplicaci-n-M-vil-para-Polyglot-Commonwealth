# ADR-0010: Esqueleto andante y estructura de módulos

- **Estado:** Propuesta
- **Fecha:** 2026-10-30
- **Autor:** Juan Santiago Guerrero

## Contexto

Los ADR 0001 a 0009 definen herramientas y reglas, pero ninguna línea de código las ha puesto a prueba juntas. Antes de repartir trabajo entre tres personas conviene un **esqueleto andante**: una sola funcionalidad completa, de la pantalla a la base de datos, que atraviese todas las capas y herramientas. Se eligió la **ficha de inscripción** del contexto `matricula`, porque ya tiene reglas de negocio reales (menor de edad, representante legal, consentimiento de datos personales).

Al construirlo hubo que decidir cosas que los ADR anteriores no fijaban.

## Decisión

### 1. Un submódulo de Gradle por capa

Cada contexto se divide en `domain`, `data` y `presentation` (por ejemplo `:matricula:domain`). Gradle impide por sí mismo que `domain` vea `data`, porque no la declara como dependencia. Es una barrera más fuerte que una convención dentro de un solo módulo.

### 2. La validación vive en el dominio y la usan cliente y servidor

`ValidadorFicha` es Kotlin puro y se ejecuta en la app (para avisar pronto al usuario) y en el servidor (porque no se confía en el cliente). Devuelve **todos** los errores juntos, no solo el primero. La pantalla y la ruta HTTP no contienen reglas de negocio.

### 3. Los DTO de red se duplican a propósito

El cliente (`matricula:data`) y el servidor (`servidor/api`) declaran cada uno sus propios DTO. El contrato es el JSON de `POST /api/v1/inscripciones`, no una clase compartida. Así el servidor puede evolucionar sin recompilar la app y ninguno expone sus modelos internos.

### 4. Ktor y las bibliotecas de red quedan ocultas tras `data`

`app` no depende de Ktor: llama a una función de fábrica (`crearRepositorioInscripciones(urlApi)`) que entrega la interfaz del dominio. La presentación nunca conoce la implementación.

### 5. Configuración solo por variables de entorno

El servidor lee `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` y `PORT`. La app de Android recibe la URL del servidor con `-PurlApi` al compilar. El tráfico HTTP sin cifrar solo se permite en el manifiesto de depuración.

### 6. Migraciones con Flyway desde `database/migrations/`

El servidor las copia al *classpath* y las aplica al arrancar. El esquema no lo crea Exposed.

### 7. Verificación automática de las reglas

- Una prueba con **Konsist** (`:pruebas-arquitectura`) comprueba la regla de dependencias, el uso de Koin solo en el arranque y la ausencia de `!!`.
- **ktlint** valida el formato, con `.editorconfig`.
- La CI en GitHub Actions ejecuta todo en Linux y las pruebas y el enlazado de iOS en macOS.

## Alternativas consideradas

- **Un módulo por contexto con carpetas por capa.** Más simple, pero nada impide un `import` incorrecto salvo la revisión humana.
- **DTO compartidos en un módulo común.** Menos código, pero acopla los despliegues del cliente y del servidor.
- **Dejar que `app` use Ktor directamente.** Más corto, pero filtra la red hacia arriba y rompe la regla de dependencias.
- **Dejar la funcionalidad para después y empezar por el catálogo.** Retrasa descubrir los problemas de integración.

## Consecuencias

- Más archivos de configuración de Gradle por contexto (tres en lugar de uno).
- Un cambio en las reglas de validación se recompila en cliente y servidor a la vez, lo que es deseable.
- El idioma de interés es por ahora un texto libre (`IdiomaId`). Cuando exista el módulo `catalogo` se reemplazará por una referencia real.
- Se escribe y mantiene un conjunto de reglas de Konsist (ADR-0009 ya lo advertía).

## Preguntas abiertas

- **detekt:** ADR-0009 lo incluye, pero aún no se ha comprobado su compatibilidad con la versión de Kotlin del proyecto. Queda fuera de la CI hasta confirmarla.
- **Envoltorio de Xcode para iOS** (`iosApp`): pendiente. Requiere un Mac; hasta entonces iOS se verifica con las pruebas y el enlazado del framework en la CI de macOS.
- Dockerfile del servidor y despliegue, según la fase indicada en ADR-0008.
