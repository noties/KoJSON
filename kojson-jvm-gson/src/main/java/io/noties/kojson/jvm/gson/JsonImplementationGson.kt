package io.noties.kojson.jvm.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import io.noties.kojson.api.Json
import io.noties.kojson.api.JsonArray
import io.noties.kojson.api.JsonElement
import io.noties.kojson.api.JsonImplementation
import io.noties.kojson.api.JsonNull
import io.noties.kojson.api.JsonObject
import io.noties.kojson.api.JsonPrimitive
import com.google.gson.JsonArray as GsonArray
import com.google.gson.JsonElement as GsonElement
import com.google.gson.JsonNull as GsonNullJava
import com.google.gson.JsonObject as GsonObject
import com.google.gson.JsonPrimitive as GsonPrimitive

public val GsonElement.json: Json get() = Json(JsonImplementationGson.of(this))

// Alias to `json`
@Suppress("SpellCheckingInspection")
public val GsonElement.kojson: Json get() = json

// this is a little weird, because it falls back to JsonNull, which might be confusing
//  as there is difference between java-null and json-null, so if a thing is absent it should
//  be undefined, like java-null, but if it is `null` in json, it should be json-null
//public val Json.gson: GsonElement get() = JsonImplementationGson.unwrap(element ?: JsonNull)

public val JsonElement.gson: GsonElement get() = JsonImplementationGson.unwrap(this)

private val GsonNull: GsonNullJava get() = GsonNullJava.INSTANCE

internal object JsonImplementationGson : JsonImplementation<GsonElement>() {

    internal val gson: Gson by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        // not pretty, let it be _ugly_ compact
        GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create()
    }

    override fun JsonObject(): JsonObject = GsonObjectImpl()

    override fun JsonArray(): JsonArray = GsonArrayImpl()

    override fun JsonPrimitive(value: Boolean): JsonPrimitive = GsonPrimitiveImpl(value)
    override fun JsonPrimitive(value: Int): JsonPrimitive = GsonPrimitiveImpl(value)
    override fun JsonPrimitive(value: Long): JsonPrimitive = GsonPrimitiveImpl(value)
    override fun JsonPrimitive(value: Float): JsonPrimitive = GsonPrimitiveImpl(value)
    override fun JsonPrimitive(value: Double): JsonPrimitive = GsonPrimitiveImpl(value)
    override fun JsonPrimitive(value: String): JsonPrimitive = GsonPrimitiveImpl(value)

    override fun of(type: GsonElement): JsonElement {
        return when (type) {
            is GsonObject -> GsonObjectImpl(type)
            is GsonArray -> GsonArrayImpl(type)
            is GsonPrimitive -> GsonPrimitiveImpl(type)
            GsonNull -> JsonNull
            else -> error("Unexpected gson element:$type")
        }
    }

    override fun unwrap(element: JsonElement): GsonElement {
        return when (element) {
            is JsonArray -> (element as GsonArrayImpl).jsonArray
            JsonNull -> GsonNull
            is JsonObject -> (element as GsonObjectImpl).jsonObject
            is JsonPrimitive -> (element as GsonPrimitiveImpl).jsonPrimitive
        }
    }

    override fun parse(json: String): JsonElement? {
        return try {
            @Suppress("ComplexRedundantLet")
            gson.fromJson(json, GsonElement::class.java)
                .let { of(it) }
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    override fun toJsonString(element: JsonElement): String {
        return gson.toJson(unwrap(element))
    }
}

internal class GsonObjectImpl(
    val jsonObject: GsonObject = GsonObject()
) : JsonObject {

    override fun get(key: String): JsonElement? {
        val element = jsonObject.get(key)
        return if (element == null) {
            null
        } else {
            JsonImplementationGson.of(element)
        }
    }

    override fun add(key: String, value: JsonElement?) {
        if (value == null) {
            jsonObject.add(key, GsonNull)
        } else {
            jsonObject.add(key, JsonImplementationGson.unwrap(value))
        }
    }

    override fun remove(key: String): JsonElement? {
        val gson = jsonObject.remove(key)
        return if (gson == null) {
            null
        } else {
            JsonImplementationGson.of(gson)
        }
    }

    override fun addProperty(key: String, value: Boolean) {
        jsonObject.addProperty(key, value)
    }

    override fun addProperty(key: String, value: Int) {
        jsonObject.addProperty(key, value)
    }

    override fun addProperty(key: String, value: Long) {
        jsonObject.addProperty(key, value)
    }

    override fun addProperty(key: String, value: Float) {
        jsonObject.addProperty(key, value)
    }

    override fun addProperty(key: String, value: Double) {
        jsonObject.addProperty(key, value)
    }

    override fun addProperty(key: String, value: String) {
        jsonObject.addProperty(key, value)
    }

    override val asJsonObject: JsonObject
        get() = this

    override fun toString(): String {
        return "JsonObject($jsonObject)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GsonObjectImpl

        return jsonObject == other.jsonObject
    }

    override fun hashCode(): Int {
        return jsonObject.hashCode()
    }
}

internal class GsonArrayImpl(
    val jsonArray: GsonArray = GsonArray()
) : JsonArray {
    override val size: Int
        get() = jsonArray.size()

    override fun add(element: JsonElement?) {
        if (element == null) {
            jsonArray.add(GsonNull)
        } else {
            jsonArray.add(JsonImplementationGson.unwrap(element))
        }
    }

    override fun get(index: Int): JsonElement {
        return JsonImplementationGson.of(jsonArray.get(index))
    }

    override fun remove(index: Int): JsonElement {
        val gson = jsonArray.remove(index)
        return JsonImplementationGson.of(gson)
    }

    override val asJsonArray: JsonArray
        get() = this

    override fun toString(): String {
        return "JsonArray($jsonArray)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GsonArrayImpl

        return jsonArray == other.jsonArray
    }

    override fun hashCode(): Int {
        return jsonArray.hashCode()
    }
}

internal class GsonPrimitiveImpl(
    val jsonPrimitive: GsonPrimitive
) : JsonPrimitive {

    constructor(value: Boolean) : this(GsonPrimitive(value))
    constructor(value: Int) : this(GsonPrimitive(value))
    constructor(value: Long) : this(GsonPrimitive(value))
    constructor(value: Float) : this(GsonPrimitive(value))
    constructor(value: Double) : this(GsonPrimitive(value))
    constructor(value: String) : this(GsonPrimitive(value))

    override val isBoolean: Boolean
        get() = jsonPrimitive.isBoolean

    override val isNumber: Boolean
        get() = jsonPrimitive.isNumber

    override val isString: Boolean
        get() = jsonPrimitive.isString

    override val asBoolean: Boolean
        get() = jsonPrimitive.asBoolean

    override val asInt: Int
        get() = jsonPrimitive.asInt

    override val asLong: Long
        get() = jsonPrimitive.asLong

    override val asFloat: Float
        get() = jsonPrimitive.asFloat

    override val asDouble: Double
        get() = jsonPrimitive.asDouble

    override val asString: String
        get() = jsonPrimitive.asString

    override val asJsonPrimitive: JsonPrimitive
        get() = this

    override fun toString(): String {
        return "JsonPrimitive($jsonPrimitive)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GsonPrimitiveImpl

        return jsonPrimitive == other.jsonPrimitive
    }

    override fun hashCode(): Int {
        return jsonPrimitive.hashCode()
    }
}
