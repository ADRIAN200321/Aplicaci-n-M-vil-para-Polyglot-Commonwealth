# ADR-0001: Plataforma móvil: Kotlin Multiplatform

- **Estado:** Propuesta
- **Fecha:** 2026-10-30
- **Autor:** Juan Santiago Guerrero

## Contexto

El proyecto necesita una aplicación móvil para aspirantes y estudiantes (RF-01 a RF-20). El SRS no fija la plataforma. El equipo trabajará en Kotlin y quiere una arquitectura limpia con principios SOLID. El proyecto dura ocho semanas, por lo que el alcance debe ser realista.

## Decisión

La aplicación móvil se construye con **Kotlin Multiplatform (KMP)**. La lógica de negocio (dominio) y el acceso a datos se escriben una sola vez en código común y se comparten entre Android e iOS.

## Alternativas consideradas

| Alternativa | Por qué no |
| --- | --- |
| Android nativo con Jetpack Compose | Más simple, pero deja fuera iOS y no reutiliza el dominio. |
| Flutter o React Native | Salen del lenguaje elegido por el equipo (Kotlin). |

## Consecuencias

- El dominio y los casos de uso viven en código común Kotlin puro, sin dependencias de Android ni iOS. Esto refuerza la regla de dependencias de Clean Architecture.
- Se necesitan bibliotecas compatibles con KMP para red, almacenamiento local, inyección de dependencias y serialización.
- Compilar y probar en iOS exige macOS. Se resuelve con CI en macOS y pruebas en iPhone, como se detalla en el ADR-0006.
- La configuración inicial es más compleja que la de un proyecto solo Android.

## Preguntas abiertas

- ¿Qué biblioteca de inyección de dependencias se usa (por ejemplo, Koin)?
- Interfaz e iOS: propuestas en el ADR-0006 (Compose Multiplatform, Android e iOS desde el inicio).
