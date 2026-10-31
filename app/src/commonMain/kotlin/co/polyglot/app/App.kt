package co.polyglot.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import co.polyglot.matricula.presentation.FichaInscripcionScreen
import co.polyglot.matricula.presentation.FichaInscripcionViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

/** Punto de entrada común a Android e iOS. Cada plataforma indica a qué servidor conectarse. */
@Composable
fun App(urlApi: String) {
    val configuracion = remember(urlApi) { koinConfiguration { modules(moduloApp(urlApi)) } }
    KoinApplication(configuracion) {
        MaterialTheme {
            val viewModel = koinViewModel<FichaInscripcionViewModel>()
            val estado by viewModel.estado.collectAsState()
            FichaInscripcionScreen(
                estado = estado,
                efectos = viewModel.efectos,
                onAction = viewModel::onAction,
            )
        }
    }
}
