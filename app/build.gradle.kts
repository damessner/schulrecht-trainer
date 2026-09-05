plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "at.schulrecht.trainer"
    compileSdk = 35

    defaultConfig {
        applicationId = "at.schulrecht.trainer"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "1.6.1"
    }

    signingConfigs {
        create("release") {
            val storePath = System.getenv("ANDROID_KEYSTORE_FILE")
                ?: (project.findProperty("TRAINER_STORE_FILE") as String?)
            val pwPath = System.getenv("ANDROID_KEYSTORE_PASSWORD_FILE")
                ?: (project.findProperty("TRAINER_STORE_PASSWORD_FILE") as String?)
            val pwEnv = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            if (storePath != null) {
                val pw = pwEnv ?: pwPath?.let { file(it).readText().trim() }
                if (pw != null) {
                    storeFile = file(storePath)
                    storePassword = pw
                    keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                        ?: (project.findProperty("TRAINER_KEY_ALIAS") as String?)
                        ?: "trainer"
                    keyPassword = pw
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            val storePath = System.getenv("ANDROID_KEYSTORE_FILE")
                ?: (project.findProperty("TRAINER_STORE_FILE") as String?)
            if (System.getenv("CI") == "true" && storePath == null) {
                throw GradleException("ANDROID_KEYSTORE_FILE fehlt in CI – Release wäre unsigniert!")
            }
            if (storePath != null && file(storePath).exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)
    implementation(libs.okhttp)
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    testImplementation(libs.junit)
}
