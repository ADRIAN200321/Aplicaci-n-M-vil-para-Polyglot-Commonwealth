package co.polyglot.servidor.api

import co.polyglot.catalogo.domain.Certificacion
import co.polyglot.catalogo.domain.CodigoIdioma
import co.polyglot.catalogo.domain.Idioma
import co.polyglot.catalogo.domain.IdiomaRepository
import co.polyglot.catalogo.domain.ListarIdiomas
import co.polyglot.catalogo.domain.NivelMcer
import co.polyglot.catalogo.domain.ObtenerIdioma
import co.polyglot.catalogo.domain.ResultadoBusqueda
import co.polyglot.catalogo.domain.ResultadoListado
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
import io.ktor.server.application.Application
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

    private class CatalogoFalso(
        var listado: ResultadoListado,
    ) : IdiomaRepository {
        override suspend fun listar(): ResultadoListado = listado

        override suspend fun buscar(codigo: CodigoIdioma): ResultadoBusqueda =
            (listado as? ResultadoListado.Exito)
                ?.idiomas
                ?.firstOrNull { it.codigo == codigo }
                ?.let(ResultadoBusqueda::Encontrado)
                ?: ResultadoBusqueda.NoEncontrado
    }

    private val ingles = Idioma(checkNotNull(CodigoIdioma.desde("en")), "English", NivelMcer.C2, listOf(Certificacion("IELTS")))
    private val catalogo = CatalogoFalso(ResultadoListado.Exito(listOf(ingles)))

    private val relojFijo =
        object : Reloj {
            override fun hoy(): LocalDate = LocalDate(2026, 10, 30)
        }

    private val fichaAdulto =
        """
        {"nombres":"Ana","apellidos":"Prueba","correo":"ana@ejemplo.test","fechaNacimiento":"2000-05-10",
         "idiomaInteres":"en","aceptaTratamientoDatos":true}
        """.trimIndent()

    private val fichaMenorSinRepresentante =
        """
        {"nombres":"Ana","apellidos":"Prueba","correo":"ana@ejemplo.test","fechaNacimiento":"2012-01-01",
         "idiomaInteres":"en","aceptaTratamientoDatos":true}
        """.trimIndent()

    private val fichaConIdiomaInexistente =
        """
        {"nombres":"Ana","apellidos":"Prueba","correo":"ana@ejemplo.test","fechaNacimiento":"2000-05-10",
         "idiomaInteres":"zz","aceptaTratamientoDatos":true}
        """.trimIndent()

    private fun Application.conDependenciasDePrueba(repositorio: RepositorioFalso) =
        moduloApi(
            EnviarFichaInscripcion(repositorio, relojFijo, ObtenerIdioma(catalogo)),
            ListarIdiomas(catalogo),
            ObtenerIdioma(catalogo),
        )

    private fun probar(
        repositorio: RepositorioFalso,
        cuerpo: String,
        verificar: suspend (HttpStatusCode, String) -> Unit,
    ) = testApplication {
        application { conDependenciasDePrueba(repositorio) }
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
            application { conDependenciasDePrueba(RepositorioFalso { ResultadoRegistro.ErrorInesperado }) }

            val respuesta = client.get("/salud")

            assertEquals(HttpStatusCode.OK, respuesta.status)
            assertEquals("""{"estado":"ok"}""", respuesta.bodyAsText())
        }

    @Test
    fun `dado un idioma que no esta en el catalogo cuando se envia la ficha entonces responde 422 y no guarda`() {
        val repositorio = RepositorioFalso { ResultadoRegistro.ErrorInesperado }

        probar(repositorio, fichaConIdiomaInexistente) { estado, cuerpo ->
            assertEquals(HttpStatusCode.UnprocessableEntity, estado)
            assertEquals("""{"errores":["idioma_interes_no_disponible"]}""", cuerpo)
        }
        assertEquals(0, repositorio.recibidas)
    }

    @Test
    fun `dado un catalogo con idiomas cuando se listan entonces responde 200 con sus datos`() =
        testApplication {
            application { conDependenciasDePrueba(RepositorioFalso { ResultadoRegistro.ErrorInesperado }) }

            val respuesta = client.get("/api/v1/idiomas")

            assertEquals(HttpStatusCode.OK, respuesta.status)
            assertEquals(
                """[{"codigo":"en","nombre":"English","nivelMaximo":"C2","certificaciones":["IELTS"]}]""",
                respuesta.bodyAsText(),
            )
        }

    @Test
    fun `dado un idioma del catalogo cuando se consulta por codigo entonces responde 200`() =
        testApplication {
            application { conDependenciasDePrueba(RepositorioFalso { ResultadoRegistro.ErrorInesperado }) }

            val respuesta = client.get("/api/v1/idiomas/en")

            assertEquals(HttpStatusCode.OK, respuesta.status)
            assertEquals(
                """{"codigo":"en","nombre":"English","nivelMaximo":"C2","certificaciones":["IELTS"]}""",
                respuesta.bodyAsText(),
            )
        }

    @Test
    fun `dado un codigo inexistente o mal formado cuando se consulta entonces responde 404`() =
        testApplication {
            application { conDependenciasDePrueba(RepositorioFalso { ResultadoRegistro.ErrorInesperado }) }

            assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/idiomas/zz").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/idiomas/ingles").status)
        }

    @Test
    fun `dado que el catalogo falla cuando se listan los idiomas entonces responde 500`() =
        testApplication {
            catalogo.listado = ResultadoListado.ErrorInesperado
            application { conDependenciasDePrueba(RepositorioFalso { ResultadoRegistro.ErrorInesperado }) }

            assertEquals(HttpStatusCode.InternalServerError, client.get("/api/v1/idiomas").status)
        }
}
