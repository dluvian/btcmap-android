import java.io.FileInputStream
import java.net.URL
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization") version "1.6.21"
    id("androidx.navigation.safeargs.kotlin")
    id("com.squareup.sqldelight")
    id("com.google.devtools.ksp") version "1.6.21-1.0.5"
}

val signingPropertiesFile = rootProject.file("signing.properties")

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "org.btcmap"
        minSdk = 26
        targetSdk = 31
        versionCode = 2
        versionName = "0.2.0"
        setProperty("archivesBaseName", "btcmap-$versionName")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    signingConfigs {
        if (signingPropertiesFile.exists()) {
            create("release") {
                val signingProperties = Properties()
                signingProperties.load(FileInputStream(signingPropertiesFile))
                storeFile = File(signingProperties["keystore_path"] as String)
                storePassword = signingProperties["keystore_password"] as String
                keyAlias = signingProperties["keystore_key_alias"] as String
                keyPassword = signingProperties["keystore_key_password"] as String
            }
        }

        create("selfSignedRelease") {
            storeFile = File(rootDir, "release.jks")
            storePassword = "btcmap"
            keyAlias = "btcmap"
            keyPassword = "btcmap"
        }
    }

    packagingOptions {
        resources.excludes += "DebugProbesKt.bin"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        if (signingPropertiesFile.exists()) {
            release {
                val signingProperties = Properties()
                signingProperties.load(FileInputStream(signingPropertiesFile))
                signingConfig = signingConfigs.getByName("release")
            }
        }

        getByName("release") {
            // Enables code shrinking, obfuscation, and optimization
            isMinifyEnabled = true

            // Enables resource shrinking
            isShrinkResources = true

            // Includes the default ProGuard rules file
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        create("selfSignedRelease") {
            signingConfig = signingConfigs.getByName("selfSignedRelease")

            // Enables code shrinking, obfuscation, and optimization
            isMinifyEnabled = true

            // Enables resource shrinking
            isShrinkResources = true

            // Includes the default ProGuard rules file
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

sqldelight {
    database("Database") {
        sourceFolders = listOf("sqldelight")
        packageName = "db"
        deriveSchemaFromMigrations = true
    }
}

tasks.register("bundleData") {
    doLast {
        val src = URL("https://raw.githubusercontent.com/bubelov/btcmap-data/main/data.json")
        val destDir = File(projectDir, "src/main/assets")
        destDir.mkdirs()
        val destFile = File(destDir, "data.json")
        destFile.writeText(src.readText())
    }
}

dependencies {
    // Kotlin extensions
    // Simplifies non-blocking programming
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")
    // Platform-agnostic JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    // Android extensions
    implementation("androidx.core:core-ktx:1.7.0")
    val navVer = "2.4.2"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVer")
    implementation("androidx.navigation:navigation-ui-ktx:$navVer")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference:1.2.0")
    implementation("com.google.android.material:material:1.6.0")

    // Modern HTTP client
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // SQLDelight generates typesafe kotlin APIs from SQL statements
    val sqlDelightVer = "1.5.3"
    implementation("com.squareup.sqldelight:coroutines-extensions:$sqlDelightVer")
    implementation("com.squareup.sqldelight:android-driver:$sqlDelightVer")
    testImplementation("com.squareup.sqldelight:sqlite-driver:$sqlDelightVer")

    // Injection library
    implementation("io.insert-koin:koin-android:3.2.0")
    val koinAnnotationsVer = "1.0.0-beta-2"
    implementation("io.insert-koin:koin-annotations:$koinAnnotationsVer")
    ksp("io.insert-koin:koin-ksp-compiler:$koinAnnotationsVer")

    // Open Street Map widget
    implementation("org.osmdroid:osmdroid-android:6.1.13")
    implementation("com.github.MKergall:osmbonuspack:6.7.0")

    // Common test dependencies
    testImplementation(kotlin("test"))
}