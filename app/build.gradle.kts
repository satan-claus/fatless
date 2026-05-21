plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.niked.fatless"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.niked.fatless"
        minSdk = 29
        targetSdk = 36
        versionCode = 81
        versionName = "1.0.81"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "IS_DEBUG", "true")
            isMinifyEnabled = false
            isShrinkResources = false
        }

        release {
            buildConfigField("boolean", "IS_DEBUG", "false")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
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

    // Gson
    implementation(libs.google.gson)

    // HILT (ключи в TOML: hilt-android, hilt-compiler, hilt-navigation-compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.lifecycle.service)
    "ksp"(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // ROOM (в TOML ключи: room-runtime, room-ktx, room-compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    "ksp"(libs.room.compiler)

    // NAVIGATION (в TOML ключ: navigation-compose)
    implementation(libs.navigation.compose)

    // Workmanager
    implementation(libs.androidx.work.runtime.ktx)

    // SplashScreen
    implementation(libs.androidx.core.splashscreen)

    // Material 3
    implementation(libs.google.material)

    // Карты для GPS-трекера
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Widget
    implementation("androidx.glance:glance-appwidget:1.0.0")

    // Тесты
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}