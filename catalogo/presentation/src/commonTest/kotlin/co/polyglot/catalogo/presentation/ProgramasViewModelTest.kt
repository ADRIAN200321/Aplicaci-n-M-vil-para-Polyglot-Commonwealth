package co.polyglot.catalogo.presentation

import co.polyglot.catalogo.domain.Certificacion
import co.polyglot.catalogo.domain.CodigoIdioma
import co.polyglot.catalogo.domain.Idioma
import co.polyglot.catalogo.domain.IdiomaRepository
import co.polyglot.catalogo.domain.ListarIdiomas
import co.polyglot.catalogo.domain.NivelMcer
import co.polyglot.catalogo.domain.ResultadoBusqueda
import co.polyglot.catalogo.domain.ResultadoListado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ProgramasViewModelTest {
    private class RepositorioFalso(
        var listado: ResultadoListado,
    ) : IdiomaRepository {
        var llamadas = 0

        override suspend fun listar(): ResultadoListado {
            llamadas++
            return listado
        }

        override suspend fun buscar(codigo: CodigoIdioma): ResultadoBusqueda = ResultadoBusqueda.NoEncontrado
    }

    private fun idioma(
        codigo: String,
        nombre: String,
        nivelMaximo: NivelMcer,
        vararg certificaciones: String,
    ) = Idioma(checkNotNull(CodigoIdioma.desde(codigo)), nombre, nivelMaximo, certificaciones.map(::Certificacion))

    @BeforeTest
    fun prepararHiloPrincipal() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun restaurarHiloPrincipal() = Dispatchers.resetMain()

    @Test
    fun `dado un catalogo con idiomas cuando se abre la pantalla entonces muestra cada programa con sus niveles y certificaciones`() {
        val repositorio =
            RepositorioFalso(
                ResultadoListado.Exito(
                    listOf(
                        idioma("fr", "Français", NivelMcer.C1, "DELF", "DALF"),
                        idioma("it", "Italiano", NivelMcer.B2, "CILS"),
                    ),
                ),
            )

        val viewModel = ProgramasViewModel(ListarIdiomas(repositorio))

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertNull(estado.error)
        assertEquals(
            listOf(
                ProgramaUi("fr", "Français", "A1 a C1", listOf("DELF", "DALF")),
                ProgramaUi("it", "Italiano", "A1 a B2", listOf("CILS")),
            ),
            estado.programas,
        )
    }

    @Test
    fun `dado un idioma que solo ensena el primer nivel cuando se muestra entonces no repite el nivel`() {
        val repositorio = RepositorioFalso(ResultadoListado.Exito(listOf(idioma("la", "Latín", NivelMcer.A1))))

        val viewModel = ProgramasViewModel(ListarIdiomas(repositorio))

        assertEquals(
            "A1",
            viewModel.estado.value.programas
                .single()
                .niveles,
        )
    }

    @Test
    fun `dado que no hay red cuando se abre la pantalla entonces muestra el error de conexion`() {
        val viewModel = ProgramasViewModel(ListarIdiomas(RepositorioFalso(ResultadoListado.SinConexion)))

        assertEquals(ErrorProgramas.SIN_CONEXION, viewModel.estado.value.error)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun `dado un error de conexion cuando se pulsa reintentar y vuelve la red entonces muestra los programas`() {
        val repositorio = RepositorioFalso(ResultadoListado.SinConexion)
        val viewModel = ProgramasViewModel(ListarIdiomas(repositorio))
        repositorio.listado = ResultadoListado.Exito(listOf(idioma("de", "Alemán", NivelMcer.C1, "Goethe")))

        viewModel.onAction(ProgramasAction.Reintentar)

        assertNull(viewModel.estado.value.error)
        assertEquals(
            listOf("Alemán"),
            viewModel.estado.value.programas
                .map { it.nombre },
        )
        assertEquals(2, repositorio.llamadas)
    }

    @Test
    fun `dado un fallo inesperado cuando se abre la pantalla entonces muestra el error inesperado`() {
        val viewModel = ProgramasViewModel(ListarIdiomas(RepositorioFalso(ResultadoListado.ErrorInesperado)))

        assertEquals(ErrorProgramas.INESPERADO, viewModel.estado.value.error)
    }
}
