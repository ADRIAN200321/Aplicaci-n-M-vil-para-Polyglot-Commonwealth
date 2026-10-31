package co.polyglot.catalogo.domain

/** Entrega los idiomas ordenados por nombre, igual en el cliente y en el servidor. */
class ListarIdiomas(
    private val repositorio: IdiomaRepository,
) {
    suspend operator fun invoke(): ResultadoListado =
        when (val resultado = repositorio.listar()) {
            is ResultadoListado.Exito -> ResultadoListado.Exito(resultado.idiomas.sortedBy { it.nombre })
            ResultadoListado.SinConexion, ResultadoListado.ErrorInesperado -> resultado
        }
}
