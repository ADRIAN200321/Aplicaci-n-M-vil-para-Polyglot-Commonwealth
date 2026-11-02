package co.polyglot.matricula.domain

import co.polyglot.catalogo.domain.ObtenerIdioma
import co.polyglot.catalogo.domain.ResultadoBusqueda

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

/**
 * Valida la ficha, comprueba que el idioma exista en el catálogo y la entrega al repositorio.
 * Cliente y servidor aplican las mismas reglas. El catálogo se consulta solo por [ObtenerIdioma].
 */
class EnviarFichaInscripcion(
    private val repositorio: InscripcionRepository,
    private val reloj: Reloj,
    private val obtenerIdioma: ObtenerIdioma,
    private val validador: ValidadorFicha = ValidadorFicha(),
) {
    suspend operator fun invoke(borrador: BorradorFicha): ResultadoEnvio =
        when (val validacion = validador.validar(borrador, reloj.hoy())) {
            is ResultadoValidacion.Invalida -> ResultadoEnvio.Invalida(validacion.errores)
            is ResultadoValidacion.Valida -> registrarSiElIdiomaExiste(validacion.ficha)
        }

    private suspend fun registrarSiElIdiomaExiste(ficha: FichaInscripcion): ResultadoEnvio =
        when (obtenerIdioma(ficha.idiomaInteres)) {
            is ResultadoBusqueda.Encontrado -> repositorio.registrar(ficha).aEnvio()
            ResultadoBusqueda.NoEncontrado -> ResultadoEnvio.Invalida(listOf(ErrorFicha.IdiomaInteresNoDisponible))
            ResultadoBusqueda.SinConexion -> ResultadoEnvio.SinConexion
            ResultadoBusqueda.ErrorInesperado -> ResultadoEnvio.ErrorInesperado
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
