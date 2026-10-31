package co.polyglot.matricula.presentation

import co.polyglot.matricula.domain.ErrorFicha

data class FichaUiState(
    val nombres: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val fechaNacimiento: String = "",
    /** Código del idioma elegido entre [idiomas]; vacío mientras no se elige. */
    val idiomaInteres: String = "",
    val idiomas: List<OpcionIdioma> = emptyList(),
    val cargandoIdiomas: Boolean = true,
    val errorCargaIdiomas: Boolean = false,
    val representanteNombre: String = "",
    val representanteCorreo: String = "",
    val aceptaTratamientoDatos: Boolean = false,
    val enviando: Boolean = false,
    val errores: Set<ErrorFicha> = emptySet(),
    val inscripcionEnviada: Boolean = false,
)

/** Idioma del catálogo tal como se ofrece en el selector. */
data class OpcionIdioma(
    val codigo: String,
    val nombre: String,
)

sealed interface FichaAction {
    data class NombresCambiados(
        val texto: String,
    ) : FichaAction

    data class ApellidosCambiados(
        val texto: String,
    ) : FichaAction

    data class CorreoCambiado(
        val texto: String,
    ) : FichaAction

    data class FechaNacimientoCambiada(
        val texto: String,
    ) : FichaAction

    data class IdiomaInteresCambiado(
        val codigo: String,
    ) : FichaAction

    data object ReintentarCargaIdiomas : FichaAction

    data class RepresentanteNombreCambiado(
        val texto: String,
    ) : FichaAction

    data class RepresentanteCorreoCambiado(
        val texto: String,
    ) : FichaAction

    data class TratamientoDatosCambiado(
        val acepta: Boolean,
    ) : FichaAction

    data object EnviarPulsado : FichaAction
}

/** Efectos de una sola vez: no forman parte del estado porque no deben repetirse al rotar la pantalla. */
sealed interface FichaEffect {
    data class MostrarAviso(
        val aviso: AvisoFicha,
    ) : FichaEffect
}

enum class AvisoFicha {
    SIN_CONEXION,
    CORREO_YA_REGISTRADO,
    ERROR_INESPERADO,
}
