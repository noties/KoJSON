import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    explicitApi = ExplicitApiMode.Strict

    sourceSets {
        test {
            kotlin.srcDirs(file("${rootProject.rootDir}/kojson-test/src/kotlin/"))
        }
    }

    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
    }
}

dependencies {
    api(project(":kojson-api"))
    api(libs.gson)

    testImplementation(libs.kotlin.test)
}