package co.polyglot.matricula.data

import co.polyglot.matricula.domain.Correo
import co.polyglot.matricula.domain.ErrorFicha
import co.polyglot.matricula.domain.FichaInscripcion
import co.polyglot.matricula.domain.IdiomaId
import co.polyglot.matricula.domain.InscripcionId
import co.polyglot.matricula.domain.ResultadoRegistro
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InscripcionApiRepositoryTest {
    private val ficha =
        FichaInscripcion(
            nombres = "Ana",
            apellidos = "Prueba",
            correo = checkNotNull(Correo.desde("ana@ejemplo.test")),
            fechaNacimiento = LocalDate(2000, 5, 10),
            idiomaInteres = IdiomaId("idioma-1"),
            representante = null,
        )

    private val cabecerasJson = headersOf(HttpHeaders.ContentType, "application/json")

    private fun repositorio(manejador: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        InscripcionApiRepository(crearClienteHttp(MockEngine(manejador)), "http://servidor.test/")

    @Test
    fun `dado un 201 cuando se registra entonces devuelve el id creado`() =
        runTest {
            val repo = repositorio { respond("""{"id":"abc-123"}""", HttpStatusCode.Created, cabecerasJson) }

            assertEquals(ResultadoRegistro.Registrada(InscripcionId("abc-123")), repo.registrar(ficha))
        }

    @Test
    fun `dado un 409 cuando se registra entonces informa que el correo ya existe`() =
        runTest {
            val repo = repositorio { respond("""{"errores":[]}""", HttpStatusCode.Conflict, cabecerasJson) }

            assertEquals(ResultadoRegistro.CorreoYaRegistrado, repo.registrar(ficha))
        }

    @Test
    fun `dado un 422 cuando se registra entonces traduce los codigos a errores de dominio`() =
        runTest {
            val cuerpo = """{"errores":["correo_invalido","representante_requerido_para_menor","codigo_desconocido"]}"""
            val repo = repositorio { respond(cuerpo, HttpStatusCode.UnprocessableEntity, cabecerasJson) }

            val esperado = listOf(ErrorFicha.CorreoInvalido, ErrorFicha.RepresentanteRequeridoParaMenor)
            assertEquals(ResultadoRegistro.Rechazada(esperado), repo.registrar(ficha))
        }

    @Test
    fun `dado un 422 con codigos desconocidos cuando se registra entonces es un error inesperado`() =
        runTest {
            val repo = repositorio { respond("""{"errores":["algo_nuevo"]}""", HttpStatusCode.UnprocessableEntity, cabecerasJson) }

            assertEquals(ResultadoRegistro.ErrorInesperado, repo.registrar(ficha))
        }

    @Test
    fun `dado un 500 cuando se registra entonces es un error inesperado`() =
        runTest {
            val repo = repositorio { respond("", HttpStatusCode.InternalServerError) }

            assertEquals(ResultadoRegistro.ErrorInesperado, repo.registrar(ficha))
        }

    @Test
    fun `dado que falla la red cuando se registra entonces devuelve sin conexion`() =
        runTest {
            val repo = repositorio { throw IOException("sin red") }

            assertEquals(ResultadoRegistro.SinConexion, repo.registrar(ficha))
        }

    @Test
    fun `dado una ficha cuando se registra entonces la envia por POST al contrato acordado`() =
        runTest {
            var peticion: HttpRequestData? = null
            val repo =
                repositorio {
                    peticion = it
                    respond("""{"id":"x"}""", HttpStatusCode.Created, cabecerasJson)
                }

            repo.registrar(ficha)

            val enviada = checkNotNull(peticion)
            assertEquals(HttpMethod.Post, enviada.method)
            assertEquals("http://servidor.test/api/v1/inscripciones", enviada.url.toString())
            val cuerpo = enviada.body.toByteArray().decodeToString()
            assertTrue(""""fechaNacimiento":"2000-05-10"""" in cuerpo)
            assertTrue(""""aceptaTratamientoDatos":true""" in cuerpo)
        }
}
