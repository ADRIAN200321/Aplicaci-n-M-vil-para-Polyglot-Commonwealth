package co.polyglot.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import co.polyglot.catalogo.presentation.ProgramasScreen
import co.polyglot.catalogo.presentation.ProgramasViewModel
import co.polyglot.matricula.presentation.FichaInscripcionScreen
import co.polyglot.matricula.presentation.FichaInscripcionViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

private enum class Destino(
    val titulo: String,
) {
    PROGRAMAS("Programas"),
    INSCRIPCION("Inscripción"),
}

/** Punto de entrada común a Android e iOS. Cada plataforma indica a qué servidor conectarse. */
@Composable
fun App(urlApi: String) {
    val configuracion = remember(urlApi) { koinConfiguration { modules(moduloApp(urlApi)) } }
    KoinApplication(configuracion) {
        MaterialTheme {
            // Se guarda el índice y no el enum: rememberSaveable solo sabe guardar tipos simples en común.
            var indiceActual by rememberSaveable { mutableIntStateOf(Destino.PROGRAMAS.ordinal) }
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        Destino.entries.forEach { destino ->
                            NavigationBarItem(
                                selected = indiceActual == destino.ordinal,
                                onClick = { indiceActual = destino.ordinal },
                                icon = {},
                                label = { Text(destino.titulo) },
                            )
                        }
                    }
                },
            ) { relleno ->
                Box(Modifier.padding(relleno)) {
                    when (Destino.entries[indiceActual]) {
                        Destino.PROGRAMAS -> Programas()
                        Destino.INSCRIPCION -> Inscripcion()
                    }
                }
            }
        }
    }
}

@Composable
private fun Programas() {
    val viewModel = koinViewModel<ProgramasViewModel>()
    val estado by viewModel.estado.collectAsState()
    ProgramasScreen(estado = estado, onAction = viewModel::onAction)
}

@Composable
private fun Inscripcion() {
    val viewModel = koinViewModel<FichaInscripcionViewModel>()
    val estado by viewModel.estado.collectAsState()
    FichaInscripcionScreen(
        estado = estado,
        efectos = viewModel.efectos,
        onAction = viewModel::onAction,
    )
}
