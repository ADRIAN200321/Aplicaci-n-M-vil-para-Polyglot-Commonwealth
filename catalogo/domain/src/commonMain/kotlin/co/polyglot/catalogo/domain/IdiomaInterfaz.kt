package co.polyglot.catalogo.domain

/**
 * Idioma en que la app muestra sus textos (RF-13). Es un tipo distinto de [Idioma]: el que se aprende
 * y el de la interfaz no se mezclan. Es un conjunto cerrado porque cada valor exige textos traducidos
 * dentro de la app.
 */
enum class IdiomaInterfaz(
    val codigoIso: String,
) {
    ESPANOL("es"),
    INGLES("en"),
    FRANCES("fr"),
    PORTUGUES("pt"),
    ;

    /** Español es a la vez idioma de la interfaz e idioma que se aprende (ELE): se relacionan solo por el código. */
    fun coincideCon(idioma: Idioma): Boolean = codigoIso == idioma.codigo.valor
}
