package co.polyglot.matricula.domain

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

    private val relojFijo =
        object : Reloj {
            override fun hoy(): LocalDate = LocalDate(2026, 10, 30)
        }

    private val borrador =
        BorradorFicha(
            nombres = "Ana",
            apellidos = "Prueba",
            correo = "ana@ejemplo.test",
            fechaNacimiento = "2000-05-10",
            idiomaInteres = "idioma-1",
            representante = null,
            aceptaTratamientoDatos = true,
        )

    @Test
    fun `dado una ficha invalida cuando se envia entonces no llama al repositorio`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.ErrorInesperado)

            val resultado = EnviarFichaInscripcion(repositorio, relojFijo)(borrador.copy(nombres = ""))

            assertEquals(ResultadoEnvio.Invalida(listOf(ErrorFicha.NombresVacios)), resultado)
            assertTrue(repositorio.recibidas.isEmpty())
        }

    @Test
    fun `dado una ficha valida cuando el repositorio la registra entonces devuelve el id`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.Registrada(InscripcionId("abc")))

            val resultado = EnviarFichaInscripcion(repositorio, relojFijo)(borrador)

            assertEquals(ResultadoEnvio.Enviada(InscripcionId("abc")), resultado)
            assertEquals(1, repositorio.recibidas.size)
        }

    @Test
    fun `dado un correo ya inscrito cuando se envia entonces informa el duplicado`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.CorreoYaRegistrado)

            val resultado = EnviarFichaInscripcion(repositorio, relojFijo)(borrador)

            assertEquals(ResultadoEnvio.CorreoYaRegistrado, resultado)
        }

    @Test
    fun `dado que el servidor rechaza la ficha cuando se envia entonces traslada sus errores`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.Rechazada(listOf(ErrorFicha.CorreoInvalido)))

            val resultado = EnviarFichaInscripcion(repositorio, relojFijo)(borrador)

            assertEquals(ResultadoEnvio.Invalida(listOf(ErrorFicha.CorreoInvalido)), resultado)
        }

    @Test
    fun `dado que no hay red cuando se envia entonces devuelve sin conexion`() =
        runTest {
            val repositorio = RepositorioFalso(ResultadoRegistro.SinConexion)

            assertIs<ResultadoEnvio.SinConexion>(EnviarFichaInscripcion(repositorio, relojFijo)(borrador))
        }
}
