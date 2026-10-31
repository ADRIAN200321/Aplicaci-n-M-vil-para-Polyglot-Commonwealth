package co.polyglot.matricula.data

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TIEMPO_MAXIMO_PETICION_MS = 15_000L

/** Cliente HTTP configurado para hablar con el servidor. Sin motor, usa el de la plataforma. */
fun crearClienteHttp(motor: HttpClientEngine? = null): HttpClient {
    val configurar: HttpClientConfig<*>.() -> Unit = {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = TIEMPO_MAXIMO_PETICION_MS
        }
    }
    return if (motor == null) HttpClient(configurar) else HttpClient(motor, configurar)
}
