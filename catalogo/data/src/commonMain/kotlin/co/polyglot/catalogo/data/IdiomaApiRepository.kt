package co.polyglot.catalogo.data

import co.polyglot.catalogo.domain.CodigoIdioma
import co.polyglot.catalogo.domain.IdiomaRepository
import co.polyglot.catalogo.domain.ResultadoBusqueda
import co.polyglot.catalogo.domain.ResultadoListado
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException

private const val RUTA_IDIOMAS = "/api/v1/idiomas"

class IdiomaApiRepository(
    private val cliente: HttpClient,
    private val urlBase: String,
) : IdiomaRepository {
    override suspend fun listar(): ResultadoListado =
        try {
            val respuesta = cliente.get(rutaIdiomas())
            if (respuesta.status == HttpStatusCode.OK) respuesta.body<List<IdiomaDto>>().aListado() else ResultadoListado.ErrorInesperado
        } catch (_: IOException) {
            ResultadoListado.SinConexion
        } catch (_: ContentConvertException) {
            ResultadoListado.ErrorInesperado
        }

    override suspend fun buscar(codigo: CodigoIdioma): ResultadoBusqueda =
        try {
            val respuesta = cliente.get(rutaIdiomas() + "/" + codigo.valor)
            when (respuesta.status) {
                HttpStatusCode.OK ->
                    respuesta.body<IdiomaDto>().aIdioma()?.let(ResultadoBusqueda::Encontrado) ?: ResultadoBusqueda.ErrorInesperado
                HttpStatusCode.NotFound -> ResultadoBusqueda.NoEncontrado
                else -> ResultadoBusqueda.ErrorInesperado
            }
        } catch (_: IOException) {
            ResultadoBusqueda.SinConexion
        } catch (_: ContentConvertException) {
            ResultadoBusqueda.ErrorInesperado
        }

    private fun rutaIdiomas() = urlBase.trimEnd('/') + RUTA_IDIOMAS

    private fun List<IdiomaDto>.aListado(): ResultadoListado {
        val idiomas = mapNotNull { it.aIdioma() }
        return if (idiomas.size == size) ResultadoListado.Exito(idiomas) else ResultadoListado.ErrorInesperado
    }
}
