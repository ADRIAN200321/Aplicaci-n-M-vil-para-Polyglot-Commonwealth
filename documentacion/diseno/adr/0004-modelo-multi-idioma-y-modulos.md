# ADR-0004: Modelo multi-idioma y módulos por contexto

- **Estado:** Propuesta
- **Fecha:** 2026-10-30
- **Autor:** Juan Santiago Guerrero

## Contexto

La visión del proyecto se amplió: además de informar e inscribir (alcance del SRS v1.0), la aplicación será una plataforma de aprendizaje de idiomas con ruta de lecciones por nivel, ejercicios y evaluaciones, progreso del estudiante y clases en vivo, similar a Open English pero para los seis idiomas de la academia (inglés, francés, italiano, portugués, español ELE y alemán).

Este cambio de alcance todavía no está reflejado en el SRS. Antes de construir hay que actualizarlo (versión 2.0) y acordarlo con el equipo y con la instructora.

## Decisión

### 1. Dominio genérico por idioma

El dominio no contiene reglas ligadas a un idioma concreto. La jerarquía del contenido es:

`Idioma → Curso → Nivel (A1 a C2) → Lección → Ejercicio`

Agregar un idioma nuevo debe ser agregar contenido, no programar.

### 2. Dos conceptos de idioma separados

- **Idioma que se aprende:** uno de los programas de la academia.
- **Idioma de la interfaz:** español, inglés, francés o portugués (RF-13).

Son tipos distintos en el código y nunca se mezclan.

### 3. Módulos por contexto

| Módulo | Responsabilidad |
| --- | --- |
| `identidad` | Registro, sesión y roles |
| `catalogo` | Idiomas, cursos, niveles, lecciones y ejercicios |
| `aprendizaje` | Progreso, evaluaciones, test de nivel y certificados |
| `clases-en-vivo` | Agenda, reservas y sesiones (ver ADR-0005) |
| `matricula` | Ficha de inscripción y paso a estudiante |
| `contenido-admin` | Carga de lecciones y multimedia por el Administrador |
| `notificaciones` | Recordatorios y promociones |

Cada módulo es un módulo Gradle propio y se divide en `domain`, `data` y `presentation`. La regla de dependencia es `presentation → domain ← data`: `domain` es Kotlin puro y no conoce Android, iOS, Ktor ni la base de datos.

### 4. Ejercicios extensibles

Cada tipo de ejercicio (opción múltiple, completar frases, asociar, audio) implementa la misma interfaz. Agregar un tipo nuevo no modifica los existentes (principio abierto/cerrado).

### 5. Contenido administrable

El Administrador crea y edita lecciones y ejercicios desde el panel, sin tocar código.

## Alternativas consideradas

| Alternativa | Por qué no |
| --- | --- |
| Un solo módulo con paquetes por capa | Nada impide que una función dependa de otra; el compilador no protege las fronteras. |
| Un módulo por capa (uno `domain`, uno `data`) | Acopla todas las funciones entre sí y vuelve cada cambio global. |
| Un módulo o código por idioma | Duplica lógica y obliga a programar cada idioma nuevo. |

## Consecuencias

- Los módulos pueden desarrollarse en paralelo por los tres integrantes con pocos conflictos.
- Hay más configuración inicial (varios módulos Gradle) y una prueba de arquitectura debe comprobar que se respetan las dependencias.
- El contenido pasa a ser datos y no código: hace falta un modelo de datos para lecciones y ejercicios, y herramientas de carga en el panel.
- El volumen de contenido real (seis idiomas por niveles A1 a C2) es el principal riesgo de plazo, más que el código.

## Preguntas abiertas

- ¿En qué formato se guarda el contenido de las lecciones: base de datos, JSON versionado o ambos?
- ¿Quién redacta y revisa el contenido pedagógico de cada idioma?
- ¿Qué tipos de ejercicio entran en la primera fase?
- ¿Cómo se califica: automáticamente, por el docente o de forma mixta?
- ¿Cuáles de estos módulos se construyen primero? Propuesta: `identidad`, `matricula`, `catalogo` y `aprendizaje`, y después `clases-en-vivo`.
