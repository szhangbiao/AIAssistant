plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
}

android {
    namespace = "cn.booslink.llm.common"
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
    implementation(libs.hilt)
    annotationProcessor(libs.hilt.compiler)

    compileOnly(libs.iflytek.sdk)

    implementation(libs.bundles.android.widget)

    implementation(libs.bundles.jetpack)
    annotationProcessor(libs.lifecycle.compiler)

    implementation(libs.bundles.glide)
    annotationProcessor(libs.glide.compiler)

    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.rxjava)

    implementation(libs.timber)
    implementation(libs.utils.view)
    implementation(libs.libpag)
    implementation(libs.blur.view)
    implementation(libs.datetime)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}