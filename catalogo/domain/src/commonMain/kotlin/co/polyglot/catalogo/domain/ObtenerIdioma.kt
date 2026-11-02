package co.polyglot.catalogo.domain

/** Busca un idioma por su código. Es la puerta pública del catálogo para otros contextos, como `matricula`. */
class ObtenerIdioma(
    private val repositorio: IdiomaRepository,
) {
    suspend operator fun invoke(codigo: CodigoIdioma): ResultadoBusqueda = repositorio.buscar(codigo)
}
