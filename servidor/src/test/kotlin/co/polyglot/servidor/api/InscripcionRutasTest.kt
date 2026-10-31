package co.polyglot.servidor.api

import co.polyglot.matricula.domain.EnviarFichaInscripcion
import co.polyglot.matricula.domain.FichaInscripcion
import co.polyglot.matricula.domain.InscripcionId
import co.polyglot.matricula.domain.InscripcionRepository
import co.polyglot.matricula.domain.Reloj
import co.polyglot.matricula.domain.ResultadoRegistro
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InscripcionRutasTest {
    private class RepositorioFalso(
        private val respuesta: () -> ResultadoRegistro,
    ) : InscripcionRepository {
        var recibidas = 0

        override suspend fun registrar(ficha: FichaInscripcion): ResultadoRegistro {
            recibidas++
            return respuesta()
        }
    }

    private val relojFijo =
        object : Reloj {
            override fun hoy(): LocalDate = LocalDate(2026, 10, 30)
        }

    private val fichaAdulto =
        """
        {"nombres":"Ana","apellidos":"Prueba","correo":"ana@ejemplo.test","fechaNacimiento":"2000-05-10",
         "idiomaInteres":"idioma-1","aceptaTratamientoDatos":true}
        """.trimIndent()

    private val fichaMenorSinRepresentante =
        """
        {"nombres":"Ana","apellidos":"Prueba","correo":"ana@ejemplo.test","fechaNacimiento":"2012-01-01",
         "idiomaInteres":"idioma-1","aceptaTratamientoDatos":true}
        """.trimIndent()

    private fun probar(
        repositorio: RepositorioFalso,
        cuerpo: String,
        verificar: suspend (HttpStatusCode, String) -> Unit,
    ) = testApplication {
        application { moduloApi(EnviarFichaInscripcion(repositorio, relojFijo)) }
        val respuesta =
            client.post("/api/v1/inscripciones") {
                contentType(ContentType.Application.Json)
                setBody(cuerpo)
            }
        verificar(respuesta.status, respuesta.bodyAsText())
    }

    @Test
    fun `dado una ficha valida cuando se envia entonces responde 201 con el id`() {
        val repositorio = RepositorioFalso { ResultadoRegistro.Registrada(InscripcionId("abc-123")) }

        probar(repositorio, fichaAdulto) { estado, cuerpo ->
            assertEquals(HttpStatusCode.Created, estado)
            assertEquals("""{"id":"abc-123"}""", cuerpo)
        }
        assertEquals(1, repositorio.recibidas)
    }

    @Test
    fun `dado un menor sin representante cuando se envia entonces responde 422 y no guarda`() {
        val repositorio = RepositorioFalso { ResultadoRegistro.ErrorInesperado }

        probar(repositorio, fichaMenorSinRepresentante) { estado, cuerpo ->
            assertEquals(HttpStatusCode.UnprocessableEntity, estado)
            assertEquals("""{"errores":["representante_requerido_para_menor"]}""", cuerpo)
        }
        assertEquals(0, repositorio.recibidas)
    }

    @Test
    fun `dado un correo ya inscrito cuando se envia entonces responde 409`() {
        val repositorio = RepositorioFalso { ResultadoRegistro.CorreoYaRegistrado }

        probar(repositorio, fichaAdulto) { estado, _ -> assertEquals(HttpStatusCode.Conflict, estado) }
    }

    @Test
    fun `dado un json mal formado cuando se envia entonces responde 400`() {
        val repositorio = RepositorioFalso { ResultadoRegistro.ErrorInesperado }

        probar(repositorio, "{esto no es json") { estado, _ -> assertEquals(HttpStatusCode.BadRequest, estado) }
    }

    @Test
    fun `dado un json sin campos obligatorios cuando se envia entonces responde 400`() {
        val repositorio = RepositorioFalso { ResultadoRegistro.ErrorInesperado }

        probar(repositorio, """{"nombres":"Ana"}""") { estado, _ -> assertEquals(HttpStatusCode.BadRequest, estado) }
    }

    @Test
    fun `dado que el repositorio falla cuando se envia entonces responde 500 sin filtrar detalles`() {
        val repositorio = RepositorioFalso { error("fallo interno con ana@ejemplo.test") }

        probar(repositorio, fichaAdulto) { estado, cuerpo ->
            assertEquals(HttpStatusCode.InternalServerError, estado)
            assertTrue(cuerpo.isEmpty())
        }
    }

    @Test
    fun `dado el servidor en marcha cuando se consulta la salud entonces responde ok`() =
        testApplication {
            application { moduloApi(EnviarFichaInscripcion(RepositorioFalso { ResultadoRegistro.ErrorInesperado }, relojFijo)) }

            val respuesta = client.get("/salud")

            assertEquals(HttpStatusCode.OK, respuesta.status)
            assertEquals("""{"estado":"ok"}""", respuesta.bodyAsText())
        }
}
