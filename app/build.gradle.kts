plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.bookwormconnect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bookwormconnect"
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // ── AndroidX ────────────────────────────────────────────────────────
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)

    // ── Credentials / Identity ───────────────────────────────────────────
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // ── Firebase BOM — manages ALL Firebase versions automatically ────────
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)

    // ── Maps / Location ──────────────────────────────────────────────────
    implementation(libs.play.services.maps)
    implementation(libs.osmdroid.android)

    // ── Networking ───────────────────────────────────────────────────────
    implementation(libs.okhttp)

    // ── Image Loading ─────────────────────────────────────────────────────
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.picasso)
    implementation(libs.circleimageview)

    // ── Scalable size units — declared INLINE so no toml entry needed ─────
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.intuit.ssp:ssp-android:1.1.1")

    // ── Testing ──────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}