import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.konsist)
}

tasks.test {
    useJUnitPlatform()

    // Konsist lee el código de todos los módulos; sin declararlo, Gradle daría la prueba por vigente aunque cambien.
    inputs
        .files(
            rootProject.fileTree(rootProject.projectDir) {
                include("**/src/**/*.kt")
                exclude("**/build/**")
            },
        ).withPropertyName("codigoFuente")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
