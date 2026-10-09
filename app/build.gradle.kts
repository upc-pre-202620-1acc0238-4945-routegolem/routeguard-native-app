import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// Public Mapbox token (pk.*): put MAPBOX_ACCESS_TOKEN=pk... in local.properties (never commit it).
val mapboxAccessToken: String = rootProject.file("local.properties")
    .takeIf { it.exists() }
    ?.inputStream()?.use { Properties().apply { load(it) } }
    ?.getProperty("MAPBOX_ACCESS_TOKEN")
    ?: System.getenv("MAPBOX_ACCESS_TOKEN")
    ?: ""

// Backend URL. Defaults to the deployed API on Azure; to use a backend running on this PC add
// API_BASE_URL=http://localhost:8080/api/v1/ to local.properties (never committed) and run `adb reverse tcp:8080 tcp:8080`.
val apiBaseUrl: String = rootProject.file("local.properties")
    .takeIf { it.exists() }
    ?.inputStream()?.use { Properties().apply { load(it) } }
    ?.getProperty("API_BASE_URL")
    ?: System.getenv("API_BASE_URL")
    ?: "https://routeguard-api-mobiles-a3cza3byc3b3dpfs.brazilsouth-01.azurewebsites.net/api/v1/"

android {
    namespace = "pe.edu.upc.routeguard"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "pe.edu.upc.routeguard"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl.trimEnd('/')}/\"")
        resValue("string", "mapbox_access_token", mapboxAccessToken.ifBlank { "MISSING_MAPBOX_TOKEN" })

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)


    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Mapbox
    implementation(libs.mapbox.maps)
    implementation(libs.mapbox.maps.compose)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
}