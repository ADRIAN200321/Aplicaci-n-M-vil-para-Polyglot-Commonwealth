package co.polyglot.catalogo.data

import co.polyglot.catalogo.domain.IdiomaRepository

/** Punto de entrada de la capa: quien la use solo ve la interfaz del dominio, nunca Ktor. */
fun crearRepositorioIdiomas(urlApi: String): IdiomaRepository = IdiomaApiRepository(crearClienteHttp(), urlApi)
