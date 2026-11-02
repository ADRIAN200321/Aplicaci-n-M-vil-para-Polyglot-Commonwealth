package co.polyglot.catalogo.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Pantalla sin estado propio: recibe el estado y entrega las acciones. La conecta quien la usa. */
@Composable
fun ProgramasScreen(
    estado: ProgramasUiState,
    onAction: (ProgramasAction) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Nuestros programas", style = MaterialTheme.typography.headlineMedium)
        when {
            estado.cargando -> CircularProgressIndicator()
            estado.error != null -> ErrorDeCarga(estado.error, onAction)
            else -> estado.programas.forEach { Programa(it) }
        }
    }
}

@Composable
private fun ErrorDeCarga(
    error: ErrorProgramas,
    onAction: (ProgramasAction) -> Unit,
) {
    Text(error.mensaje(), color = MaterialTheme.colorScheme.error)
    Button(onClick = { onAction(ProgramasAction.Reintentar) }) {
        Text("Reintentar")
    }
}

@Composable
private fun Programa(programa: ProgramaUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(programa.nombre, style = MaterialTheme.typography.titleLarge)
            Text("Niveles: ${programa.niveles}")
            if (programa.certificaciones.isNotEmpty()) {
                Text("Preparación para: ${programa.certificaciones.joinToString()}")
            }
        }
    }
}

private fun ErrorProgramas.mensaje(): String =
    when (this) {
        ErrorProgramas.SIN_CONEXION -> "No hay conexión con el servidor. Inténtalo de nuevo."
        ErrorProgramas.INESPERADO -> "Algo salió mal al cargar los programas. Inténtalo más tarde."
    }
