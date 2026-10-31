package co.polyglot.matricula.data

import co.polyglot.matricula.domain.InscripcionRepository

/** Punto de entrada de la capa: quien la use solo ve la interfaz del dominio, nunca Ktor. */
fun crearRepositorioInscripciones(urlApi: String): InscripcionRepository = InscripcionApiRepository(crearClienteHttp(), urlApi)
