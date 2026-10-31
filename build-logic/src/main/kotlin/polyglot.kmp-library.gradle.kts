import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

private val PAQUETE_BASE = "co.polyglot"
private val ANDROID_COMPILE_SDK = 37
private val ANDROID_MIN_SDK = 26

kotlin {
    android {
        namespace = PAQUETE_BASE + project.path.replace(":", ".").replace("-", "")
        compileSdk = ANDROID_COMPILE_SDK
        minSdk = ANDROID_MIN_SDK
    }
    jvm {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
    iosArm64()
    iosSimulatorArm64()

    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
