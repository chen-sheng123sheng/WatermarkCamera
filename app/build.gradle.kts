plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.watermarkcamera"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.watermarkcamera"
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Android基础库
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // CameraX依赖 - 现代相机开发的最佳选择
    // 核心库：提供基础的相机功能
    implementation(libs.androidx.camera.core)
    // Camera2实现：使用Camera2 API作为底层实现，性能更好
    implementation(libs.androidx.camera.camera2)
    // 生命周期库：自动管理相机的生命周期，避免内存泄漏
    implementation(libs.androidx.camera.lifecycle)
    // 视图库：提供PreviewView等UI组件
    implementation(libs.androidx.camera.view)
    // 扩展库：提供夜景、人像等高级功能（可选）
    implementation(libs.androidx.camera.extensions)
    implementation(libs.camera.view)

    // ExifInterface：读取和处理图片EXIF信息
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}