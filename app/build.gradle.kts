plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.kharcha"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kharcha"
        minSdk = 24
        targetSdk = 35
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
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase BoM (Manages versions for all Firebase dependencies)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    // Firebase Dependencies (BoM handles versions)
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore")

    // Material Components (Remove redundant implementation)
    implementation("com.google.android.material:material:1.9.0")


    // Testing Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
