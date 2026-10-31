package co.polyglot.servidor.datos

import co.polyglot.catalogo.domain.Certificacion
import co.polyglot.catalogo.domain.CodigoIdioma
import co.polyglot.catalogo.domain.NivelMcer
import co.polyglot.catalogo.domain.ResultadoBusqueda
import co.polyglot.catalogo.domain.ResultadoListado
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Comprueba la migración V2 contra una PostgreSQL real. Sin Docker, las pruebas se omiten. */
class IdiomaExposedRepositoryTest {
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

    private fun codigo(texto: String) = checkNotNull(CodigoIdioma.desde(texto))

    @Test
    fun `dada la migracion del catalogo cuando se listan los idiomas entonces salen los seis de la academia`() =
        runTest {
            val resultado = IdiomaExposedRepository(conexion).listar()

            val idiomas = assertIs<ResultadoListado.Exito>(resultado).idiomas
            assertEquals(setOf("de", "en", "es", "fr", "it", "pt"), idiomas.map { it.codigo.valor }.toSet())
        }

    @Test
    fun `dado el ingles cuando se busca entonces trae su nivel maximo y sus certificaciones ordenadas`() =
        runTest {
            val resultado = IdiomaExposedRepository(conexion).buscar(codigo("en"))

            val ingles = assertIs<ResultadoBusqueda.Encontrado>(resultado).idioma
            assertEquals(NivelMcer.C2, ingles.nivelMaximo)
            assertEquals(
                listOf("Business", "Cambridge", "Duolingo", "IELTS", "PTE", "TOEFL").map(::Certificacion),
                ingles.certificaciones,
            )
        }

    @Test
    fun `dado el espanol cuando se busca entonces es el programa ELE y no una interfaz`() =
        runTest {
            val resultado = IdiomaExposedRepository(conexion).buscar(codigo("es"))

            val espanol = assertIs<ResultadoBusqueda.Encontrado>(resultado).idioma
            assertEquals("Español para extranjeros (ELE)", espanol.nombre)
            assertEquals(listOf(Certificacion("DELE")), espanol.certificaciones)
        }

    @Test
    fun `dado un codigo que el catalogo no tiene cuando se busca entonces no lo encuentra`() =
        runTest {
            val resultado = IdiomaExposedRepository(conexion).buscar(codigo("zz"))

            assertEquals(ResultadoBusqueda.NoEncontrado, resultado)
        }
}
