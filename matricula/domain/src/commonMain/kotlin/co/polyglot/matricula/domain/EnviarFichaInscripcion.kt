package co.polyglot.matricula.domain

sealed interface ResultadoEnvio {
    data class Enviada(
        val id: InscripcionId,
    ) : ResultadoEnvio

    data class Invalida(
        val errores: List<ErrorFicha>,
    ) : ResultadoEnvio

    data object CorreoYaRegistrado : ResultadoEnvio

    data object SinConexion : ResultadoEnvio

    data object ErrorInesperado : ResultadoEnvio
}

/** Valida la ficha y la entrega al repositorio. Cliente y servidor aplican la misma validación. */
class EnviarFichaInscripcion(
    private val repositorio: InscripcionRepository,
    private val reloj: Reloj,
    private val validador: ValidadorFicha = ValidadorFicha(),
) {
    suspend operator fun invoke(borrador: BorradorFicha): ResultadoEnvio =
        when (val validacion = validador.validar(borrador, reloj.hoy())) {
            is ResultadoValidacion.Invalida -> ResultadoEnvio.Invalida(validacion.errores)
            is ResultadoValidacion.Valida -> repositorio.registrar(validacion.ficha).aEnvio()
        }

    private fun ResultadoRegistro.aEnvio(): ResultadoEnvio =
        when (this) {
            is ResultadoRegistro.Registrada -> ResultadoEnvio.Enviada(id)
            is ResultadoRegistro.Rechazada -> ResultadoEnvio.Invalida(errores)
            ResultadoRegistro.CorreoYaRegistrado -> ResultadoEnvio.CorreoYaRegistrado
            ResultadoRegistro.SinConexion -> ResultadoEnvio.SinConexion
            ResultadoRegistro.ErrorInesperado -> ResultadoEnvio.ErrorInesperado
        }
}
