// Configura la aplicación Android, sus credenciales de compilación y dependencias.
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.legacy.kapt)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.app.protrack"
    compileSdk = 37

    val envFile = rootProject.file(".env")
    val env = Properties()
    if (envFile.exists()) {
        env.load(envFile.inputStream())
    }

    defaultConfig {
        applicationId = "com.app.protrack"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "RESEND_API_KEY", "\"${env.getProperty("RESEND_API_KEY") ?: ""}\"")
        buildConfigField("String", "FROM_EMAIL", "\"${env.getProperty("FROM_EMAIL") ?: ""}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.print)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.material)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.4.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.0")

    // ViewModel y Corrutinas
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Coil (Recomendado en Kotlin para cargar imágenes desde URLs)
    implementation("io.coil-kt:coil:2.4.0")
    implementation("com.cloudinary:cloudinary-android:2.5.0")
    
    // Gson para persistencia simple
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
