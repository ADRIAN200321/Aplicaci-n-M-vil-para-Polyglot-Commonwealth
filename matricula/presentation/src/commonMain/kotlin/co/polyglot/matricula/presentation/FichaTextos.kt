package co.polyglot.matricula.presentation

import co.polyglot.matricula.domain.ErrorFicha

internal fun ErrorFicha.mensaje(): String =
    when (this) {
        ErrorFicha.NombresVacios -> "Escribe tus nombres."
        ErrorFicha.ApellidosVacios -> "Escribe tus apellidos."
        ErrorFicha.CorreoInvalido -> "El correo no es válido."
        ErrorFicha.FechaNacimientoInvalida -> "Usa el formato AAAA-MM-DD."
        ErrorFicha.FechaNacimientoFutura -> "La fecha de nacimiento no puede ser futura."
        ErrorFicha.IdiomaInteresVacio -> "Indica el idioma que quieres aprender."
        ErrorFicha.RepresentanteRequeridoParaMenor -> "Si eres menor de 18 años necesitas un representante legal."
        ErrorFicha.RepresentanteNombreVacio -> "Escribe el nombre del representante."
        ErrorFicha.RepresentanteCorreoInvalido -> "El correo del representante no es válido."
        ErrorFicha.TratamientoDatosNoAceptado -> "Debes aceptar el tratamiento de datos personales."
    }

internal fun AvisoFicha.mensaje(): String =
    when (this) {
        AvisoFicha.SIN_CONEXION -> "No hay conexión con el servidor. Inténtalo de nuevo."
        AvisoFicha.CORREO_YA_REGISTRADO -> "Ese correo ya tiene una inscripción."
        AvisoFicha.ERROR_INESPERADO -> "Algo salió mal. Inténtalo más tarde."
    }
