import java.net.URI
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(java.io.FileInputStream(localPropertiesFile))
}
val mavenUsername = localProperties.getProperty("mavenUsername") ?: ""
val mavenPassword = localProperties.getProperty("mavenPassword") ?: ""

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("iflytek/libs")
            dirs("downloader/libs")
        }
        maven { url = uri("https://jitpack.io") }
        // google() 国内阿里云替代
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        // mavenCentral() 国内阿里云替代
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        // okdownload Snapshots version
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        //-------------- 阿里云配置 -----------------
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven {
            credentials {
                username = mavenUsername
                password = mavenPassword
            }
            url =
                URI.create("https://packages.aliyun.com/maven/repository/2087120-snapshot-n8shdu/")
        }
        maven {
            credentials {
                username = mavenUsername
                password = mavenPassword
            }
            url = URI.create("https://packages.aliyun.com/maven/repository/2087120-release-unRi7v/")
        }
        //-----------------------------------------
    }
}

rootProject.name = "AI Assistant"
include(":app")
include(":speech")
include(":processor")
include(":downloader")
include(":common")
