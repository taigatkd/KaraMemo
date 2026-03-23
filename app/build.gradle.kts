import java.util.Properties
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun Project.stringSetting(name: String): String? =
    (findProperty(name) as String?)?.takeIf { it.isNotBlank() }
        ?: System.getenv(name)?.takeIf { it.isNotBlank() }

fun Project.booleanSetting(name: String, default: Boolean): Boolean =
    stringSetting(name)?.toBooleanStrictOrNull() ?: default

fun Properties.requiredValue(name: String): String =
    getProperty(name)?.takeIf { it.isNotBlank() }
        ?: error("Missing `$name` in keystore.properties.")

val releaseAdsEnabled = project.booleanSetting("KARAMEMO_RELEASE_ADS_ENABLED", default = true)
val releaseAdMobAppId = project.stringSetting("KARAMEMO_ADMOB_APP_ID").orEmpty()
val releaseBannerAdUnitId = project.stringSetting("KARAMEMO_BANNER_AD_UNIT_ID").orEmpty()
val releaseInterstitialAdUnitId = project.stringSetting("KARAMEMO_INTERSTITIAL_AD_UNIT_ID").orEmpty()

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use(::load)
    }
}

val isReleaseTaskRequested = gradle.startParameter.taskNames.any { taskName ->
    taskName.contains("release", ignoreCase = true)
}

if (isReleaseTaskRequested && releaseAdsEnabled) {
    check(releaseAdMobAppId.isNotBlank()) {
        "KARAMEMO_ADMOB_APP_ID is required for ads-enabled release builds."
    }
    check(releaseBannerAdUnitId.isNotBlank()) {
        "KARAMEMO_BANNER_AD_UNIT_ID is required for ads-enabled release builds."
    }
    check(releaseInterstitialAdUnitId.isNotBlank()) {
        "KARAMEMO_INTERSTITIAL_AD_UNIT_ID is required for ads-enabled release builds."
    }
}

if (isReleaseTaskRequested) {
    check(keystorePropertiesFile.exists()) {
        "keystore.properties is required for release builds. Copy keystore.properties.example and fill in local values."
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.taigatkd.karamemo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.taigatkd.karamemo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.requiredValue("storeFile"))
                storePassword = keystoreProperties.requiredValue("storePassword")
                keyAlias = keystoreProperties.requiredValue("keyAlias")
                keyPassword = keystoreProperties.requiredValue("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "ADS_ENABLED", "true")
            buildConfigField("boolean", "MOCK_BILLING_ENABLED", "true")
            buildConfigField("String", "PRO_PRODUCT_ID", "\"karamemo_pro\"")
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "ADS_ENABLED", releaseAdsEnabled.toString())
            buildConfigField("boolean", "MOCK_BILLING_ENABLED", "false")
            buildConfigField("String", "PRO_PRODUCT_ID", "\"karamemo_pro\"")
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"$releaseBannerAdUnitId\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"$releaseInterstitialAdUnitId\"")
            resValue("string", "admob_app_id", releaseAdMobAppId)
            signingConfigs.findByName("release")?.let { signingConfig = it }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.02.01")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.activity:activity-compose:1.12.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.room:room-runtime:2.8.1")
    implementation("androidx.room:room-ktx:2.8.1")
    ksp("androidx.room:room-compiler:2.8.1")

    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("com.google.android.gms:play-services-ads:24.9.0")
    implementation("com.android.billingclient:billing:8.3.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
