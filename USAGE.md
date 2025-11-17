KoJSON Usage Guide

Installation
- Repositories: add `mavenCentral()`.
- Dependencies (choose an implementation):
  - Gradle Kotlin DSL
    ```kotlin
    dependencies {
      // JVM / Android backend (Gson)
      implementation("io.noties.kojson:kojson-jvm-gson:<version>")

      // Multiplatform backend (kotlinx.serialization JSON)
      implementation("io.noties.kojson:kojson-kmp-serialization:<version>")

      // Optional: core API only
      implementation("io.noties.kojson:kojson-api:<version>")
    }
    ```

Backends
- Gson (JVM/Android): `io.noties.kojson:kojson-jvm-gson` exposes `Json.Companion.parse(String)` and interop via extensions:
  - `val Json.gson: com.google.gson.JsonElement?`
  - `val JsonElement.gson: com.google.gson.JsonElement`
- kotlinx.serialization (Multiplatform): `io.noties.kojson:kojson-kmp-serialization` exposes the same `Json.Companion.parse(String)` and interop extensions:
  - `val Json.kson: kotlinx.serialization.json.JsonElement`
  - `val JsonElement.kson: kotlinx.serialization.json.JsonElement`

Quick Start (mirrors README snippet)
```kotlin
val json = Json.parse(
  """
  {
    "hello": "JayJay",
    "world": {
      "id": 42,
      "is_false": true,
      "coefficient": 0.334,
      "days": [1, 5, 7],
      "items": [1, "2", { "type": "yes" }]
    }
  }
  """.trimIndent()
)

// strict vs best‑effort accessors
val element = json["hello"]
val strict: String? = element.string      // null if not a JSON string
val bestEffort: String = element.stringValue // "" fallback

// navigate nested values
val coefficient: Float? = json["world"]["coefficient"].float
val id: Int = json["world"]["id"].int ?: error("Missing required:id")
val isFalse: Boolean = json["world"]["is_false"].booleanValue

// arrays
val days: List<Json> = json["world"]["days"].arrayValue
val daysValues: List<Int> = days.mapNotNull { it.int }

val items: List<Json> = json["items"].arrayValue
val itemsWithType: List<String> = items
  .mapNotNull { it["type"].takeIfExistsNotNull()?.string }
val itemsAsNumbers: List<Int> = items
  .map { it.intValue }
  .filter { it > 0 }

// work with raw backend elements if needed
// Gson: val raw: com.google.gson.JsonElement? = json.gson
// KMP:  val raw: kotlinx.serialization.json.JsonElement = json.kson
```

Programmatic JSON Creation
- Gson backend:
  ```kotlin
  val obj = JsonElement.new { JsonObject() }.also {
    it["name"] = "KoJSON"       // primitives
    it["stars"] = 5
    it["active"] = true
  }
  val json = Json(obj)
  ```
- kotlinx.serialization backend: same API via `JsonElement.new { ... }`.

Multiplatform Notes
- Common code can depend on `kojson-api` and/or `kojson-kmp-serialization`.
- If using Gson, add it to JVM/Android source sets only.
  ```kotlin
  kotlin {
    sourceSets {
      commonMain {
        dependencies { implementation("io.noties.kojson:kojson-api:<version>") }
      }
      jvmMain {
        dependencies { implementation("io.noties.kojson:kojson-jvm-gson:<version>") }
      }
      androidMain {
        dependencies { implementation("io.noties.kojson:kojson-jvm-gson:<version>") }
      }
    }
  }
  ```

Tips
- Prefer strict getters (`int`, `string`, …) for validation; use best‑effort getters (`intValue`, `stringValue`, …) for fallbacks.
- Use `exists()` and `existsNotNull()` to differentiate missing vs explicit JSON null.

