package co.polyglot.servidor.api

import co.polyglot.matricula.domain.BorradorFicha
import co.polyglot.matricula.domain.BorradorRepresentante
import co.polyglot.matricula.domain.EnviarFichaInscripcion
import co.polyglot.matricula.domain.ErrorFicha
import co.polyglot.matricula.domain.ResultadoEnvio
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

@Serializable
internal data class FichaDto(
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val fechaNacimiento: String,
    val idiomaInteres: String,
    val representante: RepresentanteDto? = null,
    val aceptaTratamientoDatos: Boolean,
)

@Serializable
internal data class RepresentanteDto(
    val nombreCompleto: String,
    val correo: String,
)

@Serializable
internal data class InscripcionCreadaDto(
    val id: String,
)

@Serializable
internal data class ErroresDto(
    val errores: List<String>,
)

@Serializable
internal data class SaludDto(
    val estado: String,
)

private fun FichaDto.aBorrador() =
    BorradorFicha(
        nombres = nombres,
        apellidos = apellidos,
        correo = correo,
        fechaNacimiento = fechaNacimiento,
        idiomaInteres = idiomaInteres,
        representante = representante?.let { BorradorRepresentante(it.nombreCompleto, it.correo) },
        aceptaTratamientoDatos = aceptaTratamientoDatos,
    )

internal fun ErrorFicha.codigo(): String =
    when (this) {
        ErrorFicha.NombresVacios -> "nombres_vacios"
        ErrorFicha.ApellidosVacios -> "apellidos_vacios"
        ErrorFicha.CorreoInvalido -> "correo_invalido"
        ErrorFicha.FechaNacimientoInvalida -> "fecha_nacimiento_invalida"
        ErrorFicha.FechaNacimientoFutura -> "fecha_nacimiento_futura"
        ErrorFicha.IdiomaInteresVacio -> "idioma_interes_vacio"
        ErrorFicha.RepresentanteRequeridoParaMenor -> "representante_requerido_para_menor"
        ErrorFicha.RepresentanteNombreVacio -> "representante_nombre_vacio"
        ErrorFicha.RepresentanteCorreoInvalido -> "representante_correo_invalido"
        ErrorFicha.TratamientoDatosNoAceptado -> "tratamiento_datos_no_aceptado"
    }

/** Rutas HTTP. Reciben el caso de uso ya armado: no conocen el contenedor de dependencias. */
fun Application.moduloApi(enviarFicha: EnviarFichaInscripcion) {
    install(ContentNegotiation) { json() }
    install(StatusPages) {
        // Sin mensajes ni trazas en la respuesta: podrían arrastrar datos personales.
        exception<BadRequestException> { call, _ -> call.respond(HttpStatusCode.BadRequest) }
        exception<Throwable> { call, _ -> call.respond(HttpStatusCode.InternalServerError) }
    }

    routing {
        get("/salud") { call.respond(SaludDto("ok")) }

        post("/api/v1/inscripciones") {
            val ficha = call.receive<FichaDto>()
            when (val resultado = enviarFicha(ficha.aBorrador())) {
                is ResultadoEnvio.Enviada ->
                    call.respond(HttpStatusCode.Created, InscripcionCreadaDto(resultado.id.valor))
                is ResultadoEnvio.Invalida ->
                    call.respond(HttpStatusCode.UnprocessableEntity, ErroresDto(resultado.errores.map { it.codigo() }))
                ResultadoEnvio.CorreoYaRegistrado ->
                    call.respond(HttpStatusCode.Conflict, ErroresDto(emptyList()))
                ResultadoEnvio.SinConexion, ResultadoEnvio.ErrorInesperado ->
                    call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
