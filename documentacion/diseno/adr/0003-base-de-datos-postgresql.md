# ADR-0003: Base de datos: PostgreSQL

- **Estado:** Propuesta
- **Fecha:** 2026-10-30
- **Autor:** Juan Santiago Guerrero

## Contexto

El sistema guarda inscripciones, estudiantes, certificados con número único consecutivo (RNF-12), ciclos de pago y archivos multimedia (RNF-13). Los listados del panel deben soportar cientos de registros con búsqueda y filtro (RNF-09). Las contraseñas se guardan cifradas (RNF-01). El README prevé un script SQL y un modelo de datos en `database/`.

## Decisión

Se usa **PostgreSQL** como base de datos relacional del backend.

## Alternativas consideradas

| Alternativa | Por qué no |
| --- | --- |
| MySQL / MariaDB | Válida, pero PostgreSQL ofrece más restricciones de integridad y mejor soporte de búsqueda sobre texto. |
| Conservar el motor que ya usa el panel | Aún no sabemos cuál es; se revisa en las preguntas abiertas. |

## Consecuencias

- Las reglas de integridad (llaves únicas, llaves foráneas, secuencias para el número de certificado) se apoyan en la base de datos.
- El acceso desde Ktor se hace con Exposed (ADR-0008), siempre detrás de interfaces de repositorio del dominio.
- Las credenciales de conexión van en variables de entorno y nunca en el repositorio (RNF-03).
- Las pruebas usan datos ficticios (RNF-05).

## Preguntas abiertas

- ¿Qué motor usa hoy el panel de la academia? Si es distinto, ¿se migra o se integra?
- Alojamiento y biblioteca de acceso: propuestos en el ADR-0008 (Docker, Exposed y Flyway).
