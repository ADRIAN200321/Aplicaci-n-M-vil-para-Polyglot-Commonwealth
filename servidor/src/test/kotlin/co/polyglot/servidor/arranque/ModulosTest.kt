package co.polyglot.servidor.arranque

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import kotlin.test.Test

class ModulosTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `dado el modulo del servidor cuando se verifica entonces todas las dependencias se pueden resolver`() {
        // String: la configuración se lee del entorno dentro de su propia definición.
        moduloServidor.verify(extraTypes = listOf(String::class))
    }
}
