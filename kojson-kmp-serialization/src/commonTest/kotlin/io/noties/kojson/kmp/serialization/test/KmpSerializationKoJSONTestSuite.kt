package io.noties.kojson.kmp.serialization.test

import io.noties.kojson.api.JsonImplementation
import io.noties.kojson.kmp.serialization.JsonImplementationKotlinxSerialization
import io.noties.kojson.test.JsonNativeFactory
import io.noties.kojson.test.KoJSONTestSuite
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertTrue

// IDE is not working at all, even though compilation works, IDE do not see those classes
//  CLASSIC.
class KmpSerializationKoJSONTestSuite: KoJSONTestSuite<JsonElement>() {

    override fun createFactory(): JsonNativeFactory<JsonElement> {
        return Factory
    }

    override fun createImplementation(): JsonImplementation<JsonElement> {
        return JsonImplementationKotlinxSerialization
    }
}

object Factory: JsonNativeFactory<JsonElement> {
    // aha, very interesting, Koltinx.Serialization treats null as primitive
    //  (from the revealers of 1-billion dollar java mistake)
    override fun `null`(): JsonElement {
        return JsonNull
    }

    override fun primitive(value: Boolean): JsonElement {
        return JsonPrimitive(value)
    }

    override fun primitive(value: Int): JsonElement {
       return JsonPrimitive(value)
    }

    override fun primitive(value: Long): JsonElement {
        return JsonPrimitive(value)
    }

    override fun primitive(value: Float): JsonElement {
        return JsonPrimitive(value)
    }

    override fun primitive(value: Double): JsonElement {
        return JsonPrimitive(value)
    }

    override fun primitive(value: String): JsonElement {
        return JsonPrimitive(value)
    }

    override fun `object`(): JsonElement {
        return JsonObject(content = emptyMap())
    }

    override fun array(): JsonElement {
        return JsonArray(content = emptyList())
    }

    override fun parse(json: String): JsonElement {
        return kotlinx.serialization.json.Json.parseToJsonElement(json)
    }
}

// HAD to copy here, because IDE does not see those classes :'(
// un-comment if needed to work against it

//interface JsonNativeFactory<T : Any> {
//    fun `null`(): T
//
//    fun primitive(value: Boolean): T
//    fun primitive(value: Int): T
//    fun primitive(value: Long): T
//    fun primitive(value: Float): T
//    fun primitive(value: Double): T
//    fun primitive(value: String): T
//
//    fun `object`(): T
//
//    fun array(): T
//
//    fun parse(json: String): T
//}
//
//abstract class JsonkTestSuite<T : Any> {
//
//    protected abstract fun createFactory(): JsonNativeFactory<T>
//    protected abstract fun createImplementation(): JsonImplementation<T>
//
//}
