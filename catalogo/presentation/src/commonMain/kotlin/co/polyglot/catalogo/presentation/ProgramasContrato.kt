package co.polyglot.catalogo.presentation

data class ProgramasUiState(
    val cargando: Boolean = true,
    val programas: List<ProgramaUi> = emptyList(),
    val error: ErrorProgramas? = null,
)

/** Un idioma ya preparado para mostrarse: textos listos, sin tipos del dominio. */
data class ProgramaUi(
    val codigo: String,
    val nombre: String,
    val niveles: String,
    val certificaciones: List<String>,
)

enum class ErrorProgramas {
    SIN_CONEXION,
    INESPERADO,
}

sealed interface ProgramasAction {
    data object Reintentar : ProgramasAction
}
