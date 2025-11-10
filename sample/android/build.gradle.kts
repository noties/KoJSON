// awesome gradle :'P
// https://github.com/gradle/gradle/issues/20084
// https://github.com/gradle/gradle/issues/18236
// https://github.com/gradle/gradle/issues/17968
plugins {
    id(libs.plugins.androidApplication.get().pluginId)
    id(libs.plugins.kotlinAndroid.get().pluginId)
}

android {
    namespace = "io.noties.kojson.sample.android"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets["main"].java.srcDirs(projectDir.resolve("../shared/kotlin"))

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":kojson-kmp-serialization"))
    implementation(libs.kotlinx.serialization)
}
