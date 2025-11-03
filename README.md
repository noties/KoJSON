
# KoJSON

Pronounced **co-JAY-son** – your happy path to JSON from Kotlin on every platform.

KoJSON provides:

- A tiny, opinionated `Json` facade that gives you strict & lenient accessors (`boolean` vs `booleanValue`, etc).
- Swappable backends (`JsonImplementation`) so you can bring your favourite JSON engine.
- Ready-to-use implementations:
  - `kojson-jvm-gson` – wraps Google Gson for JVM projects.
  - `kojson-kmp-serialization` – wraps `kotlinx.serialization` JSON for multiplatform targets.
- A shared test suite (`kojson-test`) you can reuse to validate a custom backend.

---

## Modules

| Module | Description |
| ------ | ----------- |
| `kojson-api` | Core multiplatform API (`Json`, `JsonElement`, `JsonFactory`, …). |
| `kojson-jvm-gson` | JVM implementation backed by [Gson](https://github.com/google/gson). |
| `kojson-kmp-serialization` | Multiplatform implementation backed by [`kotlinx.serialization-json`](https://github.com/Kotlin/kotlinx.serialization). |
| `kojson-test` | Test harness with exhaustive behavioural checks for any implementation. |

---

## Installation

KoJSON artifacts are published to Maven Central under the `io.noties.kojson` group. Replace `{{version}}` with the latest release.

### Kotlin Gradle DSL

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.noties.kojson:kojson-api:{{version}}")

    // choose one (or both) implementations
    implementation("io.noties.kojson:kojson-jvm-gson:{{version}}")          // JVM / Android
    implementation("io.noties.kojson:kojson-kmp-serialization:{{version}}") // KMP
}
```

---

## Quick Start (JVM with Gson)

```kotlin
import com.google.gson.JsonParser
import io.noties.kojson.api.Json
import io.noties.kojson.jvm.gson.JsonImplementationGson
import io.noties.kojson.jvm.gson.json // extension on Gson JsonElement

fun main() {
    // 1) Plug in your preferred implementation once (app start, DI, etc.)
    Json.implementation = JsonImplementationGson

    // 2) Parse with your engine, then wrap it with KoJSON
    val payload = """
        {
          "id": 42,
          "name": "Cordelia",
          "flags": {
            "active": true,
            "roles": ["admin", "editor"]
          }
        }
    """.trimIndent()

    val gsonElement = JsonParser.parseString(payload)
    val json = gsonElement.json

    // 3) Navigate safely
    println(json["id"].intValue)                  // 42
    println(json["flags"]["active"].booleanValue) // true
    println(json["flags"]["roles"].arrayValue.map { it.stringValue })
    // -> [admin, editor]
}
```

### Creating JSON programmatically

`Json.new { … }` uses the active implementation to build elements in a type-safe manner:

```kotlin
val createUserPayload = Json.new {
    JsonObject().also { root ->
        root["name"] = "Yor Forger"
        root["age"] = 28
        root["skills"] = JsonArray().apply {
            add(JsonPrimitive("forging"))
            add(JsonPrimitive("parenting"))
        }
    }
}

println(createUserPayload.stringValue) // JsonObject -> stringValue == "{}" fallback
println(createUserPayload["skills"].arrayValue.map { it.stringValue })
```

---

## Multiplatform Usage (kotlinx.serialization)

```kotlin
import io.noties.kojson.api.Json
import io.noties.kojson.kmp.serialization.JsonImplementationKotlinxSerialization
import io.noties.kojson.kmp.serialization.json
import kotlinx.serialization.json.Json as KxJson

fun bootstrapKoJson() {
    Json.implementation = JsonImplementationKotlinxSerialization
}

fun parse(data: String): String {
    val element = KxJson.parseToJsonElement(data)
    val json = element.json
    return buildString {
        append(json["title"].stringValue)
        append(" (")
        append(json["year"].intValue)
        append(")")
    }
}
```

Because `Json` is multiplatform, you can keep business logic in `commonMain` while each target supplies its preferred implementation during startup.

---

## Testing a Custom Implementation

Want to integrate a different engine? Implement `JsonImplementation<T>` and reuse the suite in `kojson-test` to validate behaviour:

```kotlin
class MyJsonTestSuite : KoJSONTestSuite<MyJsonElement>() {
    override fun createFactory(): JsonNativeFactory<MyJsonElement> = MyFactory
    override fun createImplementation(): JsonImplementation<MyJsonElement> = MyJsonImplementation
}
```

Run the inherited tests to ensure your backend matches KoJSON expectations.

---

## Samples & Development

- `sample/android` and `sample/jvm` contain minimal apps showing integration on each platform.
- `kojson-test` holds an extensive catalogue of behavioural tests that double as documentation for edge cases.

---

## License

KoJSON is distributed under the [MIT License](https://opensource.org/licenses/MIT).
