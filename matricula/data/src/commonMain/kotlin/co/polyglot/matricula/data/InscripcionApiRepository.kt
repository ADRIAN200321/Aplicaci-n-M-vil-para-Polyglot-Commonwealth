package co.polyglot.matricula.data

import co.polyglot.matricula.domain.FichaInscripcion
import co.polyglot.matricula.domain.InscripcionId
import co.polyglot.matricula.domain.InscripcionRepository
import co.polyglot.matricula.domain.ResultadoRegistro
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

private const val RUTA_INSCRIPCIONES = "/api/v1/inscripciones"

class InscripcionApiRepository(
    private val cliente: HttpClient,
    private val urlBase: String,
) : InscripcionRepository {
    override suspend fun registrar(ficha: FichaInscripcion): ResultadoRegistro =
        try {
            val respuesta =
                cliente.post(urlBase.trimEnd('/') + RUTA_INSCRIPCIONES) {
                    contentType(ContentType.Application.Json)
                    setBody(ficha.aDto())
                }
            when (respuesta.status) {
                HttpStatusCode.Created -> ResultadoRegistro.Registrada(InscripcionId(respuesta.body<InscripcionCreadaDto>().id))
                HttpStatusCode.Conflict -> ResultadoRegistro.CorreoYaRegistrado
                HttpStatusCode.UnprocessableEntity -> rechazo(respuesta.body<ErroresDto>())
                else -> ResultadoRegistro.ErrorInesperado
            }
        } catch (_: IOException) {
            ResultadoRegistro.SinConexion
        } catch (_: SerializationException) {
            ResultadoRegistro.ErrorInesperado
        }

    private fun rechazo(dto: ErroresDto): ResultadoRegistro {
        val errores = dto.aErroresDeFicha()
        return if (errores.isEmpty()) ResultadoRegistro.ErrorInesperado else ResultadoRegistro.Rechazada(errores)
    }
}
