kotlin
// C:/Users/User/Documents/UniTrade/app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.unitrade"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.unitrade"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation("com.google.android.material:material:1.13.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.google.mlkit:translate:17.0.2")

    // Add these lines for Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.play.services.location)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Add this line for MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
