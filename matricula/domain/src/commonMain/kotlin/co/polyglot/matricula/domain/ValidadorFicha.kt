package co.polyglot.matricula.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearsUntil

internal const val EDAD_MAYORIA = 18

sealed interface ResultadoValidacion {
    data class Valida(
        val ficha: FichaInscripcion,
    ) : ResultadoValidacion

    data class Invalida(
        val errores: List<ErrorFicha>,
    ) : ResultadoValidacion
}

/** Reglas de la ficha de inscripción. Un menor de edad exige representante legal (Ley 1581 de 2012). */
class ValidadorFicha {
    fun validar(
        borrador: BorradorFicha,
        hoy: LocalDate,
    ): ResultadoValidacion {
        val errores = mutableListOf<ErrorFicha>()

        if (borrador.nombres.isBlank()) errores += ErrorFicha.NombresVacios
        if (borrador.apellidos.isBlank()) errores += ErrorFicha.ApellidosVacios
        val correo = Correo.desde(borrador.correo)
        if (correo == null) errores += ErrorFicha.CorreoInvalido
        if (borrador.idiomaInteres.isBlank()) errores += ErrorFicha.IdiomaInteresVacio
        if (!borrador.aceptaTratamientoDatos) errores += ErrorFicha.TratamientoDatosNoAceptado

        val fecha = interpretarFecha(borrador.fechaNacimiento)
        when {
            fecha == null -> errores += ErrorFicha.FechaNacimientoInvalida
            fecha > hoy -> errores += ErrorFicha.FechaNacimientoFutura
        }
        val esMenor = fecha != null && fecha <= hoy && fecha.yearsUntil(hoy) < EDAD_MAYORIA

        val representante = validarRepresentante(borrador.representante, esMenor, errores)

        return if (errores.isEmpty() && correo != null && fecha != null) {
            ResultadoValidacion.Valida(
                FichaInscripcion(
                    nombres = borrador.nombres.trim(),
                    apellidos = borrador.apellidos.trim(),
                    correo = correo,
                    fechaNacimiento = fecha,
                    idiomaInteres = IdiomaId(borrador.idiomaInteres.trim()),
                    representante = representante,
                ),
            )
        } else {
            ResultadoValidacion.Invalida(errores)
        }
    }

    private fun validarRepresentante(
        borrador: BorradorRepresentante?,
        esMenor: Boolean,
        errores: MutableList<ErrorFicha>,
    ): RepresentanteLegal? {
        if (borrador == null) {
            if (esMenor) errores += ErrorFicha.RepresentanteRequeridoParaMenor
            return null
        }
        val correo = Correo.desde(borrador.correo)
        if (borrador.nombreCompleto.isBlank()) errores += ErrorFicha.RepresentanteNombreVacio
        if (correo == null) errores += ErrorFicha.RepresentanteCorreoInvalido
        return if (correo != null && borrador.nombreCompleto.isNotBlank()) {
            RepresentanteLegal(borrador.nombreCompleto.trim(), correo)
        } else {
            null
        }
    }

    private fun interpretarFecha(texto: String): LocalDate? = runCatching { LocalDate.parse(texto.trim()) }.getOrNull()
}
