package co.polyglot.catalogo.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObtenerIdiomaTest {
    @Test
    fun `dado un codigo existente cuando se obtiene entonces devuelve el idioma`() =
        runTest {
            val ingles = idiomaDePrueba(codigo = "en", nombre = "English")
            val repositorio = IdiomaRepositoryFalso(busqueda = ResultadoBusqueda.Encontrado(ingles))

            val resultado = ObtenerIdioma(repositorio)(codigoDePrueba("en"))

            assertEquals(ResultadoBusqueda.Encontrado(ingles), resultado)
            assertEquals(listOf(codigoDePrueba("en")), repositorio.codigosBuscados)
        }

    @Test
    fun `dado un codigo que no esta en el catalogo cuando se obtiene entonces informa que no existe`() =
        runTest {
            val repositorio = IdiomaRepositoryFalso(busqueda = ResultadoBusqueda.NoEncontrado)

            assertEquals(ResultadoBusqueda.NoEncontrado, ObtenerIdioma(repositorio)(codigoDePrueba("xx")))
        }
}
