package io.noties.kojson.api

import kotlin.math.abs

// TODO: JsonElement toString in kotlinx.serialization
// TODO: factories in kotlinx.s

// Note: numbers do not handle the overflows. This is done for simplicity. Normally, json should
//  contain relatively small numbers. If some really big numbers should be used
//  (say - greater than Int.MAX_VALUE), then it is better to send those numbers as strings and
//  parse them explicitly in code instead of relying on the JSON spec
public class Json(
    public val element: JsonElement?
) {

    public companion object;

    /**
     * Strict boolean.
     * Returns a boolean only if the JSON element exists and its value is a boolean; otherwise returns null.
     *
     * @see booleanValue
     */
    public val boolean: Boolean?
        get() = element?.jsonPrimitive?.let {
            if (it.isBoolean) it.asBoolean else null
        }

    /**
     * Best-effort boolean.
     * Attempts to parse the JSON element as a boolean by:
     * - Using [boolean] for strict boolean values
     * - Interpreting numeric `1` or `1.0` as true
     * - Interpreting string `"true"` as true
     * - `false` otherwise
     *
     * @see boolean
     */
    public val booleanValue: Boolean
        get() = boolean ?: run {
            val primitive: JsonPrimitive = element?.jsonPrimitive ?: return false
            return when {
                // NB! floating numbers must be exactly equal to `1.0` without remainder
                //  due to implicit conversions 1.2 when called `asInt` would return 1, which this method
                //  will gladly consider as true. Take into account floating number when
                //  doing this conversion
                primitive.isNumber -> primitive.asFloat?.let {
                    abs(1F / it - 1F) < 0.000001F
                } ?: false

                // only if it is a direct match to `true`, false otherwise
                primitive.isString -> "true".equals(primitive.asString, true)

                // at this point we have covered all values, no need to pass it to native impl with `asBoolean`
                else -> false
            }
        }

    /**
     * Strict Int.
     *
     * __NB__ as JSON spec does not distinguish between numeric types, it is possible
     * to have an unexpected conversion from a floating number to a decimal one. For example,
     * `42.2` will be returned as `42`
     */
    public val int: Int?
        get() = element?.jsonPrimitive?.let {
            if (it.isNumber) it.asInt else null
        }

    public val intValue: Int
        get() = int
            ?: element?.jsonPrimitive?.asString?.let {
                it.toIntOrNull() ?: it.toFloatOrNull()?.toInt()
            }
            ?: 0

    /**
     * # Long
     */
    public val long: Long?
        get() = element?.jsonPrimitive?.let {
            if (it.isNumber) it.asLong else null
        }

    public val longValue: Long
        get() = long
            ?: element?.jsonPrimitive?.asString?.let {
                it.toLongOrNull() ?: it.toDoubleOrNull()?.toLong()
            }
            ?: 0L

    /**
     * # Float
     */
    public val float: Float?
        get() = element?.jsonPrimitive?.let {
            if (it.isNumber) it.asFloat else null
        }

    public val floatValue: Float
        get() = float
            ?: element?.jsonPrimitive?.asString?.toFloatOrNull()
            ?: 0F


    /**
     * # Double
     */
    public val double: Double?
        get() = element?.jsonPrimitive?.let {
            if (it.isNumber) it.asDouble else null
        }

    public val doubleValue: Double
        get() = double
            ?: element?.jsonPrimitive?.asString?.toDoubleOrNull()
            ?: 0.0


    /**
     * # String
     */
    public val string: String?
        get() = element?.jsonPrimitive?.let {
            if (it.isString) it.asString else null
        }

    public val stringValue: String
        get() = string
            ?: element?.jsonPrimitive?.asString
            ?: ""


    /**
     * # Array
     */
    public val array: List<Json>? get() = element?.jsonArray?.map(::Json)?.toList()

    public val arrayValue: List<Json> get() = array ?: emptyList()


    /**
     * # JsonArray
     */
    public val jsonArray: List<JsonElement?>? get() = element?.jsonArray?.map { it }?.toList()

    public val jsonArrayValue: List<JsonElement> get() = jsonArray?.mapNotNull { it } ?: emptyList()


    // I do not remember ever needing those, but there is a possibility of adding:
    //  - object, objectValue (Map<String, Json>?, Map<String, Json>)
    //  - jsonObject, jsonObjectValue (Map<String, JsonElement>?, Map<String, JsonElement>)
    // it seems a little abundant, as Json by itself more-or-less is considered as `object` already

    public operator fun get(key: String): Json = Json(element?.jsonObject?.get(key))

    /**
     * The difference is - for JSON `null` is a valid identifier, so for example:
     *
     * ```json
     * {
     *   "some_key": null
     * }
     * ```
     * ```kotlin
     * val json = Json(data)
     * println(json["some_key"].exists())
     * ```
     * would print = `true`, because `null` is present.
     *
     * @see existsNotNull
     * @see takeIfExists
     */
    public fun exists(): Boolean = element != null

    /**
     * By JSON spec `null` is existing element, there is no exception for it,
     * if it is specified in JSON, then it exists. Use this function if you need
     * to be sure that JSON property exists and is not JSON null.
     * @see JsonNull
     * @see exists
     * @see takeIfExistsNotNull
     */
    public fun existsNotNull(): Boolean = element != null && element != JsonNull

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Json

        return element == other.element
    }

    override fun hashCode(): Int {
        return element?.hashCode() ?: 0
    }

    override fun toString(): String {
        return element?.toString() ?: ""
    }
}

/**
 * @see [Json.exists]
 * @see [takeIfExistsNotNull]
 */
public fun Json.takeIfExists(): Json? = this.takeIf { it.exists() }

/**
 * @see [Json.existsNotNull]
 * @see [takeIfExists]
 */
public fun Json.takeIfExistsNotNull(): Json? = this.takeIf { it.existsNotNull() }
