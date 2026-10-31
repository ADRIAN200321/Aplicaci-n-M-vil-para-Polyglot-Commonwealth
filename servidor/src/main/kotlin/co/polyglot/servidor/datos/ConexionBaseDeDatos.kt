package co.polyglot.servidor.datos

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

private const val UBICACION_MIGRACIONES = "classpath:db/migration"
private const val CONEXIONES_MAXIMAS = 10

/** Datos de conexión. Se leen del entorno; nunca se escriben en el repositorio. */
data class ConfiguracionBaseDeDatos(
    val url: String,
    val usuario: String,
    val clave: String,
) {
    companion object {
        const val VARIABLE_URL = "DATABASE_URL"
        const val VARIABLE_USUARIO = "DATABASE_USER"
        const val VARIABLE_CLAVE = "DATABASE_PASSWORD"

        fun desdeEntorno(entorno: Map<String, String> = System.getenv()): ConfiguracionBaseDeDatos {
            val requeridas = listOf(VARIABLE_URL, VARIABLE_USUARIO, VARIABLE_CLAVE)
            val faltantes = requeridas.filter { entorno[it].isNullOrBlank() }
            check(faltantes.isEmpty()) { "Faltan variables de entorno: ${faltantes.joinToString()}" }
            return ConfiguracionBaseDeDatos(
                url = entorno.getValue(VARIABLE_URL),
                usuario = entorno.getValue(VARIABLE_USUARIO),
                clave = entorno.getValue(VARIABLE_CLAVE),
            )
        }
    }
}

/** Agrupa el pool de conexiones y la conexión de Exposed, y aplica las migraciones de Flyway. */
class ConexionBaseDeDatos(
    configuracion: ConfiguracionBaseDeDatos,
) : AutoCloseable {
    private val origen =
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = configuracion.url
                username = configuracion.usuario
                password = configuracion.clave
                maximumPoolSize = CONEXIONES_MAXIMAS
            },
        )

    val database: Database = Database.connect(origen)

    fun migrar() {
        Flyway
            .configure()
            .dataSource(origen)
            .locations(UBICACION_MIGRACIONES)
            .load()
            .migrate()
    }

    override fun close() = origen.close()
}
