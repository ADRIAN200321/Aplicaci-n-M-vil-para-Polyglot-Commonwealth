package co.polyglot.servidor.datos

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ConfiguracionBaseDeDatosTest {
    @Test
    fun `dado un entorno completo cuando se lee entonces arma la configuracion`() {
        val entorno =
            mapOf(
                "DATABASE_URL" to "jdbc:postgresql://localhost:5432/prueba",
                "DATABASE_USER" to "usuario_de_prueba",
                "DATABASE_PASSWORD" to "clave_de_prueba",
            )

        val configuracion = ConfiguracionBaseDeDatos.desdeEntorno(entorno)

        assertEquals("jdbc:postgresql://localhost:5432/prueba", configuracion.url)
        assertEquals("usuario_de_prueba", configuracion.usuario)
    }

    @Test
    fun `dado un entorno incompleto cuando se lee entonces falla nombrando solo las variables faltantes`() {
        val entorno = mapOf("DATABASE_URL" to "jdbc:postgresql://localhost:5432/prueba", "DATABASE_USER" to " ")

        val error = assertFailsWith<IllegalStateException> { ConfiguracionBaseDeDatos.desdeEntorno(entorno) }

        assertTrue("DATABASE_USER" in error.message.orEmpty())
        assertTrue("DATABASE_PASSWORD" in error.message.orEmpty())
        assertTrue("jdbc:" !in error.message.orEmpty())
    }
}
