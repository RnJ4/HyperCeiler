//file:noinspection DependencyNotationArgumentimport com.android.build.gradle.internal.api.*
import java.io.*
import java.text.*
import java.time.*
import java.time.format.*
import java.util.*

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.lsparanoid)
    // alias(libs.plugins.lspluginResopt)
}


val apkId = "HyperCeiler"
val buildTypes = "release"
val roots = mapOf(
    "animation" to "libs/animation-${buildTypes}.aar",
    "appcompat" to "libs/appcompat-${buildTypes}.aar",
    "core" to "libs/core-${buildTypes}.aar",
    "haptic" to "libs/haptic-${buildTypes}.aar",
    "preference" to "libs/preference-${buildTypes}.aar",
    "smooth" to "libs/smooth-${buildTypes}.aar",
    "springback" to "libs/springback-${buildTypes}.aar",
    "external" to "libs/external-${buildTypes}.aar"
)

val getGitCommitCount: () -> Int = {
    val output = ByteArrayOutputStream()
    ProcessBuilder("git", "rev-list", "--count", "HEAD").start().apply {
        inputStream.copyTo(output)
        waitFor()
    }
    output.toString().trim().toInt()
}

val getVersionCode: () -> Int = {
    val commitCount = getGitCommitCount()
    val major = 5
    major + commitCount
}

fun getGitHash(): String {
    val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD").start()
    val output = process.inputStream.bufferedReader().use { it.readText().trim() }
    return output
}

fun getGitHashLong(): String {
    val process = ProcessBuilder("git", "rev-parse", "HEAD").start()
    val output = process.inputStream.bufferedReader().use { it.readText().trim() }
    return output
}

fun loadPropertiesFromFile(fileName: String): Properties? {
    val propertiesFile = rootProject.file(fileName)
    return if (propertiesFile.exists()) {
        val properties = Properties()
        properties.load(propertiesFile.inputStream())
        properties
    } else null
}

android {
    namespace = "com.sevtinge.hyperceiler"
    compileSdk = 34

    defaultConfig {
        applicationId = namespace
        minSdk = 33
        targetSdk = 34
        versionCode = 146
        versionName = "2.4.146"

        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += "arm64-v8a"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    androidResources {
        additionalParameters += "--allow-reserved-package-id"
        additionalParameters += "--package-id"
        additionalParameters += "0x36"
    }

    packaging {
        resources {
            excludes += "/META-INF/**"
            excludes += "/kotlin/**"
            excludes += "/*.txt"
            excludes += "/*.bin"
            excludes += "/*.json"
        }
        dex {
            useLegacyPackaging = true
        }
        applicationVariants.all {
    val variant = this
    variant.outputs
        .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
        .forEach { output ->
            val outputFileName = "hc-${variant.baseName}-${variant.versionName}-${variant.versionCode}.apk"
            output.outputFileName = outputFileName
        }
        }
    }

    val properties: Properties? = loadPropertiesFromFile("signing.properties")
    val getString: (String, String, String) -> String = { propertyName, environmentName, prompt ->
        properties?.getProperty(propertyName)
            ?: System.getenv(environmentName)
            ?: System.console()?.readLine("\n$prompt: ") ?: ""
    }
    val gitCode = getVersionCode()
    val gitHash = getGitHash()


    buildTypes {
        release {

            versionNameSuffix = "_${DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now())}"
            buildConfigField("String", "GIT_HASH", "\"$gitHash\"")
            buildConfigField("String", "GIT_CODE", "\"$gitCode\"")
            
        }
        create("beta") {

            versionNameSuffix = "_${DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now())}"
            buildConfigField("String", "GIT_HASH", "\"${getGitHashLong()}\"")
            buildConfigField("String", "GIT_CODE", "\"$gitCode\"")
            
        }
        create("canary") {

            versionNameSuffix = "_${gitHash}_r${gitCode}"
            buildConfigField("String", "GIT_HASH", "\"${getGitHashLong()}\"")
            buildConfigField("String", "GIT_CODE", "\"$gitCode\"")
            
        }
        debug {
            versionNameSuffix = "_${gitHash}_r${gitCode}"
            buildConfigField("String", "GIT_HASH", "\"${getGitHashLong()}\"")
            buildConfigField("String", "GIT_CODE", "\"$gitCode\"")
            
        }
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(22)
        }
    }

    kotlin {
        jvmToolchain(22)
    }
}

dependencies {
    compileOnly(project(":hidden-api"))
    compileOnly(libs.xposed.api)

    implementation(libs.dexkit)
    implementation(libs.ezxhelper)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.hiddenapibypass)
    implementation(libs.gson)
    implementation(libs.commons.codec)
    implementation(libs.hooktool)
    implementation(libs.gson)

    implementation(libs.core)
    implementation(libs.collection)
    implementation(libs.recyclerview)
    implementation(libs.fragment)
    implementation(libs.lifecycle.common)
    implementation(libs.vectordrawable)
    implementation(libs.vectordrawable.animated)
    implementation(libs.customview)
    implementation(libs.customview.poolingcontainer)
    implementation(libs.constraintlayout)

    implementation(files(roots["animation"]))
    implementation(files(roots["appcompat"]))
    implementation(files(roots["core"]))
    implementation(files(roots["haptic"]))
    implementation(files(roots["preference"]))
    implementation(files(roots["smooth"]))
    implementation(files(roots["springback"]))
    implementation(files(roots["external"]))

    implementation(files("libs/hyperceiler_expansion_packs-debug.aar"))
}
