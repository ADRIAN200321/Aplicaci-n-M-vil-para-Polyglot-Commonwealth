# ADR-0006: Interfaz con Compose Multiplatform, Android e iOS desde el inicio

- **Estado:** Propuesta
- **Fecha:** 2026-10-30
- **Autor:** Juan Santiago Guerrero

## Contexto

Los usuarios de la academia se reparten entre iPhone y Android. Lanzar solo en uno deja fuera a una parte importante del público, justo en una aplicación pensada para captar aspirantes (PR-01).

El equipo desarrolla en Windows. Un integrante usa Android y dos usan iPhone. Hoy no hay un Mac confirmado, y compilar para iOS exige macOS.

## Decisión

- La interfaz se escribe con **Compose Multiplatform**: una sola interfaz en Kotlin para Android e iOS. Las capas `presentation`, `domain` y `data` de cada módulo viven en código común.
- **Android e iOS se desarrollan desde el inicio**, no uno después del otro.
- La compilación de iOS se verifica en **CI con un runner de macOS** en cada Pull Request, para que nadie dependa de tener un Mac en el trabajo diario.
- Las versiones de prueba de iOS se distribuyen por **TestFlight** a los integrantes con iPhone.
- Lo que depende de la plataforma (notificaciones, permisos, SDK de video de las clases en vivo) se aísla detrás de interfaces del dominio, con implementaciones por plataforma. El código común no usa APIs de Android ni de iOS.

## Alternativas consideradas

| Alternativa | Por qué no |
| --- | --- |
| Interfaz nativa por plataforma (Compose en Android, SwiftUI en iOS) | Obliga a aprender Swift y a mantener cada pantalla dos veces. |
| Android primero e iOS después | Pierde a los usuarios de iPhone al inicio y deja los problemas de iOS para el final. |
| Solo el sitio web como alternativa para iPhone | Menor integración con el dispositivo y sin presencia en la tienda. Sirve como apoyo mientras tanto, no como sustituto. |

## Consecuencias

- Cada función se prueba en dos sistemas. Reparto natural: Android en el equipo con Android y iPhone en los equipos con iPhone.
- Hace falta una cuenta de Apple Developer Program (de pago, anual) para TestFlight y para publicar en la App Store.
- En Windows las pruebas diarias corren en Android. Los errores propios de iOS se detectan en CI y en las pruebas en iPhone, por lo que conviene configurar la CI de macOS desde el primer día.
- El proveedor de video de las clases en vivo debe tener SDK para iOS (ver ADR-0005).

## Preguntas abiertas

- ¿Hay acceso a un Mac, propio o prestado, para depurar problemas de iOS que la CI no muestre?
- Cuenta de Apple Developer: se difiere hasta que haga falta probar en iPhone reales o publicar, y la asume la academia a su nombre (ADR-0008).
- ¿Los minutos de CI en macOS son gratuitos para este repositorio? Hay que verificarlo, porque puede depender de si el repo es público o privado.
