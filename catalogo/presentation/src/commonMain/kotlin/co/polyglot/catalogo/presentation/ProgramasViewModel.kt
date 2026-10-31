package co.polyglot.catalogo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.polyglot.catalogo.domain.Idioma
import co.polyglot.catalogo.domain.ListarIdiomas
import co.polyglot.catalogo.domain.ResultadoListado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** No tiene efectos de una sola vez: todo lo que la pantalla necesita cabe en el estado. */
class ProgramasViewModel(
    private val listarIdiomas: ListarIdiomas,
) : ViewModel() {
    private val estadoInterno = MutableStateFlow(ProgramasUiState())
    val estado: StateFlow<ProgramasUiState> = estadoInterno.asStateFlow()

    init {
        cargar()
    }

    fun onAction(accion: ProgramasAction) {
        when (accion) {
            ProgramasAction.Reintentar -> cargar()
        }
    }

    private fun cargar() {
        estadoInterno.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            estadoInterno.value =
                when (val resultado = listarIdiomas()) {
                    is ResultadoListado.Exito -> ProgramasUiState(cargando = false, programas = resultado.idiomas.map { it.aPrograma() })
                    ResultadoListado.SinConexion -> ProgramasUiState(cargando = false, error = ErrorProgramas.SIN_CONEXION)
                    ResultadoListado.ErrorInesperado -> ProgramasUiState(cargando = false, error = ErrorProgramas.INESPERADO)
                }
        }
    }
}

private fun Idioma.aPrograma(): ProgramaUi =
    ProgramaUi(
        codigo = codigo.valor,
        nombre = nombre,
        niveles = niveles.let { if (it.size == 1) it.first().name else "${it.first()} a ${it.last()}" },
        certificaciones = certificaciones.map { it.nombre },
    )
