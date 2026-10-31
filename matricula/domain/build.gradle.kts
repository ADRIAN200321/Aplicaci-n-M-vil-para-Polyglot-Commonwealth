plugins {
    id("polyglot.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Biblioteca de Kotlin puro (sin framework): fechas para la regla de mayoría de edad.
            api(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
