package io.noties.kojson.jvm.gson.test

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.noties.kojson.api.JsonImplementation
import io.noties.kojson.jvm.gson.JsonImplementationGson
import io.noties.kojson.test.JsonNativeFactory
import io.noties.kojson.test.KoJSONTestSuite

class GsonKoJSONTestSuite : KoJSONTestSuite<JsonElement>() {

    override fun createFactory(): JsonNativeFactory<JsonElement> {
        return Factory
    }

    override fun createImplementation(): JsonImplementation<JsonElement> {
        return JsonImplementationGson
    }

    object Factory : JsonNativeFactory<JsonElement> {
        override fun `null`(): JsonElement {
            return JsonNull.INSTANCE
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
            return JsonObject()
        }

        override fun array(): JsonElement {
            return JsonArray()
        }

        override fun parse(json: String): JsonElement {
            return Gson().fromJson(json, JsonElement::class.java)
        }
    }
}
