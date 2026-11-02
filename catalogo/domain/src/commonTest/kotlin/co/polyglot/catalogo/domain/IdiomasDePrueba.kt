package co.polyglot.catalogo.domain

fun codigoDePrueba(texto: String): CodigoIdioma = checkNotNull(CodigoIdioma.desde(texto)) { "Código de prueba inválido: $texto" }

fun idiomaDePrueba(
    codigo: String = "xx",
    nombre: String = "Idioma de prueba",
    nivelMaximo: NivelMcer = NivelMcer.C1,
    certificaciones: List<String> = emptyList(),
) = Idioma(codigoDePrueba(codigo), nombre, nivelMaximo, certificaciones.map(::Certificacion))

/** Doble de [IdiomaRepository]: responde lo que la prueba le configure y recuerda qué se le pidió. */
class IdiomaRepositoryFalso(
    var listado: ResultadoListado = ResultadoListado.Exito(emptyList()),
    var busqueda: ResultadoBusqueda = ResultadoBusqueda.NoEncontrado,
) : IdiomaRepository {
    val codigosBuscados = mutableListOf<CodigoIdioma>()

    override suspend fun listar(): ResultadoListado = listado

    override suspend fun buscar(codigo: CodigoIdioma): ResultadoBusqueda {
        codigosBuscados += codigo
        return busqueda
    }
}
