package co.polyglot.app

import co.polyglot.matricula.data.crearRepositorioInscripciones
import co.polyglot.matricula.domain.EnviarFichaInscripcion
import co.polyglot.matricula.domain.InscripcionRepository
import co.polyglot.matricula.domain.Reloj
import co.polyglot.matricula.domain.RelojDelSistema
import co.polyglot.matricula.domain.ValidadorFicha
import co.polyglot.matricula.presentation.FichaInscripcionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Único lugar del cliente donde se ensamblan las dependencias de los tres niveles de `matricula`. */
fun moduloApp(urlApi: String) =
    module {
        single<InscripcionRepository> { crearRepositorioInscripciones(urlApi) }
        single<Reloj> { RelojDelSistema() }
        single { ValidadorFicha() }
        factory { EnviarFichaInscripcion(get(), get(), get()) }
        viewModel { FichaInscripcionViewModel(get()) }
    }
