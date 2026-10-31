package co.polyglot.servidor

import co.polyglot.matricula.domain.EnviarFichaInscripcion
import co.polyglot.servidor.api.moduloApi
import co.polyglot.servidor.arranque.moduloServidor
import co.polyglot.servidor.datos.ConexionBaseDeDatos
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.core.context.startKoin

private const val PUERTO_POR_DEFECTO = 8080

fun main() {
    val koin = startKoin { modules(moduloServidor) }.koin
    koin.get<ConexionBaseDeDatos>().migrar()
    val enviarFicha = koin.get<EnviarFichaInscripcion>()
    val puerto = System.getenv("PORT")?.toIntOrNull() ?: PUERTO_POR_DEFECTO

    embeddedServer(Netty, port = puerto) { moduloApi(enviarFicha) }.start(wait = true)
}
