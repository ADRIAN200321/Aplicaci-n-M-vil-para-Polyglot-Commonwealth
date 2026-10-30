# Polyglot Project

Aplicación móvil para **Polyglot Commonwealth Language & Culture**, academia de idiomas de Ocaña (Norte de Santander), integrada con el sitio web y el panel administrativo que la academia ya utiliza.

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

Desarrollar una aplicación móvil para Polyglot Commonwealth, integrada con el sitio web y el panel administrativo existentes, que facilite la inscripción, el aprendizaje y el seguimiento académico de aspirantes y estudiantes, y que dé al Administrador el control completo de la información.

## Actores del sistema

| Actor | Canal | Qué puede hacer |
| --- | --- | --- |
| Visitante | Web y app | Ver programas, ofertas, equipo, ubicación y exámenes; hacer el test de nivel; conversar con Poly |
| Aspirante | Web y app | Todo lo del visitante, más completar la ficha de inscripción y descargar recursos |
| Estudiante | App y web | Consultar recursos, juegos y su información académica; recibir notificaciones |
| Administrador | Panel administrativo | Gestionar inscripciones, estudiantes, certificados, ciclos de pago y multimedia |
| Representante legal | Datos en la ficha | Sus datos son obligatorios al inscribir a un menor de edad |

## Alcance de la primera versión

**Prioridad alta**

- Inicio, programas, test de nivel, contacto y ubicación.
- Ficha de inscripción de tres pasos, con datos del representante legal para menores.
- Inicio de sesión con control de acceso por rol.
- Integración de las inscripciones con el panel (listar, descargar PDF, matricular y eliminar).
- Seguridad y protección de datos personales (Ley 1581 de 2012).

**Prioridad media:** recursos de estudio, juegos, asistente Poly, equipo y certificaciones, perfil del estudiante y gestión de estudiantes, certificados y multimedia desde el panel.

**Prioridad baja:** cambio de idioma de la interfaz, certificados propios del estudiante, notificaciones y consulta de ciclos de pago.

**Fuera de esta versión:** pagos en línea, clases en vivo, nuevos idiomas o sedes y rol de Docente.

## Estructura del repositorio

```
Aplicaci-n-M-vil-para-Polyglot-Commonwealth/
├── frontend/          # Aplicación móvil
├── backend/           # API y lógica del sistema
├── database/          # Script SQL y modelo de datos
├── documentacion/
│   ├── requisitos/    # Documento de requisitos (Semana 1)
│   ├── diseno/        # Diagramas, prototipos y modelo de datos
│   ├── pruebas/       # Matriz de pruebas y evidencias
│   └── manuales/      # Manual técnico y manual de usuario
├── README.md
└── .gitignore
```

> La tecnología de cada carpeta se define en el diseño del sistema (Semana 2); la estructura se ajustará si es necesario.

## Documentación

| Documento | Ubicación |
| --- | --- |
| Especificación de requisitos de software (SRS), Semana 1 | `documentacion/requisitos/Polyglot_Project_SRS_Semana_1.docx` |

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

Las instrucciones de instalación y ejecución se agregarán a partir de la Semana 4, cuando existan la base de datos y el backend.

## Seguridad

- No se suben al repositorio contraseñas, claves de conexión, archivos `.env` con credenciales ni datos personales reales.
- Las pruebas usan datos ficticios.
- Las credenciales se manejan con variables de entorno y se excluyen mediante `.gitignore`.

## Enlaces

- Sitio web de la academia: <https://polyglotcommonwealth.com/>
