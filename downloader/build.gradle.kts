plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
}

android {
    namespace = "cn.booslink.llm.downloader"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 19

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.hilt)
    annotationProcessor(libs.hilt.compiler)

    implementation(libs.bundles.downloader)

    implementation(libs.bundles.rxjava)
    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}