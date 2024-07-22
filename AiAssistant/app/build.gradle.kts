import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.aiassistant"
    compileSdk = 34
    android.buildFeatures.buildConfig = true
    defaultConfig {
        applicationId = "com.example.aiassistant"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add OpenAI API keys in courtesy of Antony Muchiri
        // https://medium.com/@vontonnie/secure-api-keys-in-android-projects-f8eb4839701d
        val keystoreFile = project.rootProject.file("apikeys.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        val ORGANIZATION_KEY = properties.getProperty("ORGANIZATION_KEY") ?: ""
        val PROJECT_KEY = properties.getProperty("PROJECT_KEY") ?: ""
        val OPENAI_API_KEY = properties.getProperty("OPENAI_API_KEY") ?: ""

        buildConfigField(
                type = "String",
                name = "OPENAI_API_KEY",
                value = OPENAI_API_KEY
        )
        buildConfigField(
            type = "String",
            name = "PROJECT_KEY",
            value = PROJECT_KEY
        )
        buildConfigField(
            type = "String",
            name = "ORGANIZATION_KEY",
            value = ORGANIZATION_KEY
        )

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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}