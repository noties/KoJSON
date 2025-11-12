package io.noties.kojson.sample.shared.snippets

import io.noties.kojson.api.Json
import io.noties.kojson.api.JsonArray
import io.noties.kojson.api.JsonElement
import io.noties.kojson.api.JsonFactory
import io.noties.kojson.api.JsonNull
import io.noties.kojson.api.JsonObject
import io.noties.kojson.api.JsonPrimitive
import io.noties.kojson.api.takeIfExistsNotNull
import io.noties.kojson.sample.shared.JsonSnippet

// simple abstraction that allows changing json response from backend
// update without releasing a new version
// adjust json on the server to fit the needs of the app (targeting is not included in this library)

/**
 * `Json` is a happy version of original JSON. It is optimistic and flexible.
 * Meanwhile, also strict if needed to.
 *
 * JSON input:
 * ```json
 * {
 *   "hello": "JayJay",
 *   "world": {
 *     "id": 42,
 *     "is_false": true,
 *     "coefficient": 0.334,
 *     "days": [ 1, 5, 7 ],
 *     "items": [
 *       1,
 *       "2",
 *       { "type": "yes" }
 *     ]
 *   }
 * }
 * ```
 */
fun walkthrough(json: Json) {
    /*
    Obtain Json element by the key "hello".
     */
    val element: Json = json["hello"]

    /*
    By calling then `.string` we strictly check that this element
    exists and is s 'json-string', returning null otherwise.
     */
    // "JayJay"
    val elementValue: String? = element.string

    // let's check other string values:
    print(
        // "world"
        json.stringValue,

        // null (`null` because type mismatch - string & json-object)
        json["otherString"].string,

        // "" (empty string - default fallback value for string)
        json["otherString"].stringValue,
    )

    /*
    Access other element directly by appending the path with the subscript syntax
     */
    // "0.334F"
    val coefficient: Float? = json["world"]["coefficient"].float

    /*
    Reference nested json-element
     */
    val world: Json = json["world"]

    world.also { world ->

        /*
        For a required value use _strict_ value getter and then fail on `null`
         */
        // "42"
        val id: Int? = world["id"].int ?: error("Missing required:id")

        // "true"
        val isFalse: Boolean = world["is_false"].booleanValue

        // [1,5,7]
        val days: List<Json> = world["days"].arrayValue

        // List[1,5,7]
        val daysValues: List<Int> = days.mapNotNull { it.int }
    }

    /*
    Obtain backing json-array
     */
    val items: List<Json> = json["items"].arrayValue

    /*
    Filter the items to contain only jsons that has `type` specified
     */
    // "List['yes']"
    val itemsWithType: List<String> = items
        .mapNotNull { it["type"].takeIfExistsNotNull()?.string }

    /*
     Best-effort filtering numbers
     */
    // [1, 2]
    val itemsAsNumbers = items
        .map { it.intValue }
        // for example filter-out all defaults
        .filter { it > 0 }

    /*
    Access raw json
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
}

sealed class SampleItem {
    data class SampleBoolean(val value: Boolean) : SampleItem()
    data class SampleInt(val value: Int) : SampleItem()
    data class SampleString(val value: String) : SampleItem()
    data class SampleObject(val type: String) : SampleItem()
}

//inline fun <T: Any> Json.required(block: Json.() -> T?): T {
//    val value = block()
//    if (value == null) {
//        // actually we do not have any info here, no key, no path
//        // it is pretty useless
//    }
//}

fun <T : Any?> print(
    vararg values: T
) {
    println(values)
}

@JsonSnippet
class Basic {
    fun `json-object`(json: Json) {
        // element at the path
        // it is never null, even if accessed json-element is absent, no error is thrown
        val json: Json = json["nested"]["yeah"]["this"]["deep"]

        // null if actual element does not exist
        val element = json.element
    }

    fun `value-boolean`(json: Json) {
        // nullable , `null` if json-element does not exist or json-element is not of type boolean
        val strict: Boolean? = json.boolean

        // never null. before returning default "false" does some fallback checks:
        //  - for a `1` integer = to be true
        //  - for a `"true"` string = to be `true` case-insensitive
        val relaxedOrDefault = json.booleanValue
    }

    fun `value-int`(json: Json) {
        // nullable, `null` is json-element does not exist or is not of type Int
        val strict: Int? = json.int

        // relaxed, never null, falls back to the default of: `0`
        //  if json-element is absent or is not of type __Number__, otherwise
        //  the _best effort attempt_ is done to convert it (no checks for overflows or precision)
        val relaxedOrDefault: Int = json.intValue
    }

    fun `value-long`(json: Json) {
        // nullable, `null` is json-element does not exist or is not of type Long
        val strict: Long? = json.long

        // relaxed, never null, falls back to the default of: `0L`
        //  if json-element is absent or is not of type __Number__, otherwise
        //  the _best effort attempt_ is done to convert it (no checks for overflows or precision)
        val relaxedOrDefault: Long = json.longValue
    }

    fun `value-float`(json: Json) {
        // nullable, `null` is json-element does not exist or is not of type Float
        val strict: Float? = json.float

        // relaxed, never null, falls back to the default of: `0F`
        val relaxedOrDefault: Float = json.floatValue
    }

    fun `value-double`(json: Json) {
        // nullable, `null` is json-element does not exist or is not of type Double
        val strict: Double? = json.double

        // relaxed, never null, falls back to the default of: `0.0`
        //  if json-element is absent or is not of type __Number__, otherwise
        //  the _best effort attempt_ is done to convert it (no checks for overflows or precision)
        val relaxedOrDefault: Double = json.doubleValue
    }

    fun `value-string`(json: Json) {
        // nullable, `null` is json-element does not exist or is not of type String
        val strict: String? = json.string

        // relaxed, never null, falls back to the default of: `""`
        val relaxedOrDefault: String = json.stringValue
    }

    fun `value-array`(json: Json) {
        // nullable, `null` is json-element does not exist or is not of type List<Json>
        val strict: List<Json>? = json.array

        // relaxed, never null, falls back to the default of: `[]` (empty array/list)
        val relaxedOrDefault: List<Json> = json.arrayValue
    }

    fun `value-jsonArray`(json: Json) {
        // nullable, `null` is json-element does not exist or is not of type List<JsonElement?>
        val strict: List<JsonElement?>? = json.jsonArray

        // relaxed, never null, falls back to the default of: ``
        val relaxedOrDefault: List<JsonElement?> = json.jsonArrayValue
    }
}

//object FactoryTest {
//
//    interface JsonObjectBuilder {
//
//    }
//
//    fun JsonObject(
//        builder: JsonFactory.(JsonObject) -> Unit = {}
//    ): JsonObject {
//        TODO()
//    }
//
//    fun hey() {
//        val jo = JsonObject()
//        val j2 = JsonObject {
//            it["hello"] = 1
//            it["again"] = true
//            it["nested"] = JsonObject {
//                it["hello"] = 42F
//            }
//            it["nested_array"] = JsonArray()
//            it["hey"] = JsonNull
//        }
//    }
//}