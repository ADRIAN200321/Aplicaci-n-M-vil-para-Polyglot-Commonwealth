package co.polyglot.catalogo.domain

/** Consulta del catálogo. Solo lectura: quien administre idiomas tendrá su propia interfaz de escritura. */
interface IdiomaRepository {
    suspend fun listar(): ResultadoListado

    suspend fun buscar(codigo: CodigoIdioma): ResultadoBusqueda
}

sealed interface ResultadoListado {
    data class Exito(
        val idiomas: List<Idioma>,
    ) : ResultadoListado

    data object SinConexion : ResultadoListado

    data object ErrorInesperado : ResultadoListado
}

sealed interface ResultadoBusqueda {
    data class Encontrado(
        val idioma: Idioma,
    ) : ResultadoBusqueda

    data object NoEncontrado : ResultadoBusqueda

    data object SinConexion : ResultadoBusqueda

    data object ErrorInesperado : ResultadoBusqueda
}
