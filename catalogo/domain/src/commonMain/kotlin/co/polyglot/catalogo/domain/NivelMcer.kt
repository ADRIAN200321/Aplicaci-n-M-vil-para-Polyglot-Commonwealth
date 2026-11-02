package co.polyglot.catalogo.domain

/** Niveles del Marco Común Europeo de Referencia, de menor a mayor. El orden de declaración importa. */
enum class NivelMcer {
    A1,
    A2,
    B1,
    B2,
    C1,
    C2,
    ;

    companion object {
        fun desde(texto: String): NivelMcer? = entries.firstOrNull { it.name == texto.trim().uppercase() }
    }
}
