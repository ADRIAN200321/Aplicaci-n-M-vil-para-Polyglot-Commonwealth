package co.polyglot.matricula.data

import co.polyglot.matricula.domain.ErrorFicha
import co.polyglot.matricula.domain.FichaInscripcion
import kotlinx.serialization.Serializable

@Serializable
internal data class FichaDto(
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val fechaNacimiento: String,
    val idiomaInteres: String,
    val representante: RepresentanteDto?,
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

/** Una ficha ya validada implica que la persona aceptó el tratamiento de datos. */
internal fun FichaInscripcion.aDto(): FichaDto =
    FichaDto(
        nombres = nombres,
        apellidos = apellidos,
        correo = correo.valor,
        fechaNacimiento = fechaNacimiento.toString(),
        idiomaInteres = idiomaInteres.valor,
        representante = representante?.let { RepresentanteDto(it.nombreCompleto, it.correo.valor) },
        aceptaTratamientoDatos = true,
    )

private val CODIGOS: Map<String, ErrorFicha> =
    mapOf(
        "nombres_vacios" to ErrorFicha.NombresVacios,
        "apellidos_vacios" to ErrorFicha.ApellidosVacios,
        "correo_invalido" to ErrorFicha.CorreoInvalido,
        "fecha_nacimiento_invalida" to ErrorFicha.FechaNacimientoInvalida,
        "fecha_nacimiento_futura" to ErrorFicha.FechaNacimientoFutura,
        "idioma_interes_vacio" to ErrorFicha.IdiomaInteresVacio,
        "representante_requerido_para_menor" to ErrorFicha.RepresentanteRequeridoParaMenor,
        "representante_nombre_vacio" to ErrorFicha.RepresentanteNombreVacio,
        "representante_correo_invalido" to ErrorFicha.RepresentanteCorreoInvalido,
        "tratamiento_datos_no_aceptado" to ErrorFicha.TratamientoDatosNoAceptado,
    )

internal fun ErroresDto.aErroresDeFicha(): List<ErrorFicha> = errores.mapNotNull(CODIGOS::get)
