package io.noties.kojson.sample.jvm.scratchpad

import io.noties.kojson.api.Json
import io.noties.kojson.api.JsonArray
import io.noties.kojson.api.JsonElement
import io.noties.kojson.api.JsonNull
import io.noties.kojson.api.JsonObject
import io.noties.kojson.api.JsonPrimitive
import io.noties.kojson.jvm.gson.new

public interface __JsonFactory {

    // confusing name
//    public fun json(
//        vararg pairs: Pair<String, __JsonFactory.() -> JsonElement>
//    ): JsonObject = JsonElement.new { JsonObject() }

    // to distinguish with the keyword `object`
    public fun objectOf(
        vararg pairs: Pair<String, JsonElement>
    ): JsonObject = JsonElement.new { JsonObject() }
        .also {
            for ((key, value) in pairs) {
                it.add(key, value)
            }
        }

//    public fun `object`(
//        vararg pairs: Pair<String, __JsonFactory.() -> JsonElement>
//    ): JsonObject = JsonElement.new { JsonObject() }

    // TODO: ACTUALLY USE SUPPLIED VALUES
    public fun arrayOf(
        vararg elements: JsonElement
    ): JsonArray = JsonElement.new { JsonArray() }
        .also {
            for (element in elements) {
                it.add(element)
            }
        }

    // not very good
//    public fun Boolean.bool(): JsonPrimitive = JsonElement.new { JsonPrimitive(this) }

    public fun bool(value: Boolean): JsonPrimitive = JsonElement.new { JsonPrimitive(value) }
    public fun int(value: Int): JsonPrimitive = JsonElement.new { JsonPrimitive(value) }
    public fun long(value: Long): JsonPrimitive = JsonElement.new { JsonPrimitive(value) }
    public fun float(value: Float): JsonPrimitive = JsonElement.new { JsonPrimitive(value) }
    public fun double(value: Double): JsonPrimitive = JsonElement.new { JsonPrimitive(value) }
    public fun string(value: String): JsonPrimitive = JsonElement.new { JsonPrimitive(value) }

//    public fun primitive(bool: Boolean): JsonPrimitive = JsonElement.new { JsonPrimitive(value = bool) }
//    public fun primitive(int: Int): JsonPrimitive = JsonElement.new { JsonPrimitive(value = int) }
//    public fun primitive(long: Long): JsonPrimitive = JsonElement.new { JsonPrimitive(value = long) }
//    public fun primitive(float: Float): JsonPrimitive = JsonElement.new { JsonPrimitive(value = float) }
//    public fun primitive(double: Double): JsonPrimitive = JsonElement.new { JsonPrimitive(value = double) }
//    public fun primitive(string: String): JsonPrimitive = JsonElement.new { JsonPrimitive(value = string) }

    public val nil: JsonNull get() = JsonNull

    // so, accept the interface impl, but fallback to default one by companion, when this one is needed
    public companion object : __JsonFactory
}

public fun Json.Companion.__new(
    factory: __JsonFactory.() -> JsonElement = { objectOf() }
): Json {
    return Json(element = factory(__JsonFactory))
}

private fun checkFactory(someInt: Int?) {
    // someInt is nullable, how to specify it comfortably?

    val jpb = Json.__new { bool(true) }

    val jo = Json.__new {
        objectOf(
            "hello" to bool(true),
            "another-key" to arrayOf(
                int(42),
                bool(false),
                objectOf(
                    "hey" to int(1)
                )
            ),
            "whatever" to int(42),
            "nested" to objectOf(
                "nested-inside-really" to bool(true)
            ),
            "nullable" to nil,
            "the_int" to if (someInt != null) int(someInt) else nil
        )
    }

    println("jo:${jo.toString()}")

    val ja = Json.__new { arrayOf() }
}