package co.polyglot.servidor.api

import co.polyglot.catalogo.domain.CodigoIdioma
import co.polyglot.catalogo.domain.Idioma
import co.polyglot.catalogo.domain.ListarIdiomas
import co.polyglot.catalogo.domain.ObtenerIdioma
import co.polyglot.catalogo.domain.ResultadoBusqueda
import co.polyglot.catalogo.domain.ResultadoListado
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

private const val PARAMETRO_CODIGO = "codigo"

@Serializable
internal data class IdiomaDto(
    val codigo: String,
    val nombre: String,
    val nivelMaximo: String,
    val certificaciones: List<String>,
)

private fun Idioma.aDto() =
    IdiomaDto(
        codigo = codigo.valor,
        nombre = nombre,
        nivelMaximo = nivelMaximo.name,
        certificaciones = certificaciones.map { it.nombre },
    )

/** Catálogo de solo lectura: es público porque la ficha de inscripción se llena antes de tener cuenta. */
internal fun Route.rutasIdiomas(
    listarIdiomas: ListarIdiomas,
    obtenerIdioma: ObtenerIdioma,
) {
    get("/api/v1/idiomas") {
        when (val resultado = listarIdiomas()) {
            is ResultadoListado.Exito -> call.respond(resultado.idiomas.map { it.aDto() })
            ResultadoListado.SinConexion, ResultadoListado.ErrorInesperado ->
                call.respond(HttpStatusCode.InternalServerError)
        }
    }

    get("/api/v1/idiomas/{$PARAMETRO_CODIGO}") {
        val codigo = call.parameters[PARAMETRO_CODIGO]?.let(CodigoIdioma::desde)
        if (codigo == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }
        when (val resultado = obtenerIdioma(codigo)) {
            is ResultadoBusqueda.Encontrado -> call.respond(resultado.idioma.aDto())
            ResultadoBusqueda.NoEncontrado -> call.respond(HttpStatusCode.NotFound)
            ResultadoBusqueda.SinConexion, ResultadoBusqueda.ErrorInesperado ->
                call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
