package co.polyglot.matricula.domain

import co.polyglot.catalogo.domain.CodigoIdioma
import co.polyglot.catalogo.domain.Idioma
import co.polyglot.catalogo.domain.IdiomaRepository
import co.polyglot.catalogo.domain.NivelMcer
import co.polyglot.catalogo.domain.ObtenerIdioma
import co.polyglot.catalogo.domain.ResultadoBusqueda
import co.polyglot.catalogo.domain.ResultadoListado
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EnviarFichaInscripcionTest {
    private class RepositorioFalso(
        var respuesta: ResultadoRegistro,
    ) : InscripcionRepository {
        val recibidas = mutableListOf<FichaInscripcion>()

        override suspend fun registrar(ficha: FichaInscripcion): ResultadoRegistro {
            recibidas += ficha
            return respuesta
        }
    }

    /** Doble del catálogo: solo sabe responder a la búsqueda por código. */
    private class CatalogoFalso(
        var busqueda: ResultadoBusqueda,
    ) : IdiomaRepository {
        override suspend fun listar(): ResultadoListado = ResultadoListado.Exito(emptyList())

        override suspend fun buscar(codigo: CodigoIdioma): ResultadoBusqueda = busqueda
    }

    private val relojFijo =
        object : Reloj {
            override fun hoy(): LocalDate = LocalDate(2026, 10, 30)
        }

    private val ingles = Idioma(checkNotNull(CodigoIdioma.desde("en")), "English", NivelMcer.C2, emptyList())

    private val borrador =
        BorradorFicha(
            nombres = "Ana",
            apellidos = "Prueba",
            correo = "ana@ejemplo.test",
            fechaNacimiento = "2000-05-10",
            idiomaInteres = "en",
            representante = null,
            aceptaTratamientoDatos = true,
        )

    private fun casoDeUso(
        repositorio: RepositorioFalso,
        busqueda: ResultadoBusqueda = ResultadoBusqueda.Encontrado(ingles),
    ) = EnviarFichaInscripcion(repositorio, relojFijo, ObtenerIdioma(CatalogoFalso(busqueda)))

    @Test
    fun `dado una ficha invalida cuando se envia entonces no llama al repositorio`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.ErrorInesperado)

            val resultado = casoDeUso(repositorio)(borrador.copy(nombres = ""))

            assertEquals(ResultadoEnvio.Invalida(listOf(ErrorFicha.NombresVacios)), resultado)
            assertTrue(repositorio.recibidas.isEmpty())
        }

    @Test
    fun `dado una ficha valida cuando el repositorio la registra entonces devuelve el id`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.Registrada(InscripcionId("abc")))

            val resultado = casoDeUso(repositorio)(borrador)

            assertEquals(ResultadoEnvio.Enviada(InscripcionId("abc")), resultado)
            assertEquals(1, repositorio.recibidas.size)
            assertEquals(
                "en",
                repositorio.recibidas
                    .single()
                    .idiomaInteres.valor,
            )
        }

    @Test
    fun `dado un idioma que no esta en el catalogo cuando se envia entonces lo rechaza y no guarda`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.ErrorInesperado)

            val resultado = casoDeUso(repositorio, ResultadoBusqueda.NoEncontrado)(borrador)

            assertEquals(ResultadoEnvio.Invalida(listOf(ErrorFicha.IdiomaInteresNoDisponible)), resultado)
            assertTrue(repositorio.recibidas.isEmpty())
        }

    @Test
    fun `dado que el catalogo no responde por falta de red cuando se envia entonces devuelve sin conexion y no guarda`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.ErrorInesperado)

            val resultado = casoDeUso(repositorio, ResultadoBusqueda.SinConexion)(borrador)

            assertEquals(ResultadoEnvio.SinConexion, resultado)
            assertTrue(repositorio.recibidas.isEmpty())
        }

    @Test
    fun `dado que el catalogo falla de forma inesperada cuando se envia entonces es un error inesperado y no guarda`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.ErrorInesperado)

            val resultado = casoDeUso(repositorio, ResultadoBusqueda.ErrorInesperado)(borrador)

            assertEquals(ResultadoEnvio.ErrorInesperado, resultado)
            assertTrue(repositorio.recibidas.isEmpty())
        }

    @Test
    fun `dado un correo ya inscrito cuando se envia entonces informa el duplicado`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.CorreoYaRegistrado)

            val resultado = casoDeUso(repositorio)(borrador)

            assertEquals(ResultadoEnvio.CorreoYaRegistrado, resultado)
        }

    @Test
    fun `dado que el servidor rechaza la ficha cuando se envia entonces traslada sus errores`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.Rechazada(listOf(ErrorFicha.CorreoInvalido)))

            val resultado = casoDeUso(repositorio)(borrador)

            assertEquals(ResultadoEnvio.Invalida(listOf(ErrorFicha.CorreoInvalido)), resultado)
        }

    @Test
    fun `dado que no hay red cuando se envia entonces devuelve sin conexion`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.SinConexion)

            assertIs<ResultadoEnvio.SinConexion>(casoDeUso(repositorio)(borrador))
        }
}
