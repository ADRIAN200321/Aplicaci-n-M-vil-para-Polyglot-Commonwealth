package co.polyglot.matricula.domain

/** Datos tal como los escribe la persona, sin validar. Todo es texto a propósito. */
data class BorradorFicha(
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val fechaNacimiento: String,
    val idiomaInteres: String,
    val representante: BorradorRepresentante?,
    val aceptaTratamientoDatos: Boolean,
)

data class BorradorRepresentante(
    val nombreCompleto: String,
    val correo: String,
)
