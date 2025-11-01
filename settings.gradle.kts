enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

// added underscore at the end, because name as-is causes some internal gradle exception
//  saving that such method is already defined. Changing to anything except original name fixes the issue
rootProject.name = "KoJSON_"

include(":kojson-api")

include(":kojson-jvm-gson")
include(":kojson-kmp-serialization")

include(":sample:android", ":sample:jvm")
// :'( not working
//include(":kojson-test")
