plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
}

android {
    namespace = "cn.booslink.llm.speech"
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
        create("system") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    androidComponents.beforeVariants { variantBuilder ->
        variantBuilder.enable = variantBuilder.buildType != "debug"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    compileOnly(libs.iflytek.sdk)

    compileOnly(project(":common"))
    compileOnly(project(":processor"))
    compileOnly(project(":downloader"))

    implementation(libs.hilt)
    annotationProcessor(libs.hilt.compiler)

    implementation(libs.bundles.rxjava)

    implementation(libs.gson)
    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}