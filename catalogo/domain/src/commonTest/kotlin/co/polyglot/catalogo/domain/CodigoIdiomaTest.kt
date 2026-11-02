package co.polyglot.catalogo.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CodigoIdiomaTest {
    @Test
    fun `dado un codigo de dos letras cuando se interpreta entonces lo acepta`() {
        assertEquals("en", CodigoIdioma.desde("en")?.valor)
    }

    @Test
    fun `dado un codigo con mayusculas y espacios cuando se interpreta entonces lo normaliza`() {
        assertEquals("fr", CodigoIdioma.desde("  FR ")?.valor)
    }

    @Test
    fun `dado un texto que no es un codigo de dos letras cuando se interpreta entonces lo rechaza`() {
        listOf("", "   ", "e", "eng", "e1", "e n", "ñé", "idioma-1").forEach { texto ->
            assertNull(CodigoIdioma.desde(texto), "Debía rechazar \"$texto\"")
        }
    }
}
