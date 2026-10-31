package co.polyglot.matricula.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** Fecha actual inyectable, para que las reglas que dependen de "hoy" se puedan probar. */
interface Reloj {
    fun hoy(): LocalDate
}

class RelojDelSistema(
    private val zona: TimeZone = TimeZone.currentSystemDefault(),
) : Reloj {
    override fun hoy(): LocalDate = Clock.System.todayIn(zona)
}
