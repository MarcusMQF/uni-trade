// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
<<<<<<< HEAD:UniTrade/build.gradle.kts
    alias(libs.plugins.google.gms.google.services) apply false // Change this line

=======
    alias(libs.plugins.google.services) apply false  // Add this line
>>>>>>> e5f74feaef8bb130f9666dcfeff225db8c530269:build.gradle.kts
}
