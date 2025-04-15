import com.android.build.gradle.internal.tasks.FinalizeBundleTask
import kotlin.jvm.java

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
//    id("com.google.firebase.crashlytics")

    kotlin("plugin.serialization") version "2.1.0"
}

android {
    namespace = "io.alexarix.pushreader"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.alexarix.pushreader"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        setProperty("archivesBaseName", "pushreader_$versionName($versionCode)")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    applicationVariants.all {
        outputs.all {
            // AAB file name that You want. Flavor name also can be accessed here.
            val aabPackageName = "crypto_money_ac_$versionName($versionCode).aab"
            // Get final bundle task name for this variant
            val bundleFinalizeTaskName = StringBuilder("sign").run {
                // Add each flavor dimension for this variant here
                productFlavors.forEach {
                    append(it.name.replaceFirstChar { it.uppercase() })
                }
                // Add build type of this variant
                append(buildType.name.replaceFirstChar { it.uppercase() })
                append("Bundle")
                toString()
            }
            tasks.named(bundleFinalizeTaskName, FinalizeBundleTask::class.java) {
                val file = finalBundleFile.asFile.get()
                val finalFile = File(file.parentFile, aabPackageName)
                finalBundleFile.set(finalFile)
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //AppCompatResources
    implementation(libs.androidx.appcompat)

    // Gson
    implementation(libs.gson)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
}