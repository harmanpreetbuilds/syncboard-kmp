import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }

    dependencies {
        implementation(projects.sharedLogic)

        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.activity.compose)

        implementation(libs.koin.android)

        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.ui)
        implementation(libs.compose.material3)
        implementation(libs.compose.ui.tooling.preview)

        debugImplementation(libs.compose.ui.tooling)
    }
}

android {
    namespace = "com.syncboard.app"

    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.syncboard.app"

        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()

        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
