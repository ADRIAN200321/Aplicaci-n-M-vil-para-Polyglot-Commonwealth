# Polyglot Project

Aplicación móvil para **Polyglot Commonwealth Language & Culture**, academia de idiomas de Ocaña (Norte de Santander). Es un sistema nuevo, con su propio servidor y base de datos, que no reutiliza el panel actual de la academia (ADR-0012).

> Proyecto formativo del programa Análisis y Desarrollo de Software (ADSO) del SENA.

## Equipo de desarrollo

| Integrante |
| --- |
| Adrián Camilo Rodríguez Vergel |
| Jacob Sharon Bossem |
| Juan Santiago Guerrero |

## Propósito

Crear una aplicación móvil que permita a quienes quieren aprender un idioma conocer la academia, inscribirse y consultar su información desde el celular, y que permita a la academia recibir y gestionar esa información de forma centralizada.

## Problema que resuelve

| ID | Problema | Efecto |
| --- | --- | --- |
| PR-01 | No hay canal móvil propio | Aspirantes y estudiantes entran solo por navegador y no reciben notificaciones |
| PR-02 | El estudiante no puede consultar su información | Nivel, matrícula, pagos y certificados se gestionan solo desde el panel |
| PR-03 | La captación depende de pasos manuales | La ficha termina en WhatsApp y el Administrador revisa y matricula cada aspirante a mano |

## Objetivo general

Desarrollar una aplicación móvil para Polyglot Commonwealth, con su propio servidor y una sección de gestión para el Administrador, que facilite la inscripción, el aprendizaje y el seguimiento académico de aspirantes y estudiantes, y que dé al Administrador el control completo de la información.

## Actores del sistema

| Actor | Canal | Qué puede hacer |
| --- | --- | --- |
| Visitante | Web y app | Ver programas, ofertas, equipo, ubicación y exámenes; hacer el test de nivel; conversar con Poly |
| Aspirante | Web y app | Todo lo del visitante, más completar la ficha de inscripción y descargar recursos |
| Estudiante | App y web | Consultar recursos, juegos y su información académica; recibir notificaciones |
| Administrador | App, sección de gestión (por rol) | Gestionar inscripciones, estudiantes, certificados, ciclos de pago y multimedia |
| Representante legal | Datos en la ficha | Sus datos son obligatorios al inscribir a un menor de edad |

## Alcance de la primera versión

**Prioridad alta**

- Inicio, programas, test de nivel, contacto y ubicación.
- Ficha de inscripción de tres pasos, con datos del representante legal para menores.
- Inicio de sesión con control de acceso por rol.
- Gestión de inscripciones en la app para el Administrador (listar, descargar PDF, matricular y eliminar).
- Ruta de lecciones y ejercicios por nivel, con su gestión sin programar.
- Seguridad y protección de datos personales (Ley 1581 de 2012), incluida la imagen y la voz en las clases.

**Prioridad media:** recursos de estudio, juegos, asistente Poly, equipo y certificaciones, perfil del estudiante, gestión de estudiantes, certificados y multimedia desde la app, progreso y evaluación de nivel, catálogo de idiomas, y clases en vivo con rol de Docente (agenda, reserva, video y consentimiento).

**Prioridad baja:** cambio de idioma de la interfaz, certificados propios del estudiante, notificaciones, consulta de ciclos de pago, asistencia y recordatorios de clases.

**Fuera de esta versión:** pagos en línea, sedes adicionales y la integración o migración con el panel actual (ADR-0012). La grabación de clases depende de lo que decida la academia.

## Estructura del repositorio

```
Polyglot-Commonwealth/
├── androidApp/            # Aplicación de Android
├── app/                   # Punto de arranque común (Koin, navegación)
├── catalogo/              # Módulo: idiomas y programas (domain, data, presentation)
├── matricula/             # Módulo: ficha de inscripción (domain, data, presentation)
├── servidor/              # Backend con Ktor
├── database/migrations/   # Migraciones SQL de Flyway
├── pruebas-arquitectura/  # Reglas de dependencia entre capas (Konsist)
├── build-logic/           # Plugins de convención de Gradle
└── documentacion/
    ├── requisitos/        # SRS y preguntas a la academia
    └── diseno/adr/        # Decisiones de arquitectura
```

> Los módulos `identidad`, `aprendizaje`, `clases-en-vivo`, `contenido-admin` y `notificaciones` (ADR-0004) se agregan a medida que se construyen.

## Documentación

| Documento | Ubicación |
| --- | --- |
| Especificación de requisitos de software (SRS), v2.0 (vigente) | `documentacion/requisitos/Polyglot_Project_SRS_v2.0.docx` |
| SRS de la Semana 1 (v1.0, reemplazado por la v2.0) | `documentacion/requisitos/Polyglot_Project_SRS_Semana_1.docx` |
| Preguntas pendientes para la academia | [`documentacion/requisitos/preguntas-a-la-academia.md`](documentacion/requisitos/preguntas-a-la-academia.md) |
| Decisiones de arquitectura (ADR) | [`documentacion/diseno/adr/`](documentacion/diseno/adr/README.md) |

## Avance del proyecto

| Semana | Fase | Estado |
| --- | --- | --- |
| 1 | Análisis y requisitos | En revisión |
| 2 | Diseño del sistema | Pendiente |
| 3 | Desarrollo del frontend | Pendiente |
| 4 | Base de datos y backend | Pendiente |
| 5 | Funcionalidades principales | Pendiente |
| 6 | Integración del sistema | Pendiente |
| 7 | Pruebas, calidad y correcciones | Pendiente |
| 8 | Entrega final y sustentación | Pendiente |

## Cómo ejecutar el proyecto

Ya existe un **esqueleto andante** (ADR-0010): la ficha de inscripción de punta a punta, desde la pantalla hasta PostgreSQL. La ficha elige el idioma del **catálogo de idiomas** (ADR-0011), que la migración de Flyway carga en la base de datos y la app muestra en la pantalla *Programas*.

**Requisitos:** JDK 17 o superior, Docker, y el SDK de Android (Android Studio) para la app. Para iOS se necesita un Mac.

1. **Base de datos.** Copia `.env.example` a `.env`, cambia la contraseña y ejecuta:

   ```bash
   docker compose up -d
   ```

2. **Servidor.** Define las variables de entorno (`DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, opcionalmente `PORT`) y ejecuta:

   ```bash
   ./gradlew :servidor:run
   ```

   Las migraciones de Flyway se aplican al arrancar. Comprueba que responde en `http://localhost:8080/salud`.

3. **App de Android.** Crea `local.properties` con `sdk.dir=<ruta del SDK>` y ejecuta:

   ```bash
   ./gradlew :androidApp:installDebug
   ```

   En el emulador, el servidor de tu computador es `http://10.0.2.2:8080` (valor por defecto). En un celular físico usa `-PurlApi=http://<IP-de-tu-PC>:8080`.

4. **Pruebas y calidad:**

   ```bash
   ./gradlew ktlintCheck :catalogo:domain:jvmTest :matricula:domain:jvmTest :pruebas-arquitectura:test :servidor:test
   ```

   Las pruebas de PostgreSQL usan Testcontainers y se omiten si Docker no está disponible. La lista completa de comandos está en [CLAUDE.md](CLAUDE.md).

**iOS:** el código compartido ya compila para iOS y la CI lo comprueba en macOS, pero el proyecto de Xcode (`iosApp`) aún no existe; queda pendiente (ver ADR-0010).

## Seguridad

- No se suben al repositorio contraseñas, claves de conexión, archivos `.env` con credenciales ni datos personales reales.
- Las pruebas usan datos ficticios.
- Las credenciales se manejan con variables de entorno y se excluyen mediante `.gitignore`.

## Enlaces

- Sitio web de la academia: <https://polyglotcommonwealth.com/>
