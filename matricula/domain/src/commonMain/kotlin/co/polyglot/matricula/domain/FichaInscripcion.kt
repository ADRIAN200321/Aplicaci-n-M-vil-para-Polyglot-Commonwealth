package co.polyglot.matricula.domain

import kotlinx.datetime.LocalDate
import kotlin.jvm.JvmInline

/** Ficha ya validada: si existe una instancia, cumple las reglas de inscripción. */
data class FichaInscripcion(
    val nombres: String,
    val apellidos: String,
    val correo: Correo,
    val fechaNacimiento: LocalDate,
    val idiomaInteres: IdiomaId,
    val representante: RepresentanteLegal?,
)

data class RepresentanteLegal(
    val nombreCompleto: String,
    val correo: Correo,
)

@JvmInline
value class IdiomaId(
    val valor: String,
)

@JvmInline
value class InscripcionId(
    val valor: String,
)

@JvmInline
value class Correo private constructor(
    val valor: String,
) {
    companion object {
        private val FORMATO = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        fun desde(texto: String): Correo? {
            val limpio = texto.trim().lowercase()
            return if (FORMATO.matches(limpio)) Correo(limpio) else null
        }
    }
}
