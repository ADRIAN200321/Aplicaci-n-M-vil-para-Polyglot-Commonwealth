package co.polyglot.catalogo.data

import co.polyglot.catalogo.domain.Certificacion
import co.polyglot.catalogo.domain.CodigoIdioma
import co.polyglot.catalogo.domain.Idioma
import co.polyglot.catalogo.domain.NivelMcer
import kotlinx.serialization.Serializable

@Serializable
internal data class IdiomaDto(
    val codigo: String,
    val nombre: String,
    val nivelMaximo: String,
    val certificaciones: List<String>,
)

/** Devuelve nada si el servidor envía un código o un nivel que este cliente no entiende. */
internal fun IdiomaDto.aIdioma(): Idioma? {
    val codigoIdioma = CodigoIdioma.desde(codigo) ?: return null
    val nivel = NivelMcer.desde(nivelMaximo) ?: return null
    return Idioma(codigoIdioma, nombre, nivel, certificaciones.map(::Certificacion))
}
