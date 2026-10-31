package co.polyglot.servidor.datos

import co.polyglot.matricula.domain.Correo
import co.polyglot.matricula.domain.FichaInscripcion
import co.polyglot.matricula.domain.IdiomaId
import co.polyglot.matricula.domain.RepresentanteLegal
import co.polyglot.matricula.domain.ResultadoRegistro
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Usa una PostgreSQL real en contenedor temporal. Sin Docker, las pruebas se omiten. */
class InscripcionExposedRepositoryTest {
    companion object {
        private var contenedor: PostgreSQLContainer<*>? = null
        private lateinit var conexion: ConexionBaseDeDatos

        @BeforeAll
        @JvmStatic
        fun iniciar() {
            assumeTrue(DockerClientFactory.instance().isDockerAvailable, "Docker no está disponible")
            val postgres = PostgreSQLContainer("postgres:17-alpine")
            postgres.start()
            contenedor = postgres
            conexion = ConexionBaseDeDatos(ConfiguracionBaseDeDatos(postgres.jdbcUrl, postgres.username, postgres.password))
            conexion.migrar()
        }

        @AfterAll
        @JvmStatic
        fun detener() {
            if (contenedor != null) conexion.close()
            contenedor?.stop()
        }
    }

    private fun ficha(
        correo: String,
        representante: RepresentanteLegal? = null,
    ) = FichaInscripcion(
        nombres = "Ana",
        apellidos = "Prueba",
        correo = checkNotNull(Correo.desde(correo)),
        fechaNacimiento = LocalDate(2012, 1, 1),
        idiomaInteres = IdiomaId("idioma-1"),
        representante = representante,
    )

    private fun totalDeFilas(): Long = transaction(conexion.database) { InscripcionesTabla.selectAll().count() }

    @Test
    fun `dado una ficha nueva cuando se registra entonces queda guardada`() =
        runTest {
            val repositorio = InscripcionExposedRepository(conexion)
            val antes = totalDeFilas()

            val resultado = repositorio.registrar(ficha("nueva@ejemplo.test"))

            assertIs<ResultadoRegistro.Registrada>(resultado)
            assertEquals(antes + 1, totalDeFilas())
        }

    @Test
    fun `dado un correo ya registrado cuando se registra otra vez entonces informa el duplicado y no inserta`() =
        runTest {
            val repositorio = InscripcionExposedRepository(conexion)
            repositorio.registrar(ficha("repetido@ejemplo.test"))
            val antes = totalDeFilas()

            val resultado = repositorio.registrar(ficha("repetido@ejemplo.test"))

            assertEquals(ResultadoRegistro.CorreoYaRegistrado, resultado)
            assertEquals(antes, totalDeFilas())
        }

    @Test
    fun `dado un menor con representante cuando se registra entonces guarda tambien al representante`() =
        runTest {
            val repositorio = InscripcionExposedRepository(conexion)
            val representante = RepresentanteLegal("Luis Ficticio", checkNotNull(Correo.desde("luis@ejemplo.test")))

            val resultado = repositorio.registrar(ficha("menor@ejemplo.test", representante))

            assertIs<ResultadoRegistro.Registrada>(resultado)
            val guardado =
                transaction(conexion.database) {
                    InscripcionesTabla.selectAll().single { it[InscripcionesTabla.correo] == "menor@ejemplo.test" }
                }
            assertEquals("Luis Ficticio", guardado[InscripcionesTabla.representanteNombre])
            assertEquals("luis@ejemplo.test", guardado[InscripcionesTabla.representanteCorreo])
        }
}
