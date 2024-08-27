plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.rcsdk_camera_example24"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rcsdk_camera_example24"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation (files("libs/rcsdk-v1.5.2.aar"))
    implementation (files("libs/h16_airlink.aar"))
    implementation(files("libs/fpvplayer-v3.0.9.aar"))
    implementation ("org.videolan.android:libvlc-all:4.0.0-eap8")
    implementation(files("libs/sky-ijkplayer-v1.1.aar"))
}