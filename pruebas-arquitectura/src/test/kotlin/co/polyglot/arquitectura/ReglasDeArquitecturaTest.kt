package co.polyglot.arquitectura

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.ext.list.classes
import com.lemonappdev.konsist.api.ext.list.interfaces
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test
import kotlin.test.assertTrue

/** Hace cumplir en la CI las reglas de dependencias de CLAUDE.md y de los ADR 0004, 0006 y 0009. */
class ReglasDeArquitecturaTest {
    private val archivosDeProduccion: List<KoFileDeclaration> =
        Konsist
            .scopeFromProject()
            .files
            .filter { esCodigoDeProduccion(it.rutaNormalizada()) }

    // Las reglas valen para todo módulo de contexto (matricula, catalogo...) sin listarlos uno por uno.
    private val codigoDeDominio = archivosDeProduccion.deCapa("domain")
    private val codigoDeDatos = archivosDeProduccion.deCapa("data")
    private val codigoDePresentacion = archivosDeProduccion.deCapa("presentation")

    @Test
    fun `las pruebas encuentran codigo de cada capa para revisar`() {
        // Si una ruta cambia y una regla deja de ver archivos, pasaría en verde sin comprobar nada.
        assertTrue(codigoDeDominio.isNotEmpty(), "No se encontró código de dominio")
        assertTrue(codigoDeDatos.isNotEmpty(), "No se encontró código de datos")
        assertTrue(codigoDePresentacion.isNotEmpty(), "No se encontró código de presentación")
    }

    @Test
    fun `cada contexto tiene sus tres capas`() {
        val contextosPorCapa = listOf(codigoDeDominio, codigoDeDatos, codigoDePresentacion).map { it.contextos() }

        assertTrue(contextosPorCapa.distinct().size == 1, "Los contextos por capa no coinciden: $contextosPorCapa")
        assertTrue(contextosPorCapa.first().size >= 2, "Se esperaban al menos matricula y catalogo")
    }

    @Test
    fun `un contexto solo conoce el dominio de otro, nunca sus datos ni su presentacion`() {
        val infractores =
            (codigoDeDominio + codigoDeDatos + codigoDePresentacion)
                .filter { archivo ->
                    val propio = archivo.contexto()
                    archivo.imports.any { import ->
                        IMPORT_DE_CAPA_INTERNA
                            .find(import.name)
                            ?.groupValues
                            ?.get(1)
                            ?.let { it != propio } == true
                    }
                }.rutas()

        assertTrue(infractores.isEmpty(), "Un contexto usa capas internas de otro en: $infractores")
    }

    @Test
    fun `el dominio no importa frameworks ni bibliotecas de plataforma`() {
        val infractores = PREFIJOS_PROHIBIDOS_EN_DOMINIO.flatMap { codigoDeDominio.importan(it) }.rutas()

        assertTrue(infractores.isEmpty(), "El dominio importa frameworks en: $infractores")
    }

    @Test
    fun `la presentacion no conoce la capa de datos ni la red`() {
        val infractores =
            codigoDePresentacion
                .filter { archivo -> archivo.imports.any { PAQUETE_DE_DATOS.containsMatchIn(it.name) || it.name.startsWith("io.ktor.") } }
                .rutas()

        assertTrue(infractores.isEmpty(), "La presentación importa datos o red en: $infractores")
    }

    @Test
    fun `la capa de datos no conoce la presentacion`() {
        val infractores =
            codigoDeDatos
                .filter { archivo -> archivo.imports.any { PAQUETE_DE_PRESENTACION.containsMatchIn(it.name) } }
                .rutas()

        assertTrue(infractores.isEmpty(), "Datos importa presentación en: $infractores")
    }

    @Test
    fun `las interfaces de repositorio viven en el dominio`() {
        val repositorios = archivosDeProduccion.interfaces().filter { it.name.endsWith("Repository") }

        assertTrue(repositorios.isNotEmpty(), "No se encontró ningún repositorio para revisar")
        repositorios.assertTrue { it.path.replace('\\', '/').contains("/domain/") }
    }

    @Test
    fun `los ViewModel no reciben repositorios, solo casos de uso`() {
        val viewModels = archivosDeProduccion.classes().filter { it.name.endsWith("ViewModel") }

        assertTrue(viewModels.isNotEmpty(), "No se encontró ningún ViewModel para revisar")
        viewModels.assertTrue { clase ->
            clase.primaryConstructor
                ?.parameters
                .orEmpty()
                .none { it.type.name.endsWith("Repository") }
        }
    }

    @Test
    fun `el contenedor de dependencias solo se usa en el arranque`() {
        val infractores =
            archivosDeProduccion
                .importan("org.koin.")
                .rutas()
                .filter { ruta -> RUTAS_DE_ARRANQUE.none { ruta.contains(it) } }

        assertTrue(infractores.isEmpty(), "Koin se usa fuera del arranque en: $infractores")
    }

    @Test
    fun `el codigo de produccion no usa el operador de asercion no nula`() {
        val infractores = archivosDeProduccion.filter { it.text.contains("!!") }.rutas()

        assertTrue(infractores.isEmpty(), "Se usa !! en: $infractores")
    }

    private companion object {
        val PREFIJOS_PROHIBIDOS_EN_DOMINIO =
            listOf(
                "android.",
                "androidx.",
                "io.ktor.",
                "kotlinx.serialization.",
                "org.koin.",
                "org.jetbrains.exposed.",
                "org.flywaydb.",
                "java.sql.",
                "javax.sql.",
                "com.zaxxer.",
                "org.postgresql.",
                "platform.",
            )

        val RUTAS_DE_ARRANQUE =
            listOf(
                "/app/src/",
                "/androidApp/src/",
                "/servidor/src/main/kotlin/co/polyglot/servidor/arranque/",
                "/servidor/src/main/kotlin/co/polyglot/servidor/Main.kt",
            )

        val CONJUNTO_DE_PRUEBAS = Regex("/src/[^/]*[tT]est/")

        val PAQUETE_DE_DATOS = Regex("""^co\.polyglot\.[a-z0-9_]+\.data(\.|$)""")
        val PAQUETE_DE_PRESENTACION = Regex("""^co\.polyglot\.[a-z0-9_]+\.presentation(\.|$)""")

        /** Captura el contexto dueño de un import a `data` o `presentation`: `co.polyglot.<contexto>.data...`. */
        val IMPORT_DE_CAPA_INTERNA = Regex("""^co\.polyglot\.([a-z0-9_]+)\.(?:data|presentation)(?:\.|$)""")

        fun esCodigoDeProduccion(ruta: String) =
            ruta.contains("/src/") &&
                !ruta.contains("/build/") &&
                !ruta.contains("/pruebas-arquitectura/") &&
                !CONJUNTO_DE_PRUEBAS.containsMatchIn(ruta)
    }
}

/** Ruta de un módulo de contexto: `/<contexto>/<capa>/src/`. */
private val MODULO_DE_CONTEXTO = Regex("""/([^/]+)/(domain|data|presentation)/src/""")

private fun KoFileDeclaration.rutaNormalizada() = path.replace('\\', '/')

private fun List<KoFileDeclaration>.deCapa(capa: String) = filter { it.moduloDeContexto()?.second == capa }

private fun KoFileDeclaration.moduloDeContexto() =
    MODULO_DE_CONTEXTO
        .find(rutaNormalizada())
        ?.destructured
        ?.let { (contexto, capa) -> contexto to capa }

private fun KoFileDeclaration.contexto() = moduloDeContexto()?.first

private fun List<KoFileDeclaration>.contextos() = mapNotNull { it.contexto() }.toSet()

private fun List<KoFileDeclaration>.importan(prefijo: String) = filter { archivo -> archivo.imports.any { it.name.startsWith(prefijo) } }

private fun List<KoFileDeclaration>.rutas() = map { it.rutaNormalizada() }.distinct()
