plugins {
    // trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.jetbrains.kotlin.jvm).apply(false)
    alias(libs.plugins.vanniktechMavenPublish).apply(false)
}

group = property("GROUP") as String
version = property("VERSION_NAME") as String

allprojects {
    group = rootProject.group
    version = rootProject.version
}
