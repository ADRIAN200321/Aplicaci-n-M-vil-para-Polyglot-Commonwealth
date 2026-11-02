package co.polyglot.matricula.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import co.polyglot.matricula.domain.ErrorFicha
import kotlinx.coroutines.flow.Flow

/** Pantalla sin estado propio: recibe el estado y entrega las acciones. La conecta quien la usa. */
@Composable
fun FichaInscripcionScreen(
    estado: FichaUiState,
    efectos: Flow<FichaEffect>,
    onAction: (FichaAction) -> Unit,
) {
    val snackbars = remember { SnackbarHostState() }
    LaunchedEffect(efectos) {
        efectos.collect { efecto ->
            when (efecto) {
                is FichaEffect.MostrarAviso -> snackbars.showSnackbar(efecto.aviso.mensaje())
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbars) }) { relleno ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (estado.inscripcionEnviada) {
                ConfirmacionEnvio()
            } else {
                Formulario(estado, onAction)
            }
        }
    }
}

@Composable
private fun ConfirmacionEnvio() {
    Text("¡Listo!", style = MaterialTheme.typography.headlineMedium)
    Text("Recibimos tu ficha de inscripción. La academia se pondrá en contacto contigo.")
}

@Composable
private fun Formulario(
    estado: FichaUiState,
    onAction: (FichaAction) -> Unit,
) {
    Text("Ficha de inscripción", style = MaterialTheme.typography.headlineMedium)

    Campo("Nombres", estado.nombres, estado.errores.mensajeDe(ErrorFicha.NombresVacios)) {
        onAction(FichaAction.NombresCambiados(it))
    }
    Campo("Apellidos", estado.apellidos, estado.errores.mensajeDe(ErrorFicha.ApellidosVacios)) {
        onAction(FichaAction.ApellidosCambiados(it))
    }
    Campo("Correo", estado.correo, estado.errores.mensajeDe(ErrorFicha.CorreoInvalido)) {
        onAction(FichaAction.CorreoCambiado(it))
    }
    Campo(
        "Fecha de nacimiento (AAAA-MM-DD)",
        estado.fechaNacimiento,
        estado.errores.mensajeDe(ErrorFicha.FechaNacimientoInvalida, ErrorFicha.FechaNacimientoFutura),
    ) { onAction(FichaAction.FechaNacimientoCambiada(it)) }
    SelectorIdioma(estado, onAction)

    Text("Si eres menor de 18 años", style = MaterialTheme.typography.titleMedium)
    Campo(
        "Nombre del representante legal",
        estado.representanteNombre,
        estado.errores.mensajeDe(ErrorFicha.RepresentanteRequeridoParaMenor, ErrorFicha.RepresentanteNombreVacio),
    ) { onAction(FichaAction.RepresentanteNombreCambiado(it)) }
    Campo(
        "Correo del representante",
        estado.representanteCorreo,
        estado.errores.mensajeDe(ErrorFicha.RepresentanteCorreoInvalido),
    ) { onAction(FichaAction.RepresentanteCorreoCambiado(it)) }

    ConsentimientoDatos(estado, onAction)

    Button(
        onClick = { onAction(FichaAction.EnviarPulsado) },
        enabled = !estado.enviando,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(if (estado.enviando) "Enviando…" else "Enviar ficha")
    }
}

@Composable
private fun SelectorIdioma(
    estado: FichaUiState,
    onAction: (FichaAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Idioma que quieres aprender", style = MaterialTheme.typography.titleMedium)
        when {
            estado.cargandoIdiomas -> CircularProgressIndicator()
            estado.errorCargaIdiomas -> {
                Text("No pudimos cargar los idiomas.", color = MaterialTheme.colorScheme.error)
                Button(onClick = { onAction(FichaAction.ReintentarCargaIdiomas) }) { Text("Reintentar") }
            }
            else ->
                estado.idiomas.forEach { opcion ->
                    OpcionDeIdioma(opcion, opcion.codigo == estado.idiomaInteres) {
                        onAction(FichaAction.IdiomaInteresCambiado(opcion.codigo))
                    }
                }
        }
        estado.errores.mensajeDe(ErrorFicha.IdiomaInteresVacio, ErrorFicha.IdiomaInteresNoDisponible)?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun OpcionDeIdioma(
    opcion: OpcionIdioma,
    seleccionada: Boolean,
    alElegir: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = seleccionada, onClick = alElegir, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = seleccionada, onClick = null)
        Text(opcion.nombre, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun ConsentimientoDatos(
    estado: FichaUiState,
    onAction: (FichaAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = estado.aceptaTratamientoDatos,
                onCheckedChange = { onAction(FichaAction.TratamientoDatosCambiado(it)) },
            )
            Text(
                "Autorizo el tratamiento de mis datos personales (Ley 1581 de 2012).",
                modifier = Modifier.weight(1f),
            )
        }
        estado.errores.mensajeDe(ErrorFicha.TratamientoDatosNoAceptado)?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun Campo(
    etiqueta: String,
    valor: String,
    error: String?,
    alCambiar: (String) -> Unit,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun Set<ErrorFicha>.mensajeDe(vararg errores: ErrorFicha): String? = errores.firstOrNull { it in this }?.mensaje()
