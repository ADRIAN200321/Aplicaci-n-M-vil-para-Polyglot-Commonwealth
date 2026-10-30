# ADR-0002: Backend: Ktor

- **Estado:** Propuesta
- **Fecha:** 2026-10-30
- **Autor:** Juan Santiago Guerrero

## Contexto

La aplicación necesita una API que reciba las fichas de inscripción y las deje disponibles en Inscripciones (Leads) del panel (RF-10, RF-23 a RF-26), autentique usuarios (RF-15, RF-21) y compruebe el rol en cada operación (RF-20, RNF-02). Las comunicaciones deben usar HTTPS (RNF-03) y los datos personales, incluidos los de menores, se tratan según la Ley 1581 de 2012 (RNF-04).

## Decisión

El backend se construye con **Ktor** en Kotlin, como API REST.

## Alternativas consideradas

| Alternativa | Por qué no |
| --- | --- |
| Spring Boot (Kotlin) | Más herramientas incluidas, pero más pesado y con más comportamiento implícito por anotaciones. |
| Usar solo la API del panel existente | Depende de un sistema que no controlamos y cuyo funcionamiento interno aún no está documentado. |

## Consecuencias

- Un solo lenguaje en app y backend; es posible compartir un módulo de contrato (modelos de petición y respuesta) entre ambos.
- Ktor es liviano y deja a nosotros la estructura, lo que encaja con Clean Architecture: rutas → casos de uso → repositorios.
- Seguridad, validación y manejo de errores se configuran con plugins explícitos; hay que escribirlos y probarlos, no vienen por defecto.
- Hay que decidir cómo convive el backend con el panel y el sitio actuales.

## Preguntas abiertas

- ¿Con qué tecnología está hecho el panel actual y cómo lee y escribe sus datos?
- ¿El backend nuevo reemplaza al del panel, lo complementa o comparte con él la base de datos?
- ¿Cómo se autentican los usuarios: sesión, JWT u otro mecanismo?
