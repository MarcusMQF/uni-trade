import java.util.Properties

fun loadEnvFile(envFile: File) {
    if (!envFile.exists()) return
    val props = Properties()
    envFile.inputStream().use { props.load(it) }
    props.forEach { key, value ->
        if (!project.hasProperty(key.toString())) {
            project.extensions.extraProperties.set(key.toString(), value.toString())
        }
    }
}

loadEnvFile(rootProject.file(".env"))

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)  // Use the alias from libs.versions.toml
    alias(libs.plugins.google.gms.google.services)  // Only need this once
}

android {
    namespace = "com.example.unitrade"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.unitrade"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val supabaseUrl = project.findProperty("NEXT_PUBLIC_SUPABASE_URL")?.toString()
            ?: project.findProperty("SUPABASE_URL")?.toString()
            ?: ""
        val supabaseKey = project.findProperty("NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY")?.toString()
            ?: project.findProperty("SUPABASE_ANON_KEY")?.toString()
            ?: ""

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/ASL2.0"
        }
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
    kotlinOptions {
        jvmTarget = "11"
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
    implementation(libs.play.services.location)

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.firebase:firebase-messaging")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Firebase - Using BOM for version management
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)

    // Supabase
    implementation(libs.supabase.kt)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //FCM
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

}