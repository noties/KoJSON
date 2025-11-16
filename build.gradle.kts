import java.net.URI
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension

plugins {
    // trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.jetbrains.kotlin.jvm).apply(false)
}

data class ArtifactMetadata(
    val displayName: String,
    val description: String,
)

fun Project.propertyOrEnv(key: String): String? =
    (findProperty(key) as? String)?.takeIf { it.isNotBlank() }
        ?: System.getenv(key)?.takeIf { it.isNotBlank() }

group = property("GROUP") as String
version = property("VERSION_NAME") as String

allprojects {
    group = rootProject.group
    version = rootProject.version
}

val artifactMetadata = mapOf(
    "kojson-api" to ArtifactMetadata(
        displayName = "KoJSON API",
        description = "Core multiplatform interfaces for JSON accessors and implementations.",
    ),
    "kojson-jvm-gson" to ArtifactMetadata(
        displayName = "KoJSON JVM Gson",
        description = "Gson-backed KoJSON implementation for JVM and Android targets.",
    ),
    "kojson-kmp-serialization" to ArtifactMetadata(
        displayName = "KoJSON Kotlin Serialization",
        description = "Kotlinx.serialization-powered KoJSON implementation for multiplatform projects.",
    ),
)

val pomName = property("POM_NAME") as String
val pomDescription = property("POM_DESCRIPTION") as String
val pomUrl = property("POM_URL") as String
val pomInceptionYear = property("POM_INCEPTION_YEAR") as String
val pomScmUrl = property("POM_SCM_URL") as String
val pomScmConnection = property("POM_SCM_CONNECTION") as String
val pomScmDevConnection = property("POM_SCM_DEV_CONNECTION") as String
val pomLicenseName = property("POM_LICENSE_NAME") as String
val pomLicenseUrl = property("POM_LICENSE_URL") as String
val pomLicenseDist = property("POM_LICENSE_DIST") as String
val pomDeveloperId = property("POM_DEVELOPER_ID") as String
val pomDeveloperName = property("POM_DEVELOPER_NAME") as String
val pomDeveloperEmail = property("POM_DEVELOPER_EMAIL") as String

description = pomDescription

val sonatypeUsername = propertyOrEnv("SONATYPE_USERNAME")
val sonatypePassword = propertyOrEnv("SONATYPE_PASSWORD")
val signingKeyId = propertyOrEnv("SIGNING_KEY_ID")
val signingKey = propertyOrEnv("SIGNING_KEY")
val signingPassword = propertyOrEnv("SIGNING_PASSWORD")

val releaseRepositoryUrl = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
val snapshotRepositoryUrl = URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")

subprojects {
    val metadata = artifactMetadata[name] ?: return@subprojects
    description = metadata.description

    pluginManager.apply("maven-publish")
    pluginManager.apply("signing")

    extensions.configure<PublishingExtension> {
        publications.withType<MavenPublication>().configureEach {
            pom {
                name.set(metadata.displayName.ifBlank { pomName })
                description.set(metadata.description.ifBlank { pomDescription })
                url.set(pomUrl)
                inceptionYear.set(pomInceptionYear)

                licenses {
                    license {
                        name.set(pomLicenseName)
                        url.set(pomLicenseUrl)
                        distribution.set(pomLicenseDist)
                    }
                }
                developers {
                    developer {
                        id.set(pomDeveloperId)
                        name.set(pomDeveloperName)
                        email.set(pomDeveloperEmail)
                    }
                }
                scm {
                    url.set(pomScmUrl)
                    connection.set(pomScmConnection)
                    developerConnection.set(pomScmDevConnection)
                }
            }
        }

        repositories {
//            maven {
//                url = (property("MAVEN_LOCAL_DIR") as String)
//                    .let { "file://$it" }
//                    .let { URI.create(it) }
//            }
            maven {
                name = "Sonatype"
                url = if (version.toString().endsWith("SNAPSHOT")) {
                    snapshotRepositoryUrl
                } else {
                    releaseRepositoryUrl
                }

                if (sonatypeUsername != null && sonatypePassword != null) {
                    credentials {
                        username = sonatypeUsername
                        password = sonatypePassword
                    }
                }
            }
        }
    }

    extensions.configure<SigningExtension> {
        val publishing = extensions.getByType<PublishingExtension>()
        isRequired = !version.toString().endsWith("SNAPSHOT")

        if (!signingKey.isNullOrBlank()) {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(publishing.publications)
        }
    }
}

tasks.register("publishKojsonToSonatype") {
    dependsOn(
        ":kojson-api:publishAllPublicationsToMavenRepository",
        ":kojson-kmp-serialization:publishAllPublicationsToMavenRepository",
        ":kojson-jvm-gson:publishReleasePublicationToMavenRepository",
    )
}
