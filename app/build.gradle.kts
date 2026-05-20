import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

val localProperties = Properties()
rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { localProperties.load(it) }

android {
    namespace = "com.fit5046.wattwise"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fit5046.wattwise"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "DEFAULT_WEB_CLIENT_ID", "\"${localProperties.getProperty("DEFAULT_WEB_CLIENT_ID", "")}\"")
        buildConfigField("String", "WEATHER_API_KEY", "\"${localProperties.getProperty("WEATHER_API_KEY", "")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
    androidResources {
        noCompress += "tflite"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.compose.remote.creation.core)
    ksp(libs.room.compiler)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.firebase:firebase-firestore:25.1.4")
    implementation(libs.work.runtime)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    }
