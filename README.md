
# KoJSON

Pronounced **co-JAY-son** – the happy path from JSON to Kotlin.

KoJSON provides:

- A tiny, opinionated `Json` facade that gives you strict vs lenient accessors (`boolean` vs `booleanValue`, `int` vs `intValue`, etc).
- Swappable backends (`JsonImplementation`) so you can bring your favourite JSON engine.
- Ready-to-use implementations:
  - `kojson-jvm-gson` – wraps Google Gson for JVM projects.
  - `kojson-kmp-serialization` – wraps `kotlinx.serialization` JSON for multiplatform targets.
- A shared test suite (`kojson-test`) you can reuse to validate a custom backend.

---

## Installation

<!--
yes. snapshot is via metadata. https://github.com/badges/shields/issues/10894
how to specify it? by using `maven-metadata` and manually creating URL to
published snapshot `maven-metadata.xml` file. of cause.
-->
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fnoties%2Fkojson%2Fkojson-api%2Fmaven-metadata.xml)

```groovy
dependencies {
    // choose one (or both) implementations
    implementation("io.noties.kojson:kojson-jvm-gson:{{version}}")          // JVM / Android
    implementation("io.noties.kojson:kojson-kmp-serialization:{{version}}") // KMP

    // just the api, not required to add in order to use
    implementation("io.noties.kojson:kojson-api:{{version}}")
}
```

---

## Quick Start (JVM with Gson)

> `Json` is a happy version of original JSON. It is optimistic and flexible. Meanwhile, also strict if needed to.

JSON input:
```json
{
  "hello": "JayJay",
  "world": {
    "id": 42,
    "is_false": true,
    "coefficient": 0.334,
    "days": [
      1,
      5,
      7
    ],
    "items": [
      1,
      "2",
      {
        "type": "yes"
      }
    ]
  }
}
```

```kotlin
/*
 Each implementation has own `parse` extension function
 */
val json = Json.parse(jsonInput)

/*
 Obtain child element with the key "hello".
 */
val element: Json = json["hello"]

/*
 By calling then `.string` we strictly check that this element
 exists and is s 'json-string', returning null otherwise.
 */
//="JayJay"
val elementValue: String? = element.string

// let's check other string values (by accessing non-existing element):
print(
  //=null (`null` because type mismatch - string & json-object)
  json["otherString"].string,

  //="" (empty string - default fallback value for string)
  json["otherString"].stringValue,
)

/*
 Access nested elements with the subscript syntax
 */
// =0.334F
val coefficient: Float? = json["world"]["coefficient"].float

/*
 Access deeply nested property
 */
//=null
val someMissingProperty: Boolean? = json["yes"]["no"]["maybe"]["obviously_invalid"].boolean

/*
 Reference nested json-element
 */
val world: Json = json["world"]

world.also { world ->

  /*
   For a required value use _strict_ value getter and then fail on `null`
   */
  //=42
  val id: Int? = world["id"].int ?: error("Missing required:id")

  //=true
  val isFalse: Boolean = world["is_false"].booleanValue

  //=[1,5,7]
  val days: List<Json> = world["days"].arrayValue

  //=[1,5,7]
  val daysValues: List<Int> = days.mapNotNull { it.int }
}

/*
 Obtain backing json-array
 */
val items: List<Json> = json["items"].arrayValue

/*
 Filter the items to contain only jsons that has `type` specified
 */
//=['yes']
val itemsWithType: List<String> = items
  .mapNotNull { it["type"].takeIfExistsNotNull()?.string }

/*
 Best-effort filtering numbers
 */
//=[1, 2]
val itemsAsNumbers: List<Int> = items
  .map { it.intValue }
  // for example filter-out all defaults
  .filter { it > 0 }

/*
 Access raw json elements
 */
val itemsAsSampleItems: List<SampleItem> = items
  .mapNotNull { item ->
    val element = item.element
    if (element == null) {
      // json-element is undefined/missing
      null
    } else {
      // check type of the element
      when (element) {
        JsonNull -> null
        is JsonArray -> null
        is JsonObject -> item["type"].string
          ?.takeIf { it.isEmpty() }
          ?.let { SampleItem.SampleObject(type = it) }
        is JsonPrimitive -> {
          when {
            element.isBoolean -> SampleItem.SampleBoolean(value = item.booleanValue)
            element.isNumber -> SampleItem.SampleInt(value = item.intValue)
            element.isString -> SampleItem.SampleString(value = item.stringValue)
            element.isJsonNull -> null
            else -> null
          }
        }
      }
    }
  }
```


## See also

The project was inspired by the great [SwiftyJSON](https://github.com/SwiftyJSON/SwiftyJSON) library