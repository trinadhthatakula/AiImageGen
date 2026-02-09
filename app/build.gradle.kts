import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val secretsPropertiesFile: File = rootProject.file("secrets.properties")
val secretsProperties = Properties()
if (secretsPropertiesFile.exists()) {
    secretsProperties.load(FileInputStream(secretsPropertiesFile))
}

android {
    namespace = "com.ai.image.gen"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.ai.image.gen"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val token = secretsProperties.getProperty("HF_TOKEN")
        if (token.isNullOrEmpty()) {
            logger.error(
                "\n" +
                        "\u001B[31m============================================================================\u001B[0m\n" +
                        "\u001B[31m🛑 SECURITY ALERT: HF_TOKEN MISSING 🛑\u001B[0m\n" +
                        "\u001B[31m----------------------------------------------------------------------------\u001B[0m\n" +
                        "The app will compile, but Image Generation features will CRASH at runtime.\n" +
                        "Please create 'secrets.properties' in the project root with:\n" +
                        "HF_TOKEN=hf_your_token_here\n\n" +
                        "👉 Get your FREE token here: https://huggingface.co/settings/tokens\n" +
                        "\u001B[31m============================================================================\u001B[0m\n"
            )
            buildConfigField("String", "HF_TOKEN", "\"\"")
        } else {
            logger.lifecycle(
                "\n" +
                        "\u001B[32m============================================================================\u001B[0m\n" +
                        "\u001B[32m✅ SECURITY: HF_TOKEN FOUND\u001B[0m\n" +
                        "\u001B[32m----------------------------------------------------------------------------\u001B[0m\n" +
                        "Token injection successful. The Forge is ready.\n" +
                        "\u001B[32m============================================================================\u001B[0m\n"
            )
            buildConfigField("String", "HF_TOKEN", "\"$token\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.koin.androidx.workmanager)

    implementation(libs.koin.androidx.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.gson) // For JSON request body
    implementation(libs.coil.compose)
    implementation(libs.logging.interceptor)

}