plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// En el emulador de Android, 10.0.2.2 es el computador donde corre el servidor.
// Para otro servidor: ./gradlew :androidApp:assembleDebug -PurlApi=https://...
private val urlApi = providers.gradleProperty("urlApi").orElse("http://10.0.2.2:8080")

android {
    namespace = "co.polyglot.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "co.polyglot.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "URL_API", "\"${urlApi.get()}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":app"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
}
