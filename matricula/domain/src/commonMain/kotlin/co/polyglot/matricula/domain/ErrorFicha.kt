package co.polyglot.matricula.domain

/** Motivos por los que una ficha no cumple las reglas. Se devuelven todos juntos, no uno a uno. */
sealed interface ErrorFicha {
    data object NombresVacios : ErrorFicha

    data object ApellidosVacios : ErrorFicha

    data object CorreoInvalido : ErrorFicha

    data object FechaNacimientoInvalida : ErrorFicha

    data object FechaNacimientoFutura : ErrorFicha

    data object IdiomaInteresVacio : ErrorFicha

    /** El texto no es el código de un idioma del catálogo, o el catálogo no lo tiene. */
    data object IdiomaInteresNoDisponible : ErrorFicha

    data object RepresentanteRequeridoParaMenor : ErrorFicha

    data object RepresentanteNombreVacio : ErrorFicha

    data object RepresentanteCorreoInvalido : ErrorFicha

    data object TratamientoDatosNoAceptado : ErrorFicha
}
