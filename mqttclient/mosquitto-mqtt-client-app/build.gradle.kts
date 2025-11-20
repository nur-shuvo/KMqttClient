plugins {
    alias(libs.plugins.android.library)
    id("org.jetbrains.kotlin.kapt")
    kotlin("android")
    `maven-publish`
}

version = "1.0.0"
group = "com.github.nurshuvo"

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
                cppFlags += listOf("-std=c++17", "-fexceptions", "-frtti")
                arguments += listOf(
                    "-DANDROID_STL=c++_static",
                    "-DANDROID_DEBUG=1"
                )
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
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

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.github.nurshuvo"
                artifactId = "kmqtt"
                version = "1.0.0"

                from(components["release"])
            }
        }
    }
}
