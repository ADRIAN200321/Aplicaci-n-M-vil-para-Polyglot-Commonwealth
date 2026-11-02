# ADR-0011: Catálogo de idiomas

- **Estado:** Propuesta
- **Fecha:** 2026-10-31
- **Autor:** Juan Santiago Guerrero

## Contexto

El ADR-0004 pide un dominio multi-idioma en el que agregar un idioma sea agregar contenido, no código. El ADR-0010 dejó el idioma de interés de la ficha como un texto libre (`IdiomaId`) y anunció que el módulo `catalogo` lo reemplazaría por una referencia real.

La academia ofrece seis programas, cada uno con su propio nivel máximo y sus certificaciones:

| Programa | Niveles | Certificaciones |
| --- | --- | --- |
| English | A1-C2 | Cambridge, IELTS, TOEFL, PTE, Duolingo, Business |
| Français | A1-C1 | DELF, DALF |
| Italiano | A1-B2 | CILS |
| Português | A1-B2 | CELPE-Bras |
| Español para extranjeros (ELE) | A1-C2 | DELE |
| Alemán | A1-C1 | TestDaF, Goethe |

Español aparece dos veces en el producto: como idioma que se **aprende** (ELE) y como idioma de la **interfaz**.

## Decisión

### 1. Dos tipos distintos: `Idioma` e `IdiomaInterfaz`

`Idioma` es un programa que se enseña y vive en la base de datos. `IdiomaInterfaz` es el idioma en que la app muestra sus textos y es un `enum` en el código (`ESPANOL`, `INGLES`, `FRANCES`, `PORTUGUES`), porque agregar uno exige traducir textos y eso sí es código. `IdiomaInterfaz.coincideCon(idioma)` es la única unión entre ambos y solo compara códigos ISO. Ninguna regla de negocio nombra un idioma concreto.

### 2. El nivel máximo y las certificaciones son datos del idioma

`Idioma` lleva `nivelMaximo: NivelMcer` y `certificaciones`. Los niveles ofrecidos se calculan (de A1 al máximo). Agregar un idioma, o subirle el nivel, es una migración de datos sin tocar código.

### 3. La clave es el código ISO 639-1

`CodigoIdioma` es una clase de valor con constructor privado y fábrica `desde(texto)` que normaliza (`" EN "` → `en`) y rechaza lo que no sean dos letras. La base de datos repite la restricción (`CHECK`).

### 4. Los idiomas viven en PostgreSQL y los carga Flyway

- `V2__crear_catalogo_idiomas.sql` crea `idiomas` y `certificaciones` y siembra los seis programas.
- `V3__relacionar_inscripciones_con_idiomas.sql` agrega la llave foránea de `inscripciones.idioma_interes` a `idiomas.codigo`. Falla con un mensaje claro si ya hay filas con códigos que no existen, en lugar de adivinar una conversión.
- `V1` no se toca: las migraciones aplicadas son inmutables.

### 5. `matricula` consume el catálogo solo por su dominio

`:matricula:domain` declara `api(project(":catalogo:domain"))`. La ficha guarda un `CodigoIdioma`. `EnviarFichaInscripcion` recibe `ObtenerIdioma` y, tras validar la ficha, comprueba que el idioma exista: si no, devuelve `Invalida([IdiomaInteresNoDisponible])`. Como la comprobación está en el caso de uso, corre igual en la app y en el servidor.

`ValidadorFicha` sigue siendo puro y síncrono (solo valida el formato del código). Por eso los errores del validador y la consulta al catálogo no se combinan en una sola respuesta; con un selector en la pantalla el caso es raro.

Una regla nueva de Konsist hace cumplir esto: un contexto puede importar el dominio de otro, nunca su `data` ni su `presentation`.

### 6. Contrato HTTP de solo lectura y público

`GET /api/v1/idiomas` y `GET /api/v1/idiomas/{codigo}` (404 si no existe). Es público porque la ficha se llena antes de tener cuenta. La escritura del catálogo queda para `contenido-admin`, por eso `IdiomaRepository` solo tiene lectura (segregación de interfaces).

### 7. Pantallas

`catalogo:presentation` ofrece la pantalla **Programas** (niveles y certificaciones por idioma). `matricula:presentation` reemplaza el campo de texto por un selector alimentado con `ListarIdiomas`. La `app` las une con una barra de navegación inferior.

## Alternativas consideradas

- **`Idioma` como `enum` en el código.** Más simple, pero agregar un idioma sería recompilar y publicar la app, y contradice el ADR-0004.
- **Una sola tabla con las certificaciones en una columna de texto.** Menos tablas, pero impide consultar o validar certificaciones por separado.
- **Validar la existencia del idioma solo en el servidor.** Evita una consulta del cliente, pero la regla viviría en la ruta HTTP y no en el dominio.
- **Hacer `ValidadorFicha` suspendible para consultar el catálogo.** Combinaría todos los errores, pero ensucia un validador puro con acceso a datos.
- **Extraer ya un módulo común para `crearClienteHttp`.** Hoy está duplicado en `matricula:data` y `catalogo:data` (dos usos). Se extraerá al aparecer el tercero.

## Consecuencias

- Al enviar la ficha, la app hace una consulta extra al catálogo.
- La CI y las pruebas de arquitectura ahora cubren los módulos de `catalogo`.
- Los nombres de los programas (`English`, `Français`, ...) se muestran como los publica la academia, sin traducirlos.
- El nivel máximo de ELE (C2) y el tema "Business" de English como certificación son datos tomados de la lista de la academia; la web publicada no los detalla del todo y deben confirmarse con ella.
- El ADR-0010, que anunciaba este reemplazo, no se modifica: este ADR lo cumple.

## Preguntas abiertas

- ¿Se muestran los nombres de los programas traducidos al idioma de la interfaz?
- ¿Quién administra el catálogo y cuándo? Depende de `contenido-admin` y de la autenticación (ADR-0007).
