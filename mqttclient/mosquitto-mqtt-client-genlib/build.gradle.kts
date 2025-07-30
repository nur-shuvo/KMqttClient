plugins {
    alias(libs.plugins.android.library)
}

version = "4.0.0"

android {
    namespace = "com.nurshuvo.kmqtt.genlib"

    defaultConfig {
        consumerProguardFiles("consumer-proguard-rules.pro")

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                targets("mosquitto_project")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
        }
    }
}
