plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

sourceSets {
    main {
        java.srcDirs(projectDir.resolve("../shared/kotlin/"))
    }
}

dependencies {
    // validate snapshot publishing to maven-central
    implementation("io.noties.kojson:kojson-jvm-gson:1.0.0-SNAPSHOT")
    implementation("io.noties.kojson:kojson-kmp-serialization:1.0.0-SNAPSHOT")

//    implementation(project(":kojson-jvm-gson"))
//    implementation(project(":kojson-kmp-serialization"))
}
