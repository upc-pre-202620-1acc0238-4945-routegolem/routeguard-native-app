import java.util.Properties

plugins {
    alias(libs.plugins.google.services)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun resolveConfigValue(key: String, defaultValue: String = ""): String {
    return localProperties.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }
        ?: System.getenv(key)?.trim()?.takeIf { it.isNotEmpty() }
        ?: defaultValue
}

// Public Mapbox token (pk.*): put MAPBOX_ACCESS_TOKEN=pk... in local.properties (never commit it).
val mapboxAccessToken: String = resolveConfigValue("MAPBOX_ACCESS_TOKEN")

// Backend URL. Defaults to the deployed API on Render; to use a backend running on this PC add
// API_BASE_URL=http://localhost:8080/api/v1/ to local.properties (never committed) and run `adb reverse tcp:8080 tcp:8080`.
val apiBaseUrl: String = resolveConfigValue(
    key = "API_BASE_URL",
    defaultValue = "https://routeguard-web-services.onrender.com/api/v1/"
)

val stripePublishableKey: String = resolveConfigValue(
    key = "STRIPE_PUBLISHABLE_KEY",
    defaultValue = "pk_test_51UPqsJ2cXM5Q72HKBRhXS93oFgiLy5s4YpkEuoEY7EXcgl7cRnxSJwkqNOwHw2PioQ5p5jucV2FgtKdnMdk0rvUR00D9Goup5m"
)

android {
    namespace = "pe.edu.upc.routeguard"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "pe.edu.upc.routeguard"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "1.0.1"

        val normalizedApiBaseUrl = apiBaseUrl
            .takeIf { it.startsWith("http://") || it.startsWith("https://") }
            ?: "https://routeguard-web-services.onrender.com/api/v1/"

        buildConfigField("String", "API_BASE_URL", "\"${normalizedApiBaseUrl.trimEnd('/')}/\"")
        buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"$stripePublishableKey\"")
        resValue("string", "mapbox_access_token", mapboxAccessToken.ifBlank { "MISSING_MAPBOX_TOKEN" })

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
        debug {
            packaging {
                jniLibs.keepDebugSymbols.add("**/*.so")
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

    // Firebase
    implementation(libs.firebase.messaging)

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
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Stripe
    implementation(libs.stripe.android)
}