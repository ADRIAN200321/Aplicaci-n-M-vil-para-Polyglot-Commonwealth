package co.polyglot.matricula.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.polyglot.matricula.domain.BorradorFicha
import co.polyglot.matricula.domain.BorradorRepresentante
import co.polyglot.matricula.domain.EnviarFichaInscripcion
import co.polyglot.matricula.domain.ResultadoEnvio
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FichaInscripcionViewModel(
    private val enviarFicha: EnviarFichaInscripcion,
) : ViewModel() {
    private val estadoInterno = MutableStateFlow(FichaUiState())
    val estado: StateFlow<FichaUiState> = estadoInterno.asStateFlow()

    private val efectosInternos = Channel<FichaEffect>(Channel.BUFFERED)
    val efectos: Flow<FichaEffect> = efectosInternos.receiveAsFlow()

    fun onAction(accion: FichaAction) {
        when (accion) {
            is FichaAction.NombresCambiados -> estadoInterno.update { it.copy(nombres = accion.texto) }
            is FichaAction.ApellidosCambiados -> estadoInterno.update { it.copy(apellidos = accion.texto) }
            is FichaAction.CorreoCambiado -> estadoInterno.update { it.copy(correo = accion.texto) }
            is FichaAction.FechaNacimientoCambiada -> estadoInterno.update { it.copy(fechaNacimiento = accion.texto) }
            is FichaAction.IdiomaInteresCambiado -> estadoInterno.update { it.copy(idiomaInteres = accion.texto) }
            is FichaAction.RepresentanteNombreCambiado -> estadoInterno.update { it.copy(representanteNombre = accion.texto) }
            is FichaAction.RepresentanteCorreoCambiado -> estadoInterno.update { it.copy(representanteCorreo = accion.texto) }
            is FichaAction.TratamientoDatosCambiado -> estadoInterno.update { it.copy(aceptaTratamientoDatos = accion.acepta) }
            FichaAction.EnviarPulsado -> enviar()
        }
    }

    private fun enviar() {
        val actual = estadoInterno.value
        if (actual.enviando || actual.inscripcionEnviada) return
        estadoInterno.update { it.copy(enviando = true, errores = emptySet()) }
        viewModelScope.launch {
            val resultado = enviarFicha(actual.aBorrador())
            estadoInterno.update { it.copy(enviando = false) }
            when (resultado) {
                is ResultadoEnvio.Enviada -> estadoInterno.update { it.copy(inscripcionEnviada = true) }
                is ResultadoEnvio.Invalida -> estadoInterno.update { it.copy(errores = resultado.errores.toSet()) }
                ResultadoEnvio.CorreoYaRegistrado -> avisar(AvisoFicha.CORREO_YA_REGISTRADO)
                ResultadoEnvio.SinConexion -> avisar(AvisoFicha.SIN_CONEXION)
                ResultadoEnvio.ErrorInesperado -> avisar(AvisoFicha.ERROR_INESPERADO)
            }
        }
    }

    private fun avisar(aviso: AvisoFicha) {
        efectosInternos.trySend(FichaEffect.MostrarAviso(aviso))
    }
}

private fun FichaUiState.aBorrador(): BorradorFicha =
    BorradorFicha(
        nombres = nombres,
        apellidos = apellidos,
        correo = correo,
        fechaNacimiento = fechaNacimiento,
        idiomaInteres = idiomaInteres,
        representante =
            if (representanteNombre.isBlank() && representanteCorreo.isBlank()) {
                null
            } else {
                BorradorRepresentante(representanteNombre, representanteCorreo)
            },
        aceptaTratamientoDatos = aceptaTratamientoDatos,
    )
