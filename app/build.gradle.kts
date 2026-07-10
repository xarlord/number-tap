import java.util.Properties

// Read signing config from local.properties
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.xarlord.numbertap"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.xarlord.numbertap"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "1.4.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = localProps.getProperty("keystore.file")?.let { file(it) }
            storePassword = localProps.getProperty("keystore.password", "")
            keyAlias = localProps.getProperty("key.alias", "upload")
            keyPassword = localProps.getProperty("key.password", "")
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Core library desugaring for java.time on API < 26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // AndroidX
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.work.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose) // #145: needed for LocalLifecycle

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.work.testing)
    testImplementation(libs.mockk)
    testImplementation("org.json:json:20231013")
    implementation(libs.play.services.ads)
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    // Force fragment version to satisfy Play Console deprecation warning
    // (pulled transitively by work-runtime-ktx, play-services-ads)
    implementation("androidx.fragment:fragment:1.8.6")
}

kover {
    reports {
        filters {
            excludes {
                // Composable UI — requires instrumented tests, not unit-testable
                classes("com.xarlord.numbertap.ui.*")
                classes("com.xarlord.numbertap.ads.BannerAd")
                classes("com.xarlord.numbertap.MainActivity")
            }
        }
        verify {
            rule {
                minBound(60)
            }
        }
    }
}
