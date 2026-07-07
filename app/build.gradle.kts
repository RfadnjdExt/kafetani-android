plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kafetani.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kafetani.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // ⚠️ URL server Laravel — SESUAIKAN dengan environment testing-mu:
        //   • Emulator Android Studio → biarkan seperti ini ("10.0.2.2" = alias
        //     bawaan emulator untuk mengakses localhost laptop kamu)
        //   • HP Android fisik (satu jaringan Wi-Fi dengan laptop) → ganti jadi
        //     IP lokal laptop, misalnya "http://192.168.1.10:8000/"
        //   • Nanti kalau sudah online/production → ganti ke domain aslinya
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")

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
        debug {
            // Izinkan traffic HTTP polos (non-HTTPS) saat debug, karena kita
            // testing ke server lokal (Laravel `php artisan serve` tidak pakai SSL).
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))

    // Core & lifecycle
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigasi antar layar
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Networking ke backend Laravel
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Simpan token login secara persisten di device
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Load gambar produk/petani dari URL
    implementation("io.coil-kt:coil-compose:2.7.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
