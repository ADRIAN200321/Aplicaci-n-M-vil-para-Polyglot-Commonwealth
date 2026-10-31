package co.polyglot.matricula.domain

/** Guarda una ficha ya validada. En el cliente la envía al servidor; en el servidor la persiste. */
interface InscripcionRepository {
    suspend fun registrar(ficha: FichaInscripcion): ResultadoRegistro
}

sealed interface ResultadoRegistro {
    data class Registrada(
        val id: InscripcionId,
    ) : ResultadoRegistro

    data object CorreoYaRegistrado : ResultadoRegistro

    /** El servidor aplicó sus propias reglas y rechazó la ficha. */
    data class Rechazada(
        val errores: List<ErrorFicha>,
    ) : ResultadoRegistro

    data object SinConexion : ResultadoRegistro

    data object ErrorInesperado : ResultadoRegistro
}
