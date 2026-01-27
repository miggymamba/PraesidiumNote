import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.miguelrivera.praesidiumnote"
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        applicationId = "com.miguelrivera.praesidiumnote"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()

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
    buildFeatures {
        compose = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xmx2g", "-noverify")
            }
        }
    }

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(libs.kotlinx.serialization.json)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.graphics)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.core.splashscreen)
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.debug.compose.ui.test.manifest)

        /* Hilt Dependencies */
        implementation(libs.hilt.android)
        implementation(libs.hilt.navigation.compose)
        ksp(libs.hilt.compiler)
        ksp(libs.hilt.android.compiler)

        /* Room */
        implementation(libs.room.runtime)
        implementation(libs.room.ktx)
        ksp(libs.room.compiler)

        /* Navigation */
        implementation(libs.androidx.navigation.compose)

        /* Coroutines */
        implementation(libs.coroutines.android)

        /* Security */
        implementation(libs.androidx.biometric)
        implementation(libs.sqlcipher.android)

        /* Testing */
        testImplementation(libs.test.mockk)
        testImplementation(libs.test.junit)
        testImplementation(libs.coroutines.test)
        testImplementation(libs.test.truth)
        testImplementation(libs.test.navigation)
        testImplementation(libs.test.robolectric)
        testImplementation(libs.test.androidx.core)
        androidTestImplementation(libs.test.androidx.junit)
        androidTestImplementation(libs.test.androidx.espresso)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.debug.compose.ui.test.junit4)
    }
}