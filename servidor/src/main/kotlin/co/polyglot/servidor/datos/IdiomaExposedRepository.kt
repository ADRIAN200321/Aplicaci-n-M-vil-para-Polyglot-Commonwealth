package co.polyglot.servidor.datos

import co.polyglot.catalogo.domain.Certificacion
import co.polyglot.catalogo.domain.CodigoIdioma
import co.polyglot.catalogo.domain.Idioma
import co.polyglot.catalogo.domain.IdiomaRepository
import co.polyglot.catalogo.domain.NivelMcer
import co.polyglot.catalogo.domain.ResultadoBusqueda
import co.polyglot.catalogo.domain.ResultadoListado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal object IdiomasTabla : Table("idiomas") {
    val codigo = text("codigo")
    val nombre = text("nombre")
    val nivelMaximo = text("nivel_maximo")

    override val primaryKey = PrimaryKey(codigo)
}

internal object CertificacionesTabla : Table("certificaciones") {
    val idiomaCodigo = text("idioma_codigo")
    val nombre = text("nombre")

    override val primaryKey = PrimaryKey(idiomaCodigo, nombre)
}

class IdiomaExposedRepository(
    private val conexion: ConexionBaseDeDatos,
) : IdiomaRepository {
    override suspend fun listar(): ResultadoListado =
        try {
            ResultadoListado.Exito(leer(codigo = null))
        } catch (e: ExposedSQLException) {
            ResultadoListado.ErrorInesperado
        }

    override suspend fun buscar(codigo: CodigoIdioma): ResultadoBusqueda =
        try {
            leer(codigo.valor)
                .firstOrNull()
                ?.let(ResultadoBusqueda::Encontrado)
                ?: ResultadoBusqueda.NoEncontrado
        } catch (e: ExposedSQLException) {
            ResultadoBusqueda.ErrorInesperado
        }

    private suspend fun leer(codigo: String?): List<Idioma> =
        withContext(Dispatchers.IO) {
            transaction(conexion.database) {
                val certificaciones =
                    CertificacionesTabla
                        .selectAll()
                        .where { codigo?.let { CertificacionesTabla.idiomaCodigo eq it } ?: Op.TRUE }
                        .groupBy({ it[CertificacionesTabla.idiomaCodigo] }, { it[CertificacionesTabla.nombre] })
                IdiomasTabla
                    .selectAll()
                    .where { codigo?.let { IdiomasTabla.codigo eq it } ?: Op.TRUE }
                    .map { it.aIdioma(certificaciones[it[IdiomasTabla.codigo]].orEmpty()) }
            }
        }
}

/** Los datos salen de nuestras propias migraciones: un valor que no encaja es un error de datos, no de entrada. */
private fun ResultRow.aIdioma(certificaciones: List<String>): Idioma =
    Idioma(
        codigo = checkNotNull(CodigoIdioma.desde(this[IdiomasTabla.codigo])),
        nombre = this[IdiomasTabla.nombre],
        nivelMaximo = checkNotNull(NivelMcer.desde(this[IdiomasTabla.nivelMaximo])),
        certificaciones = certificaciones.sorted().map(::Certificacion),
    )
