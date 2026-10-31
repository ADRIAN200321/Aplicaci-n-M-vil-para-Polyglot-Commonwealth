package co.polyglot.servidor.arranque

import co.polyglot.matricula.domain.EnviarFichaInscripcion
import co.polyglot.matricula.domain.InscripcionRepository
import co.polyglot.matricula.domain.Reloj
import co.polyglot.matricula.domain.RelojDelSistema
import co.polyglot.matricula.domain.ValidadorFicha
import co.polyglot.servidor.datos.ConexionBaseDeDatos
import co.polyglot.servidor.datos.ConfiguracionBaseDeDatos
import co.polyglot.servidor.datos.InscripcionExposedRepository
import org.koin.dsl.module

/** Único lugar del servidor donde se ensamblan las dependencias. */
val moduloServidor =
    module {
        single { ConfiguracionBaseDeDatos.desdeEntorno() }
        single { ConexionBaseDeDatos(get()) }
        single<InscripcionRepository> { InscripcionExposedRepository(get()) }
        single<Reloj> { RelojDelSistema() }
        single { ValidadorFicha() }
        factory { EnviarFichaInscripcion(get(), get(), get()) }
    }
