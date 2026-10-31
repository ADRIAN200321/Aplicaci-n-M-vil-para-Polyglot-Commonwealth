# ADR-0007: Autenticación con JWT y registro por etapas

- **Estado:** Propuesta
- **Fecha:** 2026-10-30
- **Autor:** Juan Santiago Guerrero

## Contexto

El SRS exige registro, inicio y cierre de sesión y recuperación de contraseña (RF-15), control de acceso por rol (RF-20), contraseñas cifradas (RNF-01), comprobación del rol en el servidor en cada operación (RNF-02), HTTPS (RNF-03) y tratamiento de datos personales conforme a la Ley 1581 de 2012, con los datos de menores acompañados de su representante legal (RNF-04, RF-09).

Los actores pasan de visitante a aspirante y de aspirante a estudiante cuando el Administrador matricula (CU-01).

## Decisión

### 1. Sesión con JWT y token de refresco

- El servidor emite un **token de acceso** de vida corta (valor inicial: 15 minutos) y un **token de refresco** que se **rota** en cada uso.
- Los tokens de refresco se guardan en la base de datos (solo su hash) para poder revocarlos al cerrar sesión, al restablecer la contraseña o ante una sospecha de robo.
- En el dispositivo los tokens se guardan en el almacenamiento seguro del sistema (Keystore en Android, Keychain en iOS), detrás de una interfaz del dominio (por ejemplo `SecureTokenStore`) con una implementación por plataforma. Nunca en preferencias normales.
- El rol viaja en el token, pero el servidor **vuelve a comprobarlo contra la base de datos** en las operaciones de gestión (RNF-02). Tener un token válido no basta para ejecutar acciones de administrador.

### 2. Contraseñas

- Se almacenan con **Argon2id** (con bcrypt como alternativa aceptable si hay un problema de compatibilidad), con sal por usuario. Nunca en texto plano ni en registros.
- 5 intentos fallidos seguidos bloquean la cuenta durante 15 minutos, y hay un límite por origen (dirección IP) contra ataques que prueban muchas cuentas. Ambos valores son configurables.
- Los mensajes de error no revelan si un correo existe.

### 3. Registro por etapas

1. Cualquier persona se registra con correo y contraseña y queda como **aspirante**. Debe verificar su correo.
2. El aspirante puede completar la ficha de inscripción y descargar recursos (RF-04, RF-08).
3. Cuando el Administrador **matricula**, la cuenta se vincula al registro de estudiante y pasa al rol **estudiante** (RF-16 a RF-18).
4. Los roles con privilegios (Administrador y, más adelante, Docente) **nunca se asignan por autorregistro**: solo los otorga un Administrador existente.

### 4. Menores de edad

- Un menor de 18 años no puede completar su registro ni enviar la ficha sin los datos del **representante legal** (nombre, documento, teléfono y correo) y su **autorización de tratamiento de datos**, que se guarda con fecha y versión del aviso (RF-09).
- La autorización se confirma desde el **correo del representante legal**: se le envía un enlace y la cuenta del menor se activa solo cuando lo confirma. Queda registrada la fecha y la versión del aviso aceptado.
- Mientras no exista esa confirmación, la cuenta del menor queda restringida.

### 5. Recuperar contraseña

- La persona pide el restablecimiento con su correo. El servidor envía un enlace con un token de **un solo uso** y vida corta (valor inicial: 30 minutos), guardado solo como hash.
- La respuesta es siempre la misma exista o no el correo.
- Al restablecer la contraseña se **revocan todos los tokens de refresco** de esa cuenta.

### 6. Correo

El envío de correos es una interfaz del dominio (`EmailSender`) con un adaptador SMTP **configurable por variables de entorno**, que sirve para cualquier proveedor. Los correos salen desde una dirección del dominio de la academia, con SPF y DKIM configurados. En desarrollo se usa un envío falso que imprime el correo y no envía nada.

### 7. Primer Administrador

- La primera cuenta se crea con un script de arranque que lee el correo y una clave temporal desde **variables de entorno**, nunca desde el repositorio.
- En el primer inicio de sesión se obliga a cambiar la clave.
- Las demás cuentas de Administrador las crea solo otro Administrador.
- Mientras el panel actual siga en uso, sus administradores entran por él (ADR-0002).

### 8. Conservación y eliminación de datos

- Los datos se conservan mientras haya relación con la academia.
- Si el representante legal (o el titular, si es mayor de edad) pide eliminarlos, se **eliminan o anonimizan**, salvo lo que la academia deba conservar por obligación legal, como los certificados y actas ya emitidos.
- Los aspirantes que nunca se matriculan se eliminan tras 12 meses de inactividad.
- Estos plazos deben coincidir con la política de tratamiento de datos personales de la academia.

### 9. Estructura

Los casos de uso viven en `identidad/domain`: `RegistrarUsuario`, `IniciarSesion`, `CerrarSesion`, `RefrescarSesion`, `SolicitarRestablecimiento` y `RestablecerContrasena`. Hash de contraseñas, emisión de tokens y envío de correo son interfaces del dominio que implementa `data`.

## Alternativas consideradas

| Alternativa | Por qué no |
| --- | --- |
| Sesión con cookie | Pensada para navegadores; incómoda en móvil y con dos sistemas operativos. |
| Reutilizar el inicio de sesión del panel actual | Ata el sistema nuevo al código heredado, que decidimos aislar (ADR-0002). |
| Solo la academia crea las cuentas | Un aspirante no podría descargar recursos, que el SRS condiciona a registro (RF-04). |
| Inicio de sesión con Google o Apple | Útil, pero suma dependencias y reglas de cada tienda. Puede agregarse después detrás de las mismas interfaces. |

## Consecuencias

- Un token de acceso robado sirve hasta que vence; el costo de su vida corta es una renovación frecuente, transparente para el usuario.
- Hay que guardar tokens de refresco y de restablecimiento en la base de datos, con tareas de limpieza de los vencidos.
- Verificar correos y recuperar contraseñas exige un servicio que envíe correos.
- Las dos implementaciones del almacenamiento seguro (Android e iOS) se prueban en cada sistema.
- La lógica de edad y representante legal vive en el dominio y se prueba sin interfaz ni red.

## Preguntas abiertas

Pendientes de confirmar con la academia:

- ¿El plan de Hostinger incluye cuentas de correo con su dominio, o se usa un proveedor de correo aparte y con qué costo?
- ¿La academia tiene una política de tratamiento de datos personales escrita? Los plazos de conservación (sección 8) deben coincidir con ella. Es una decisión legal, no técnica.
