package co.polyglot.catalogo.domain

/**
 * Idioma que la academia enseña. El nivel máximo y las certificaciones son datos de cada idioma:
 * ninguna regla del código nombra un idioma concreto.
 */
data class Idioma(
    val codigo: CodigoIdioma,
    val nombre: String,
    val nivelMaximo: NivelMcer,
    val certificaciones: List<Certificacion>,
) {
    /** Del primer nivel hasta el máximo que se enseña. */
    val niveles: List<NivelMcer>
        get() = NivelMcer.entries.filter { it <= nivelMaximo }

    fun ofrece(nivel: NivelMcer): Boolean = nivel <= nivelMaximo
}

/** Examen o enfoque con el que la academia prepara a sus estudiantes (IELTS, DELE, Business...). */
data class Certificacion(
    val nombre: String,
)
