import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun prop(name: String, fallback: String = "") = localProps.getProperty(name, fallback)

android {
    namespace = "com.yourname.tempmail"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yourname.tempmail"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            // Official Unity test / demo credentials (safe, only when running debug builds).
            buildConfigField("String", "UNITY_APP_KEY", "\"${prop("unity.appKey", "25b63cf85")}\"")
            buildConfigField("String", "UNITY_BANNER_AD_UNIT", "\"${prop("unity.banner.adUnitId", "demoBanner")}\"")
            buildConfigField("String", "UNITY_INTERSTITIAL_AD_UNIT", "\"${prop("unity.interstitial.adUnitId", "demoInterstitial")}\"")
            buildConfigField("String", "UNITY_REWARDED_AD_UNIT", "\"${prop("unity.rewarded.adUnitId", "demoRewarded")}\"")
            buildConfigField("Boolean", "IS_TEST_MODE", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "UNITY_APP_KEY", "\"${prop("unity.appKey", "")}\"")
            buildConfigField("String", "UNITY_BANNER_AD_UNIT", "\"${prop("unity.banner.adUnitId", "")}\"")
            buildConfigField("String", "UNITY_INTERSTITIAL_AD_UNIT", "\"${prop("unity.interstitial.adUnitId", "")}\"")
            buildConfigField("String", "UNITY_REWARDED_AD_UNIT", "\"${prop("unity.rewarded.adUnitId", "")}\"")
            buildConfigField("Boolean", "IS_TEST_MODE", "false")
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
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)
    androidTestImplementation(libs.compose.ui.test.manifest)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.work.runtime)
    implementation(libs.datastore)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)

    implementation(libs.coil.compose)
    implementation(libs.jsoup)
    implementation(libs.coroutines.android)

    implementation(libs.levelplay)
    implementation(libs.gms.ads.identifier)
    implementation(libs.gms.appset)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.room.testing)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.room.testing)
}