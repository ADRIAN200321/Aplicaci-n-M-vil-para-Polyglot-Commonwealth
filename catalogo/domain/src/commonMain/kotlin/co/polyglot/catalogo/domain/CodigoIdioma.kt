package co.polyglot.catalogo.domain

import kotlin.jvm.JvmInline

/** Identificador de un idioma que se aprende: su código ISO 639-1 (`en`, `fr`, `es`...). */
@JvmInline
value class CodigoIdioma private constructor(
    val valor: String,
) {
    companion object {
        private val FORMATO = Regex("^[a-z]{2}$")

        fun desde(texto: String): CodigoIdioma? {
            val limpio = texto.trim().lowercase()
            return if (FORMATO.matches(limpio)) CodigoIdioma(limpio) else null
        }
    }
}
