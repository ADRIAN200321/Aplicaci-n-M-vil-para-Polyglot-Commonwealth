package co.polyglot.matricula.domain

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ValidadorFichaTest {
    private val hoy = LocalDate(2026, 10, 30)
    private val validador = ValidadorFicha()

    private fun borradorAdulto() =
        BorradorFicha(
            nombres = "  Ana María ",
            apellidos = "Prueba Ficticia",
            correo = "Ana@Ejemplo.test ",
            fechaNacimiento = "2000-05-10",
            idiomaInteres = "idioma-1",
            representante = null,
            aceptaTratamientoDatos = true,
        )

    private fun erroresDe(borrador: BorradorFicha): List<ErrorFicha> =
        assertIs<ResultadoValidacion.Invalida>(validador.validar(borrador, hoy)).errores

    @Test
    fun `dado un adulto con datos correctos cuando se valida entonces la ficha es valida y normalizada`() {
        val resultado = assertIs<ResultadoValidacion.Valida>(validador.validar(borradorAdulto(), hoy))

        assertEquals("Ana María", resultado.ficha.nombres)
        assertEquals("ana@ejemplo.test", resultado.ficha.correo.valor)
        assertNull(resultado.ficha.representante)
    }

    @Test
    fun `dado un menor sin representante cuando se valida entonces exige representante legal`() {
        val menor = borradorAdulto().copy(fechaNacimiento = "2012-01-01")

        assertEquals(listOf(ErrorFicha.RepresentanteRequeridoParaMenor), erroresDe(menor))
    }

    @Test
    fun `dado un menor con representante completo cuando se valida entonces es valida`() {
        val menor =
            borradorAdulto().copy(
                fechaNacimiento = "2012-01-01",
                representante = BorradorRepresentante("Luis Ficticio", "luis@ejemplo.test"),
            )

        val resultado = assertIs<ResultadoValidacion.Valida>(validador.validar(menor, hoy))

        assertEquals("Luis Ficticio", resultado.ficha.representante?.nombreCompleto)
    }

    @Test
    fun `dado alguien que cumple 18 hoy cuando se valida entonces ya no necesita representante`() {
        val cumpleHoy = borradorAdulto().copy(fechaNacimiento = "2008-10-30")

        assertIs<ResultadoValidacion.Valida>(validador.validar(cumpleHoy, hoy))
    }

    @Test
    fun `dado alguien que cumple 18 manana cuando se valida entonces sigue siendo menor`() {
        val cumpleManana = borradorAdulto().copy(fechaNacimiento = "2008-10-31")

        assertEquals(listOf(ErrorFicha.RepresentanteRequeridoParaMenor), erroresDe(cumpleManana))
    }

    @Test
    fun `dado un representante incompleto cuando se valida entonces informa nombre y correo`() {
        val menor =
            borradorAdulto().copy(
                fechaNacimiento = "2012-01-01",
                representante = BorradorRepresentante(" ", "no-es-correo"),
            )

        assertEquals(
            listOf(ErrorFicha.RepresentanteNombreVacio, ErrorFicha.RepresentanteCorreoInvalido),
            erroresDe(menor),
        )
    }

    @Test
    fun `dado varios datos incorrectos cuando se valida entonces devuelve todos los errores juntos`() {
        val vacio =
            BorradorFicha(
                nombres = "",
                apellidos = " ",
                correo = "x",
                fechaNacimiento = "ayer",
                idiomaInteres = "",
                representante = null,
                aceptaTratamientoDatos = false,
            )

        assertEquals(
            listOf(
                ErrorFicha.NombresVacios,
                ErrorFicha.ApellidosVacios,
                ErrorFicha.CorreoInvalido,
                ErrorFicha.IdiomaInteresVacio,
                ErrorFicha.TratamientoDatosNoAceptado,
                ErrorFicha.FechaNacimientoInvalida,
            ),
            erroresDe(vacio),
        )
    }

    @Test
    fun `dado una fecha de nacimiento futura cuando se valida entonces se rechaza`() {
        val futuro = borradorAdulto().copy(fechaNacimiento = "2027-01-01")

        assertEquals(listOf(ErrorFicha.FechaNacimientoFutura), erroresDe(futuro))
    }

    @Test
    fun `dado que no acepta el tratamiento de datos cuando se valida entonces se rechaza`() {
        val sinConsentimiento = borradorAdulto().copy(aceptaTratamientoDatos = false)

        assertEquals(listOf(ErrorFicha.TratamientoDatosNoAceptado), erroresDe(sinConsentimiento))
    }
}
