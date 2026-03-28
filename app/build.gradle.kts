plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
}

android {
    namespace = "cn.booslink.llm"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "cn.booslink.llm"
        minSdk = 19
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    sourceSets {
        named("main") {
            assets.directories.add("../iflytek/assets")
            jniLibs.directories.add("../iflytek/jniLibs")
        }
    }
}

dependencies {
    implementation(libs.multidex)
    implementation(libs.boost.multidex)

    implementation(libs.iflytek.sdk)

    implementation(project(":speech"))
    implementation(project(":processor"))
    implementation(project(":downloader"))
    implementation(project(":common"))

    implementation(libs.hilt)
    annotationProcessor(libs.hilt.compiler)

    implementation(libs.bundles.android.widget)

    implementation(libs.bundles.jetpack)
    annotationProcessor(libs.lifecycle.compiler)

    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.rxjava)
    implementation(libs.bundles.downloader)

    implementation(libs.bundles.glide)
    annotationProcessor(libs.glide.compiler)

    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}