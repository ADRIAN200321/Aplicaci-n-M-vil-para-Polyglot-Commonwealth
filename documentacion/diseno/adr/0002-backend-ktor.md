# ADR-0002: Backend: Ktor

- **Estado:** Propuesta
- **Fecha:** 2026-10-30
- **Autor:** Juan Santiago Guerrero

## Contexto

La aplicación necesita una API que reciba las fichas de inscripción y las deje disponibles en Inscripciones (Leads) del panel (RF-10, RF-23 a RF-26), autentique usuarios (RF-15, RF-21) y compruebe el rol en cada operación (RF-20, RNF-02). Las comunicaciones deben usar HTTPS (RNF-03) y los datos personales, incluidos los de menores, se tratan según la Ley 1581 de 2012 (RNF-04).

## Decisión

El backend se construye con **Ktor** en Kotlin, como API REST.

### Relación con el panel existente: convivencia por etapas

Del sitio público actual se observa que está en Hostinger (HTML y JavaScript estático) y que la inscripción se envía a un script PHP (`backend/procesar_inscripcion.php`). El panel administrativo no es visible desde fuera.

- Ktor y PostgreSQL son el núcleo para lo **nuevo**: identidad, catálogo, aprendizaje y clases en vivo.
- El código y el esquema heredados **no se reutilizan** como base. Se aíslan detrás de una interfaz del dominio (por ejemplo `InscripcionesPanelGateway`) con un adaptador que entrega las inscripciones al panel actual (RF-10).
- Cuando exista un panel nuevo, se reemplaza solo el adaptador, sin tocar casos de uso ni pantallas.

## Alternativas consideradas

| Alternativa | Por qué no |
| --- | --- |
| Spring Boot (Kotlin) | Más herramientas incluidas, pero más pesado y con más comportamiento implícito por anotaciones. |
| Usar solo la API del panel existente | Depende de un sistema que no controlamos y cuyo funcionamiento interno aún no está documentado. |

## Consecuencias

- Un solo lenguaje en app y backend; es posible compartir un módulo de contrato (modelos de petición y respuesta) entre ambos.
- Ktor es liviano y deja a nosotros la estructura, lo que encaja con Clean Architecture: rutas → casos de uso → repositorios.
- Seguridad, validación y manejo de errores se configuran con plugins explícitos; hay que escribirlos y probarlos, no vienen por defecto.
- Durante la transición hay dos fuentes de datos: el panel actual y la base nueva. El adaptador debe manejar errores y reintentos para no perder inscripciones.
- Ktor corre en la JVM y el hosting compartido de Hostinger normalmente no la ejecuta: el backend necesitará un VPS o un servicio en la nube.

## Preguntas abiertas

- Pendientes de confirmar con la academia (no verificables desde fuera):
  - ¿Dónde está el código del panel y quién tiene acceso?
  - ¿Qué motor de base de datos usa y se puede conectar desde fuera del hosting?
  - ¿`procesar_inscripcion.php` guarda en la base de datos o solo arma el mensaje de WhatsApp?
  - ¿El plan de Hostinger es compartido o un VPS?
- Autenticación: propuesta en el ADR-0007 (JWT con token de refresco).
