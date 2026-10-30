# ADR-0005: Clases en vivo con proveedor externo

- **Estado:** Propuesta
- **Fecha:** 2026-10-30
- **Autor:** Juan Santiago Guerrero

## Contexto

Las clases en vivo estaban fuera del alcance del SRS v1.0 y el rol de Docente también. La visión ampliada (ver ADR-0004) las incluye. Una clase en vivo exige agenda, reservas, un docente y video en tiempo real, con latencia baja, audio estable y, posiblemente, grabación.

Entre los participantes habrá menores de edad. Su imagen y su voz son datos personales protegidos por la Ley 1581 de 2012 (RNF-04).

## Decisión

El video en vivo **no se construye**: se usa un proveedor externo. El dominio define una interfaz y el proveedor concreto es un detalle de la capa `data`:

- En `clases-en-vivo/domain`: una interfaz (por ejemplo `VideoSessionGateway`) con operaciones como crear sala, emitir acceso para un participante y cerrar la sesión.
- En `clases-en-vivo/data`: un adaptador para el proveedor elegido.
- El resto del sistema (agenda, reservas, asistencia) depende solo de la interfaz.

Esto aplica la inversión de dependencias: cambiar de proveedor implica escribir otro adaptador, sin tocar casos de uso ni pantallas.

El proveedor concreto se decide más adelante con estos criterios:

- Costo por minuto o por participante y existencia de plan gratuito para desarrollo.
- SDK para Android y compatibilidad con Kotlin Multiplatform.
- Grabación, con control de quién la ve y por cuánto tiempo.
- Tratamiento y ubicación de los datos de menores.

## Alternativas consideradas

| Alternativa | Por qué no |
| --- | --- |
| Construir la infraestructura de video propia | Muy costosa y riesgosa para un equipo de tres. |
| Solo abrir un enlace externo (Meet, Zoom) | Es la opción más barata y puede servir como primera versión detrás de la misma interfaz, pero no integra agenda ni asistencia. |

## Consecuencias

- Aparece un costo recurrente y una dependencia de un tercero.
- Se necesita el rol **Docente**, que el SRS v1.0 excluía; el SRS v2.0 debe incluirlo.
- Hace falta un aviso de consentimiento específico para la grabación y el tratamiento de video, con autorización del representante legal en el caso de menores.
- Se construye después del módulo de aprendizaje. Como primera entrega puede bastar un adaptador que abra un enlace externo.

## Preguntas abiertas

- ¿Qué proveedor se usa y con qué presupuesto?
- ¿Las clases se graban? Si es así, ¿quién accede y por cuánto tiempo?
- ¿Cuántos participantes máximo por clase?
- ¿Los docentes de la academia usarán la app o su propia herramienta?
