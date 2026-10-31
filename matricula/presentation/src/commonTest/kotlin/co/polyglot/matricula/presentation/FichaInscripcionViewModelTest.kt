package co.polyglot.matricula.presentation

import co.polyglot.matricula.domain.EnviarFichaInscripcion
import co.polyglot.matricula.domain.ErrorFicha
import co.polyglot.matricula.domain.FichaInscripcion
import co.polyglot.matricula.domain.InscripcionId
import co.polyglot.matricula.domain.InscripcionRepository
import co.polyglot.matricula.domain.Reloj
import co.polyglot.matricula.domain.ResultadoRegistro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FichaInscripcionViewModelTest {
    private class RepositorioFalso(
        var respuesta: ResultadoRegistro,
    ) : InscripcionRepository {
        var llamadas = 0

        override suspend fun registrar(ficha: FichaInscripcion): ResultadoRegistro {
            llamadas++
            return respuesta
        }
    }

    private val relojFijo =
        object : Reloj {
            override fun hoy(): LocalDate = LocalDate(2026, 10, 30)
        }

    @BeforeTest
    fun prepararHiloPrincipal() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun restaurarHiloPrincipal() = Dispatchers.resetMain()

    private fun crear(respuesta: ResultadoRegistro): Pair<FichaInscripcionViewModel, RepositorioFalso> {
        val repositorio = RepositorioFalso(respuesta)
        return FichaInscripcionViewModel(EnviarFichaInscripcion(repositorio, relojFijo)) to repositorio
    }

    private fun FichaInscripcionViewModel.llenarFichaDeAdulto() {
        onAction(FichaAction.NombresCambiados("Ana"))
        onAction(FichaAction.ApellidosCambiados("Prueba"))
        onAction(FichaAction.CorreoCambiado("ana@ejemplo.test"))
        onAction(FichaAction.FechaNacimientoCambiada("2000-05-10"))
        onAction(FichaAction.IdiomaInteresCambiado("idioma-1"))
        onAction(FichaAction.TratamientoDatosCambiado(true))
    }

    @Test
    fun `dado un formulario vacio cuando se pulsa enviar entonces muestra los errores y no llama al repositorio`() {
        val (viewModel, repositorio) = crear(ResultadoRegistro.ErrorInesperado)

        viewModel.onAction(FichaAction.EnviarPulsado)

        assertTrue(ErrorFicha.NombresVacios in viewModel.estado.value.errores)
        assertTrue(ErrorFicha.TratamientoDatosNoAceptado in viewModel.estado.value.errores)
        assertEquals(0, repositorio.llamadas)
        assertFalse(viewModel.estado.value.enviando)
    }

    @Test
    fun `dado una ficha valida cuando se pulsa enviar entonces queda enviada`() {
        val (viewModel, repositorio) = crear(ResultadoRegistro.Registrada(InscripcionId("abc")))
        viewModel.llenarFichaDeAdulto()

        viewModel.onAction(FichaAction.EnviarPulsado)

        assertTrue(viewModel.estado.value.inscripcionEnviada)
        assertTrue(
            viewModel.estado.value.errores
                .isEmpty(),
        )
        assertEquals(1, repositorio.llamadas)
    }

    @Test
    fun `dado un menor con campos de representante vacios cuando se envia entonces pide representante`() {
        val (viewModel, _) = crear(ResultadoRegistro.ErrorInesperado)
        viewModel.llenarFichaDeAdulto()
        viewModel.onAction(FichaAction.FechaNacimientoCambiada("2012-01-01"))

        viewModel.onAction(FichaAction.EnviarPulsado)

        assertEquals(setOf(ErrorFicha.RepresentanteRequeridoParaMenor), viewModel.estado.value.errores)
    }

    @Test
    fun `dado un envio ya completado cuando se pulsa enviar otra vez entonces no repite la llamada`() {
        val (viewModel, repositorio) = crear(ResultadoRegistro.Registrada(InscripcionId("abc")))
        viewModel.llenarFichaDeAdulto()
        viewModel.onAction(FichaAction.EnviarPulsado)

        viewModel.onAction(FichaAction.EnviarPulsado)

        assertEquals(1, repositorio.llamadas)
    }

    @Test
    fun `dado un correo repetido cuando se envia entonces emite un aviso y no marca la ficha como enviada`() =
        runTest {
            val (viewModel, _) = crear(ResultadoRegistro.CorreoYaRegistrado)
            viewModel.llenarFichaDeAdulto()

            viewModel.onAction(FichaAction.EnviarPulsado)

            assertEquals(FichaEffect.MostrarAviso(AvisoFicha.CORREO_YA_REGISTRADO), viewModel.efectos.first())
            assertFalse(viewModel.estado.value.inscripcionEnviada)
        }

    @Test
    fun `dado que no hay red cuando se envia entonces emite aviso de sin conexion`() =
        runTest {
            val (viewModel, _) = crear(ResultadoRegistro.SinConexion)
            viewModel.llenarFichaDeAdulto()

            viewModel.onAction(FichaAction.EnviarPulsado)

            assertEquals(FichaEffect.MostrarAviso(AvisoFicha.SIN_CONEXION), viewModel.efectos.first())
        }
}
