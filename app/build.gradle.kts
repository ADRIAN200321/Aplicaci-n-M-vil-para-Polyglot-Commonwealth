import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("polyglot.kmp-library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    // Aquí convergen Compose y Koin, que publican copias distintas de androidx (aviso "KLIB loader").
    // No es código nuestro; los demás módulos siguen con avisos como errores.
    compilerOptions {
        allWarningsAsErrors.set(false)
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":matricula:domain"))
            implementation(project(":matricula:data"))
            implementation(project(":matricula:presentation"))
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.koin.test)
        }
    }
}
