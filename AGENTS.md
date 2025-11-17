KoJSON • Agent Guide

Purpose
- KoJSON is a Kotlin Multiplatform JSON facade with pluggable backends.
- Primary modules you can depend on as libraries:
  - kojson-api — shared API interfaces and helpers.
  - kojson-jvm-gson — JVM/Android implementation backed by Gson.
  - kojson-kmp-serialization — KMP implementation backed by kotlinx.serialization JSON.

Toolchain
- Gradle wrapper: use `./gradlew` from repo root.
- Java: JDK 17+ (tooling), code targets Java 8 bytecode.
- Kotlin: 2.1.20; AGP: 8.5.2. Versions are defined in `gradle/libs.versions.toml`.

Build & Test
- Build everything: `./gradlew build`
- Run all tests: `./gradlew check`
- Module-specific build:
  - API: `./gradlew :kojson-api:build`
  - JVM Gson: `./gradlew :kojson-jvm-gson:build`
  - KMP Serialization: `./gradlew :kojson-kmp-serialization:build`

Publish Locally (for consumers and examples)
- To local Maven cache: `./gradlew :kojson-api:publishToMavenLocal :kojson-jvm-gson:publishToMavenLocal :kojson-kmp-serialization:publishToMavenLocal`
- To a directory (file-based repo), set `MAVEN_LOCAL_DIR` in `gradle.properties` or env and use `publishAllPublicationsToMavenRepository` tasks per module.

Artifacts (Maven coordinates)
- Group: `io.noties.kojson`
- Artifacts:
  - `io.noties.kojson:kojson-api:<version>`
  - `io.noties.kojson:kojson-jvm-gson:<version>`
  - `io.noties.kojson:kojson-kmp-serialization:<version>`

Quick Usage
- Gradle Kotlin DSL:
  ```kotlin
  dependencies {
    implementation("io.noties.kojson:kojson-jvm-gson:<version>") // JVM/Android
    implementation("io.noties.kojson:kojson-kmp-serialization:<version>") // KMP
    // optional API-only
    implementation("io.noties.kojson:kojson-api:<version>")
  }
  ```
- Example (Gson):
  ```kotlin
  val json = Json.parse("{""hello"":""world""}")
  val value = json["hello"].stringValue
  ```

Repository Structure
- `kojson-api` — KMP sources; Android and JVM share sources in `src/commonJvmAndroid/kotlin`.
- `kojson-jvm-gson` — JVM/Android module depending on `kojson-api` and `com.google.code.gson:gson`.
- `kojson-kmp-serialization` — KMP module depending on `kojson-api` and `org.jetbrains.kotlinx:kotlinx-serialization-json`.
- `sample` — example apps (`android`, `jvm`).

Common Tasks (reference)
- List project tasks: `./gradlew tasks`
- Generate API docs (if configured): `./gradlew dokkaHtml` or `dokkaHtmlMultiModule`.

Notes for Automated Agents
- Prefer module-scoped tasks (e.g., `:kojson-api:build`) to speed up iterations.
- Respect the declared Kotlin/AGP versions to avoid ABI mismatches.
- The repo is designed for open consumption; no network restrictions are imposed by default.

