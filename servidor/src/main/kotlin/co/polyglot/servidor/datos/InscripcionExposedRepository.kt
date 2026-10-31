package co.polyglot.servidor.datos

import co.polyglot.matricula.domain.FichaInscripcion
import co.polyglot.matricula.domain.InscripcionId
import co.polyglot.matricula.domain.InscripcionRepository
import co.polyglot.matricula.domain.ResultadoRegistro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import kotlin.uuid.Uuid

private const val VIOLACION_DE_UNICIDAD = "23505"

internal object InscripcionesTabla : Table("inscripciones") {
    val id = uuid("id")
    val nombres = text("nombres")
    val apellidos = text("apellidos")
    val correo = text("correo")
    val fechaNacimiento = date("fecha_nacimiento")
    val idiomaInteres = text("idioma_interes")
    val representanteNombre = text("representante_nombre").nullable()
    val representanteCorreo = text("representante_correo").nullable()
    val consentimientoEn = timestamp("consentimiento_en")
    val creadaEn = timestamp("creada_en")

    override val primaryKey = PrimaryKey(id)
}

class InscripcionExposedRepository(
    private val conexion: ConexionBaseDeDatos,
) : InscripcionRepository {
    override suspend fun registrar(ficha: FichaInscripcion): ResultadoRegistro =
        withContext(Dispatchers.IO) {
            val nuevoId = Uuid.random()
            val ahora = Clock.System.now()
            try {
                transaction(conexion.database) {
                    InscripcionesTabla.insert {
                        it[id] = nuevoId
                        it[nombres] = ficha.nombres
                        it[apellidos] = ficha.apellidos
                        it[correo] = ficha.correo.valor
                        it[fechaNacimiento] = ficha.fechaNacimiento
                        it[idiomaInteres] = ficha.idiomaInteres.valor
                        it[representanteNombre] = ficha.representante?.nombreCompleto
                        it[representanteCorreo] = ficha.representante?.correo?.valor
                        it[consentimientoEn] = ahora
                        it[creadaEn] = ahora
                    }
                }
                ResultadoRegistro.Registrada(InscripcionId(nuevoId.toString()))
            } catch (e: ExposedSQLException) {
                if (e.sqlState == VIOLACION_DE_UNICIDAD) ResultadoRegistro.CorreoYaRegistrado else throw e
            }
        }
}
