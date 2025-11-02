package io.noties.kojson.kmp.serialization

import io.noties.kojson.api.Json
import io.noties.kojson.api.JsonArray
import io.noties.kojson.api.JsonElement
import io.noties.kojson.api.JsonImplementation
import io.noties.kojson.api.JsonNull
import io.noties.kojson.api.JsonObject
import io.noties.kojson.api.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.JsonArray as KsonArray
import kotlinx.serialization.json.JsonElement as KsonElement
import kotlinx.serialization.json.JsonNull as KsonNull
import kotlinx.serialization.json.JsonObject as KsonObject
import kotlinx.serialization.json.JsonPrimitive as KsonPrimitive


public val KsonElement.json: Json get() = Json(JsonImplementationKotlinxSerialization.of(this))

// Alias to `json`
@Suppress("SpellCheckingInspection")
public val KsonElement.kojson: Json get() = json

@Suppress("SpellCheckingInspection")
public val Json.kson: KsonElement
    get() = JsonImplementationKotlinxSerialization.unwrap(element ?: JsonNull)

@Suppress("SpellCheckingInspection")
public val JsonElement.kson: KsonElement
    get() = JsonImplementationKotlinxSerialization.unwrap(this)

internal object JsonImplementationKotlinxSerialization : JsonImplementation<KsonElement>() {

    override fun JsonObject(): JsonObject = KsonObjectImpl()

    override fun JsonArray(): JsonArray = KsonArrayImpl()

    override fun JsonPrimitive(value: Boolean): JsonPrimitive = KsonPrimitiveImpl(value)
    override fun JsonPrimitive(value: Int): JsonPrimitive = KsonPrimitiveImpl(value)
    override fun JsonPrimitive(value: Long): JsonPrimitive = KsonPrimitiveImpl(value)
    override fun JsonPrimitive(value: Float): JsonPrimitive = KsonPrimitiveImpl(value)
    override fun JsonPrimitive(value: Double): JsonPrimitive = KsonPrimitiveImpl(value)
    override fun JsonPrimitive(value: String): JsonPrimitive = KsonPrimitiveImpl(value)

    // :'(
    // they have `JsonNull` extend `JsonPrimitive`
    //  it is coming from the team that zealously reminds the world about 1-billion dollar mistake
    //  `null` being primitive is weird type system (can object be null?, an array?, so the types
    //  becomes skewed, and what had been object without `null`, will become primitive when value is `null`.
    //  So, instead of introducing `null` as special type (which is considered to be _the_ mistake with null in Java)
    //  they made null a type that extend primitive...
    override fun of(type: KsonElement): JsonElement {
        return when (type) {
            is KsonObject -> KsonObjectImpl(type)
            is KsonArray -> KsonArrayImpl(type)
            // MOVED up, because JsonNull extend JsonPrimitive
            KsonNull -> JsonNull
            is KsonPrimitive -> KsonPrimitiveImpl(type)
        }
    }

    override fun unwrap(element: JsonElement): KsonElement {
        return when (element) {
            is JsonObject -> (element as KsonObjectImpl).jsonObject
            is JsonArray -> (element as KsonArrayImpl).jsonArray
            JsonNull -> KsonNull
            is JsonPrimitive -> (element as KsonPrimitiveImpl).jsonPrimitive
        }
    }
}

internal class KsonObjectImpl(
    var jsonObject: KsonObject = KsonObject(emptyMap())
) : JsonObject {
    override fun get(key: String): JsonElement? {
        val element = jsonObject[key]
        return if (element == null) {
            null
        } else {
            JsonImplementationKotlinxSerialization.of(element)
        }
    }

    // NB! that kotlinx.serialization would create a copy for each mutation
    override fun add(key: String, value: JsonElement?) {
        add(
            key,
            if (value == null) {
                KsonNull
            } else {
                JsonImplementationKotlinxSerialization.unwrap(value)
            }
        )
    }

    override fun remove(key: String): JsonElement? {
        val kson = jsonObject[key]
        return if (kson == null) {
            null
        } else {
            // mutate the map
            jsonObject = KsonObject(jsonObject.toMutableMap().apply {
                remove(key)
            })
            JsonImplementationKotlinxSerialization.of(kson)
        }
    }

    override fun addProperty(key: String, value: Boolean) {
        add(key, KsonPrimitive(value))
    }

    override fun addProperty(key: String, value: Int) {
        add(key, KsonPrimitive(value))
    }

    override fun addProperty(key: String, value: Long) {
        add(key, KsonPrimitive(value))
    }

    override fun addProperty(key: String, value: Float) {
        add(key, KsonPrimitive(value))
    }

    override fun addProperty(key: String, value: Double) {
        add(key, KsonPrimitive(value))
    }

    override fun addProperty(key: String, value: String) {
        add(key, KsonPrimitive(value))
    }

    private fun add(key: String, property: KsonElement?) {
        jsonObject = KsonObject(jsonObject.toMutableMap().apply {
            put(key, property ?: KsonNull)
        })
    }

    override val asJsonObject: JsonObject
        get() = this

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as KsonObjectImpl

        return jsonObject == other.jsonObject
    }

    override fun hashCode(): Int {
        return jsonObject.hashCode()
    }
}

internal class KsonArrayImpl(
    var jsonArray: KsonArray = KsonArray(emptyList())
) : JsonArray {

    override val size: Int
        get() = jsonArray.size

    override fun add(element: JsonElement?) {
        add(
            if (element == null) {
                KsonNull
            } else {
                JsonImplementationKotlinxSerialization.unwrap(element)
            }
        )
    }

    override fun get(index: Int): JsonElement {
        if (index !in 0..<size) {
            throw IndexOutOfBoundsException("index:$index size:$size")
        }
        return JsonImplementationKotlinxSerialization.of(jsonArray.get(index))
    }

    override fun remove(index: Int): JsonElement {
        if (index !in 0..<size) {
            throw IndexOutOfBoundsException("index:$index size:$size")
        }

        val kson = jsonArray[index]
        jsonArray = KsonArray(jsonArray.toMutableList().apply {
            removeAt(index)
        })
        return JsonImplementationKotlinxSerialization.of(kson)
    }

    private fun add(value: KsonElement) {
        jsonArray = KsonArray(jsonArray.toMutableList().apply {
            add(value)
        })
    }

    override val asJsonArray: JsonArray
        get() = this

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as KsonArrayImpl

        return jsonArray == other.jsonArray
    }

    override fun hashCode(): Int {
        return jsonArray.hashCode()
    }
}

internal class KsonPrimitiveImpl(
    val jsonPrimitive: KsonPrimitive
) : JsonPrimitive {
    constructor(value: Boolean) : this(KsonPrimitive(value))
    constructor(value: Int) : this(KsonPrimitive(value))
    constructor(value: Long) : this(KsonPrimitive(value))
    constructor(value: Float) : this(KsonPrimitive(value))
    constructor(value: Double) : this(KsonPrimitive(value))
    constructor(value: String) : this(KsonPrimitive(value))

    // Interesting (not). `"true"` (string) is parsed as boolean
    //  How (not) to implement strict type type system (primitive is backed by string property)
    override val isBoolean: Boolean
        /**
         * We do not want `"true"` to be returned here as boolean, as it is not - it is string!
         * It is weird that it is how it works on `kotlin.serialization` side
         */
        get() = if (jsonPrimitive.isString) false else jsonPrimitive.booleanOrNull != null

    /**
     * It seems that this condition needs to met these requirements:
     * - if something can be parsed as long or double, it can be a number.
     * - but also, BUT MUST NOT BE STRING. If we do not check that then
     *   a string `"43"` will be incorrectly labeled as `isNumber=true`
     */
    override val isNumber: Boolean
        get() = if (jsonPrimitive.isString) false else (jsonPrimitive.longOrNull
            ?: jsonPrimitive.doubleOrNull) != null

    override val isString: Boolean
        get() = jsonPrimitive.isString

    override val asBoolean: Boolean
        get() = jsonPrimitive.boolean

    /**
     * As int should not throw if it is JsonPrimitive.Number, because json does not specify
     * this directly, thus all possible numbers are represented by a single type. Most of
     * other json parsers follow the same rule, this code aligns with them.
     */
    override val asInt: Int
        get() = try {
            jsonPrimitive.int
        } catch (e: NumberFormatException) {
            // try to parse as double, and then convert to it
            jsonPrimitive.double.toInt()
        }

    /**
     * @see asInt
     */
    override val asLong: Long
        get() = try {
            jsonPrimitive.long
        } catch (e: NumberFormatException) {
            jsonPrimitive.double.toLong()
        }

    override val asFloat: Float
        get() = jsonPrimitive.float

    override val asDouble: Double
        get() = jsonPrimitive.double

    override val asString: String
        get() = jsonPrimitive.content

    override val asJsonPrimitive: JsonPrimitive
        get() = this

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as KsonPrimitiveImpl

        return jsonPrimitive == other.jsonPrimitive
    }

    override fun hashCode(): Int {
        return jsonPrimitive.hashCode()
    }
}
