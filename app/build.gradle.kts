
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") // 添加 Google Services 插件
    id("kotlin-kapt") // 啟用 kapt 插件
}

android {
    namespace = "com.example.itcough"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.example.itcough"
        minSdk = 26
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    androidResources {
        noCompress += "tflite"
    }
}

dependencies {
    implementation("com.arthenica:ffmpeg-kit-full:6.0.LTS")
    implementation("com.google.android.material:material:1.9.0")  // Material Design 组件
    implementation("androidx.viewpager2:viewpager2:1.0.0")  // ViewPager2
    implementation(libs.gson)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.material)
    implementation(libs.androidx.room.ktx)
    implementation(libs.tensorflow.lite) // 引用 TensorFlow Lite
    implementation(libs.tensorflow.lite.task.audio) // 引用 TensorFlow Lite Task Audio
    implementation(libs.googlesignin)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.play.services.auth.v2130)
    implementation(libs.glide)
    kapt(libs.compiler)
}
