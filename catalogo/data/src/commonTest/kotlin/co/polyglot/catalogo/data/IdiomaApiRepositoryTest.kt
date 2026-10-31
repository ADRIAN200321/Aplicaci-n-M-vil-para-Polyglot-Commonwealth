package co.polyglot.catalogo.data

import co.polyglot.catalogo.domain.Certificacion
import co.polyglot.catalogo.domain.CodigoIdioma
import co.polyglot.catalogo.domain.Idioma
import co.polyglot.catalogo.domain.NivelMcer
import co.polyglot.catalogo.domain.ResultadoBusqueda
import co.polyglot.catalogo.domain.ResultadoListado
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class IdiomaApiRepositoryTest {
    private val cabecerasJson = headersOf(HttpHeaders.ContentType, "application/json")

    private val cuerpoIngles = """{"codigo":"en","nombre":"English","nivelMaximo":"C2","certificaciones":["IELTS","Business"]}"""

    private val ingles =
        Idioma(
            codigo = checkNotNull(CodigoIdioma.desde("en")),
            nombre = "English",
            nivelMaximo = NivelMcer.C2,
            certificaciones = listOf(Certificacion("IELTS"), Certificacion("Business")),
        )

    private fun repositorio(manejador: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        IdiomaApiRepository(crearClienteHttp(MockEngine(manejador)), "http://servidor.test/")

    private fun codigo(texto: String) = checkNotNull(CodigoIdioma.desde(texto))

    @Test
    fun `dado un 200 con idiomas cuando se listan entonces los traduce al dominio`() =
        runTest {
            val repo = repositorio { respond("[$cuerpoIngles]", HttpStatusCode.OK, cabecerasJson) }

            assertEquals(ResultadoListado.Exito(listOf(ingles)), repo.listar())
        }

    @Test
    fun `dado una peticion de listado entonces usa GET sobre el contrato acordado`() =
        runTest {
            var peticion: HttpRequestData? = null
            val repo =
                repositorio {
                    peticion = it
                    respond("[]", HttpStatusCode.OK, cabecerasJson)
                }

            repo.listar()

            val enviada = checkNotNull(peticion)
            assertEquals(HttpMethod.Get, enviada.method)
            assertEquals("http://servidor.test/api/v1/idiomas", enviada.url.toString())
        }

    @Test
    fun `dado un idioma con nivel desconocido cuando se listan entonces es un error inesperado`() =
        runTest {
            val cuerpo = """[{"codigo":"en","nombre":"English","nivelMaximo":"Z9","certificaciones":[]}]"""
            val repo = repositorio { respond(cuerpo, HttpStatusCode.OK, cabecerasJson) }

            assertEquals(ResultadoListado.ErrorInesperado, repo.listar())
        }

    @Test
    fun `dado un cuerpo que no es json cuando se listan entonces es un error inesperado`() =
        runTest {
            val repo = repositorio { respond("{no es json", HttpStatusCode.OK, cabecerasJson) }

            assertEquals(ResultadoListado.ErrorInesperado, repo.listar())
        }

    @Test
    fun `dado que falla la red cuando se listan entonces devuelve sin conexion`() =
        runTest {
            val repo = repositorio { throw IOException("sin red") }

            assertEquals(ResultadoListado.SinConexion, repo.listar())
        }

    @Test
    fun `dado un 500 cuando se listan entonces es un error inesperado`() =
        runTest {
            val repo = repositorio { respond("", HttpStatusCode.InternalServerError) }

            assertEquals(ResultadoListado.ErrorInesperado, repo.listar())
        }

    @Test
    fun `dado un 200 cuando se busca un idioma entonces lo devuelve y pide su codigo en la ruta`() =
        runTest {
            var peticion: HttpRequestData? = null
            val repo =
                repositorio {
                    peticion = it
                    respond(cuerpoIngles, HttpStatusCode.OK, cabecerasJson)
                }

            assertEquals(ResultadoBusqueda.Encontrado(ingles), repo.buscar(codigo("en")))
            assertEquals("http://servidor.test/api/v1/idiomas/en", checkNotNull(peticion).url.toString())
        }

    @Test
    fun `dado un 404 cuando se busca un idioma entonces informa que no existe`() =
        runTest {
            val repo = repositorio { respond("", HttpStatusCode.NotFound) }

            assertEquals(ResultadoBusqueda.NoEncontrado, repo.buscar(codigo("xx")))
        }

    @Test
    fun `dado que falla la red cuando se busca un idioma entonces devuelve sin conexion`() =
        runTest {
            val repo = repositorio { throw IOException("sin red") }

            assertEquals(ResultadoBusqueda.SinConexion, repo.buscar(codigo("en")))
        }

    @Test
    fun `dado un cuerpo que no es json cuando se busca un idioma entonces es un error inesperado`() =
        runTest {
            val repo = repositorio { respond("{no es json", HttpStatusCode.OK, cabecerasJson) }

            assertEquals(ResultadoBusqueda.ErrorInesperado, repo.buscar(codigo("en")))
        }
}
