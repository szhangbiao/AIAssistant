import com.android.build.api.variant.impl.VariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
}

val nonSystemLabel = "release"
val systemLabel = "system"
val channelPub = "pub"
val channelBjbs = "bjbs"
val channelVoice = "voice"
val isDevMode = false

android {
    namespace = "cn.booslink.llm"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "cn.booslink.llm"
        minSdk = 19
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // 使用 += 或 addAll 添加你需要的架构
            abiFilters += if (isDevMode) listOf("armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64") else listOf("armeabi-v7a", "arm64-v8a")
        }
        buildConfigField("Boolean", "DEBUG_MODE", isDevMode.toString())
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        // 正式环境非系统签名
        create(nonSystemLabel) {
            storeFile = project.file("${project.rootProject.rootDir}/keystore/release.keystore")
            storePassword = "123456"
            keyAlias = "booslink"
            keyPassword = "123456"
        }
        // 正式环境系统签名
        create(systemLabel) {
            storeFile = project.file("${project.rootProject.rootDir}/keystore/uid.keystore")
            storePassword = "123456"
            keyAlias = "booslink"
            keyPassword = "123456"
        }
    }

    buildTypes {
        getByName(nonSystemLabel) {
            if (isDevMode) {
                isDebuggable = true
                isMinifyEnabled = false
            } else {
                isMinifyEnabled = true
                isShrinkResources = true
            }
            signingConfig = signingConfigs.getByName(nonSystemLabel)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        create(systemLabel) {
            if (isDevMode) {
                isDebuggable = true
                isMinifyEnabled = false
            } else {
                isMinifyEnabled = true
                isShrinkResources = true
            }
            signingConfig = signingConfigs.getByName(systemLabel)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create(channelPub) {
            dimension = "version"
            manifestPlaceholders["channelValue"] = channelPub
        }
        create(channelBjbs) {
            dimension = "version"
            manifestPlaceholders["channelValue"] = channelBjbs
        }
        create(channelVoice) {
            dimension = "version"
            applicationId = "com.booslink.aivoiceremote"
            manifestPlaceholders["channelValue"] = channelVoice
        }
    }

    androidComponents.beforeVariants { variantBuilder ->
        variantBuilder.enable = when (variantBuilder.buildType) {
            nonSystemLabel, systemLabel -> true
            else -> false
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

    androidComponents.onVariants { variant ->
        variant.outputs.forEach { output ->
            val projectName = "VoiceHelper"
            val flavor = (variant.flavorName ?: "").uppercase()
            val versionName = output.versionName.get()
            val date = SimpleDateFormat("yyyyMMdd").format(Date())
            val appEnv = if (isDevMode) "DEV" else "PROD"
            val suffix = if (flavor == "PUB") "pub" else flavor.lowercase()
            var fileName = "${projectName}_${suffix.uppercase()}_${appEnv}_${date}_V${versionName}.apk"
            (output as? VariantOutputImpl)?.outputFileName?.set(fileName)
        }
    }
}

// 强制所有依赖使用 OkHttp 3.12.12 版本
configurations.all {
    resolutionStrategy {
        force("com.squareup.okhttp3:okhttp:3.12.12")
        force("com.squareup.okhttp3:logging-interceptor:3.12.12")
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
    implementation(libs.media)
    annotationProcessor(libs.lifecycle.compiler)

    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.rxjava)
    implementation(libs.bundles.downloader)

    implementation(libs.bundles.glide)
    annotationProcessor(libs.glide.compiler)

    implementation(libs.timber)
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    annotationProcessor(libs.hilt.work.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}