package co.polyglot.matricula.presentation

import co.polyglot.catalogo.domain.CodigoIdioma
import co.polyglot.catalogo.domain.Idioma
import co.polyglot.catalogo.domain.IdiomaRepository
import co.polyglot.catalogo.domain.ListarIdiomas
import co.polyglot.catalogo.domain.NivelMcer
import co.polyglot.catalogo.domain.ObtenerIdioma
import co.polyglot.catalogo.domain.ResultadoBusqueda
import co.polyglot.catalogo.domain.ResultadoListado
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

    private class CatalogoFalso(
        var listado: ResultadoListado,
    ) : IdiomaRepository {
        var llamadas = 0

        override suspend fun listar(): ResultadoListado {
            llamadas++
            return listado
        }

        override suspend fun buscar(codigo: CodigoIdioma): ResultadoBusqueda =
            (listado as? ResultadoListado.Exito)
                ?.idiomas
                ?.firstOrNull { it.codigo == codigo }
                ?.let(ResultadoBusqueda::Encontrado)
                ?: ResultadoBusqueda.NoEncontrado
    }

    private val relojFijo =
        object : Reloj {
            override fun hoy(): LocalDate = LocalDate(2026, 10, 30)
        }

    private val ingles = Idioma(checkNotNull(CodigoIdioma.desde("en")), "English", NivelMcer.C2, emptyList())
    private val frances = Idioma(checkNotNull(CodigoIdioma.desde("fr")), "Français", NivelMcer.C1, emptyList())

    @BeforeTest
    fun prepararHiloPrincipal() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun restaurarHiloPrincipal() = Dispatchers.resetMain()

    private fun crear(
        respuesta: ResultadoRegistro,
        catalogo: CatalogoFalso = CatalogoFalso(ResultadoListado.Exito(listOf(ingles, frances))),
    ): Pair<FichaInscripcionViewModel, RepositorioFalso> {
        val repositorio = RepositorioFalso(respuesta)
        val enviar = EnviarFichaInscripcion(repositorio, relojFijo, ObtenerIdioma(catalogo))
        return FichaInscripcionViewModel(enviar, ListarIdiomas(catalogo)) to repositorio
    }

    private fun FichaInscripcionViewModel.llenarFichaDeAdulto() {
        onAction(FichaAction.NombresCambiados("Ana"))
        onAction(FichaAction.ApellidosCambiados("Prueba"))
        onAction(FichaAction.CorreoCambiado("ana@ejemplo.test"))
        onAction(FichaAction.FechaNacimientoCambiada("2000-05-10"))
        onAction(FichaAction.IdiomaInteresCambiado("en"))
        onAction(FichaAction.TratamientoDatosCambiado(true))
    }

    @Test
    fun `dado un catalogo con idiomas cuando se abre la ficha entonces ofrece los idiomas ordenados`() {
        val (viewModel, _) = crear(ResultadoRegistro.ErrorInesperado)

        val estado = viewModel.estado.value
        assertFalse(estado.cargandoIdiomas)
        assertFalse(estado.errorCargaIdiomas)
        assertEquals(listOf(OpcionIdioma("en", "English"), OpcionIdioma("fr", "Français")), estado.idiomas)
    }

    @Test
    fun `dado que el catalogo no responde cuando se abre la ficha entonces marca el error de carga`() {
        val catalogo = CatalogoFalso(ResultadoListado.SinConexion)

        val (viewModel, _) = crear(ResultadoRegistro.ErrorInesperado, catalogo)

        assertTrue(viewModel.estado.value.errorCargaIdiomas)
        assertTrue(
            viewModel.estado.value.idiomas
                .isEmpty(),
        )
    }

    @Test
    fun `dado un error de carga de idiomas cuando se pulsa reintentar y vuelve la red entonces ofrece los idiomas`() {
        val catalogo = CatalogoFalso(ResultadoListado.SinConexion)
        val (viewModel, _) = crear(ResultadoRegistro.ErrorInesperado, catalogo)
        catalogo.listado = ResultadoListado.Exito(listOf(ingles))

        viewModel.onAction(FichaAction.ReintentarCargaIdiomas)

        assertFalse(viewModel.estado.value.errorCargaIdiomas)
        assertEquals(listOf(OpcionIdioma("en", "English")), viewModel.estado.value.idiomas)
        assertEquals(2, catalogo.llamadas)
    }

    @Test
    fun `dado que no se eligio idioma cuando se pulsa enviar entonces pide elegir uno`() {
        val (viewModel, repositorio) = crear(ResultadoRegistro.ErrorInesperado)
        viewModel.llenarFichaDeAdulto()
        viewModel.onAction(FichaAction.IdiomaInteresCambiado(""))

        viewModel.onAction(FichaAction.EnviarPulsado)

        assertEquals(setOf(ErrorFicha.IdiomaInteresVacio), viewModel.estado.value.errores)
        assertEquals(0, repositorio.llamadas)
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
