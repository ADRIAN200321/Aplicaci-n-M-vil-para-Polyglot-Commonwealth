package co.polyglot.app

import androidx.compose.ui.window.ComposeUIViewController

/** Lo llama la aplicación de iOS para mostrar la interfaz común. */
@Suppress("FunctionName")
fun MainViewController(urlApi: String) = ComposeUIViewController { App(urlApi) }
