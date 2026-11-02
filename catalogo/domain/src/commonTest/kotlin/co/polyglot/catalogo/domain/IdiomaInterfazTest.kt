package co.polyglot.catalogo.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdiomaInterfazTest {
    @Test
    fun `dado el catalogo de interfaz entonces ofrece espanol, ingles, frances y portugues`() {
        assertEquals(listOf("es", "en", "fr", "pt"), IdiomaInterfaz.entries.map { it.codigoIso })
    }

    @Test
    fun `dado espanol como idioma que se aprende cuando se compara con la interfaz en espanol entonces coinciden`() {
        val espanolParaExtranjeros = idiomaDePrueba(codigo = "es")

        assertTrue(IdiomaInterfaz.ESPANOL.coincideCon(espanolParaExtranjeros))
    }

    @Test
    fun `dado un idioma distinto cuando se compara con la interfaz en espanol entonces no coinciden`() {
        assertFalse(IdiomaInterfaz.ESPANOL.coincideCon(idiomaDePrueba(codigo = "de")))
    }
}
