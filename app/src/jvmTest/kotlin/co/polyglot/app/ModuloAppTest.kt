package co.polyglot.app

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import kotlin.test.Test

class ModuloAppTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `dado el modulo de la app cuando se verifica entonces todas las dependencias se pueden resolver`() {
        // String es la URL del servidor, que cada plataforma entrega al arrancar.
        moduloApp("http://servidor.test").verify(extraTypes = listOf(String::class))
    }
}
