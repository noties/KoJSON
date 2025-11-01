package io.noties.kojson.api

import kotlin.math.abs

// Note: numbers do not handle the overflows. This is done for simplicity. Normally, json should
//  contain relatively small numbers. If some really big numbers should be used
//  (say - greater than Int.MAX_VALUE), then it is better to send those numbers as strings and
//  parse them explicitly.
public class Json(
    public val element: JsonElement? = null
) {

    /**
     * # Boolean
     */
    public val boolean: Boolean? get() = element?.jsonPrimitive?.let {
        if (it.isBoolean) it.asBoolean else null
    }

    // if number, then 1 == true,
    //  if string then "true" == true
    // else getAsBoolean
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
                primitive.isString -> "true".equals(primitive.asString, true)
                else -> primitive.asBoolean ?: false
            }
        }

    /**
     * # Int
     */
    public val int: Int? get() = element?.jsonPrimitive?.let {
        if (it.isNumber) it.asInt else null
    }

    public val intValue: Int get() = int
        ?: element?.jsonPrimitive?.asString?.let {
            it.toIntOrNull() ?: it.toFloatOrNull()?.toInt()
        }
        ?: 0

    /**
     * # Long
     */
    public val long: Long? get() = element?.jsonPrimitive?.let {
        if (it.isNumber) it.asLong else null
    }

    public val longValue: Long get() = long
        ?: element?.jsonPrimitive?.asString?.let {
            it.toLongOrNull() ?: it.toDoubleOrNull()?.toLong()
        }
        ?: 0L

    /**
     * # Float
     */
    public val float: Float? get() = element?.jsonPrimitive?.let {
        if (it.isNumber) it.asFloat else null
    }

    public val floatValue: Float get() = float
        ?: element?.jsonPrimitive?.asString?.toFloatOrNull()
        ?: 0F


    /**
     * # Double
     */
    public val double: Double? get() = element?.jsonPrimitive?.let {
        if (it.isNumber) it.asDouble else null
    }

    public val doubleValue: Double get() = double
        ?: element?.jsonPrimitive?.asString?.toDoubleOrNull()
        ?: 0.0


    /**
     * # String
     */
    public val string: String? get() = element?.jsonPrimitive?.let {
        if (it.isString) it.asString else null
    }

    public val stringValue: String get() = string
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
