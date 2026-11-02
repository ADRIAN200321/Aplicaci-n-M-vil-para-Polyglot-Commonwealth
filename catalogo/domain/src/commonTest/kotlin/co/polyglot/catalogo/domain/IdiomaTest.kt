package co.polyglot.catalogo.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IdiomaTest {
    @Test
    fun `dado un idioma con nivel maximo C1 cuando se piden sus niveles entonces van de A1 a C1`() {
        val idioma = idiomaDePrueba(nivelMaximo = NivelMcer.C1)

        assertEquals(listOf(NivelMcer.A1, NivelMcer.A2, NivelMcer.B1, NivelMcer.B2, NivelMcer.C1), idioma.niveles)
    }

    @Test
    fun `dado un idioma con nivel maximo B2 cuando se consulta un nivel superior entonces no lo ofrece`() {
        val idioma = idiomaDePrueba(nivelMaximo = NivelMcer.B2)

        assertTrue(idioma.ofrece(NivelMcer.B2))
        assertFalse(idioma.ofrece(NivelMcer.C1))
    }

    @Test
    fun `dado el nombre de un nivel cuando se interpreta entonces devuelve el nivel o nada`() {
        assertEquals(NivelMcer.B1, NivelMcer.desde(" b1 "))
        assertNull(NivelMcer.desde("D1"))
    }
}
