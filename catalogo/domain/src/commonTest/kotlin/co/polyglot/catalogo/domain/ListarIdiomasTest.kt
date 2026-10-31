package co.polyglot.catalogo.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ListarIdiomasTest {
    @Test
    fun `dado idiomas desordenados cuando se listan entonces salen ordenados por nombre`() =
        runTest {
            val portugues = idiomaDePrueba(codigo = "pt", nombre = "Português")
            val aleman = idiomaDePrueba(codigo = "de", nombre = "Alemán")
            val ingles = idiomaDePrueba(codigo = "en", nombre = "English")
            val repositorio = IdiomaRepositoryFalso(listado = ResultadoListado.Exito(listOf(portugues, aleman, ingles)))

            val resultado = ListarIdiomas(repositorio)()

            assertEquals(ResultadoListado.Exito(listOf(aleman, ingles, portugues)), resultado)
        }

    @Test
    fun `dado que no hay red cuando se listan entonces traslada la falta de conexion`() =
        runTest {
            val repositorio = IdiomaRepositoryFalso(listado = ResultadoListado.SinConexion)

            assertEquals(ResultadoListado.SinConexion, ListarIdiomas(repositorio)())
        }

    @Test
    fun `dado un fallo inesperado cuando se listan entonces lo traslada`() =
        runTest {
            val repositorio = IdiomaRepositoryFalso(listado = ResultadoListado.ErrorInesperado)

            assertEquals(ResultadoListado.ErrorInesperado, ListarIdiomas(repositorio)())
        }
}
