plugins {
    id("polyglot.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Solo el dominio público del catálogo: la ficha lo expone (CodigoIdioma), por eso es `api`.
            api(project(":catalogo:domain"))
            // Biblioteca de Kotlin puro (sin framework): fechas para la regla de mayoría de edad.
            api(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
