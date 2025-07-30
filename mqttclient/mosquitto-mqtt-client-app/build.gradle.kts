plugins {
    alias(libs.plugins.android.library)
    id("org.jetbrains.kotlin.kapt")
    kotlin("android")
}

version = "4.0.0"

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "com.nurshuvo.kmqtt"
    compileSdk = 34

    defaultConfig {
        ndkVersion = "29.0.13599879"
        minSdk = 29
        consumerProguardFiles("consumer-proguard-rules.pro")

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-fexceptions", "-frtti")
            }
        }

        ndk {
            abiFilters += listOf("arm64-v8a") // Add others like "armeabi-v7a" if needed
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    lint {
        disable += "PackagedPrivateKey"
    }

    sourceSets {
        getByName("debug") {
            res.srcDirs("src/debug/res")
        }
    }
}

dependencies {
    implementation(libs.hilt.android)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
}
