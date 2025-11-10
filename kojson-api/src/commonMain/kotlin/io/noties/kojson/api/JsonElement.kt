package io.noties.kojson.api

public sealed interface JsonElement {

    // to postpone checks, to move to runtime, so those are not parsed/initialized for each property
    //  before it is actually being used, which will impact parsing time
    // they are also just a way of optimization, instead of using the `as` checks,
    //  as proper type overrides its own designated property. it should be more performant that `as` checks
    public val isJsonObject: Boolean get() = false
    public val isJsonArray: Boolean get() = false
    public val isJsonPrimitive: Boolean get() = false
    public val isJsonNull: Boolean get() = false

    // we let implementation provide own mechanism of resolving the type,
    //  this way we can init only the elements that were asked about and not the whole json tree.
    public val asJsonObject: JsonObject get() = error("Not a JsonObject:$this")
    public val asJsonArray: JsonArray get() = error("Not a JsonArray:$this")
    public val asJsonPrimitive: JsonPrimitive get() = error("Not a JsonPrimitive:$this")
    public val asJsonNull: JsonNull get() = error("Not a JsonNull:$this")
}

/**
 * Is valid JSON element. If `null` is specified in JSON output:
 * ```json
 * {
 *   "some_key": null
 * }
 * ```
 * `some_key` in this case would be parsed as [JsonNull].
 * So, if some property is checked for existence with:
 * - [Json.exists] = `true`
 * - [Json.existsNotNull] = `false`
 */
public data object JsonNull : JsonElement {
    public override val isJsonNull: Boolean
        get() = true
    public override val asJsonNull: JsonNull
        get() = this
}

public interface JsonObject : JsonElement {
    public override val isJsonObject: Boolean
        get() = true
    public override val asJsonObject: JsonObject
        get() = this

    public fun get(key: String): JsonElement?

    /**
     * Passing `null` here would actually set json-null, so calling
     * this function with `null` and `JsonNull` should yield the same result
     */
    public fun add(key: String, value: JsonElement?)

    /**
     * In a case of json-null [JsonNull] would be returned, and not kotlin `null`
     * @return removed element or `null` if not present
     */
    public fun remove(key: String): JsonElement?

    public fun addProperty(key: String, value: Boolean)
    public fun addProperty(key: String, value: Int)
    public fun addProperty(key: String, value: Long)
    public fun addProperty(key: String, value: Float)
    public fun addProperty(key: String, value: Double)
    public fun addProperty(key: String, value: String)

    public operator fun set(key: String, value: Boolean): JsonObject = this.also { addProperty(key, value) }
    public operator fun set(key: String, value: Int): JsonObject = this.also { addProperty(key, value) }
    public operator fun set(key: String, value: Long): JsonObject = this.also { addProperty(key, value) }
    public operator fun set(key: String, value: Float): JsonObject = this.also { addProperty(key, value) }
    public operator fun set(key: String, value: Double): JsonObject = this.also { addProperty(key, value) }
    public operator fun set(key: String, value: String): JsonObject = this.also { addProperty(key, value) }
    public operator fun set(key: String, value: JsonElement?): JsonObject = this.also { add(key, value) }

    // TODO: check this
//    public operator fun JsonObject.set(key: String, value: JsonObject.() -> Unit): Unit
}

public interface JsonArray : JsonElement, Sequence<JsonElement> {
    public override val isJsonArray: Boolean
        get() = true
    public override val asJsonArray: JsonArray
        get() = this

    public val size: Int

    public fun add(element: JsonElement?)

    /**
     * @throws IndexOutOfBoundsException
     */
    @Throws(IndexOutOfBoundsException::class)
    public fun get(index: Int): JsonElement

    /**
     * @throws IndexOutOfBoundsException if given `index` is not within array's range
     */
    @Throws(IndexOutOfBoundsException::class)
    public fun remove(index: Int): JsonElement

    public override fun iterator(): Iterator<JsonElement> {
        return JsonArrayIterator(array = this)
    }
}

internal class JsonArrayIterator(
    val array: JsonArray
) : Iterator<JsonElement> {

    private var index: Int = 0

    override fun hasNext(): Boolean {
        return index < array.size
    }

    override fun next(): JsonElement {
        if (hasNext()) {
            return array.get(index++)
        } else {
            throw NoSuchElementException("index:$index size:${array.size} array:$array")
        }
    }
}

public interface JsonPrimitive : JsonElement {
    public override val isJsonPrimitive: Boolean
        get() = true
    public override val asJsonPrimitive: JsonPrimitive
        get() = this

    public val isBoolean: Boolean
    public val isNumber: Boolean
    public val isString: Boolean

    public val asBoolean: Boolean?
    public val asInt: Int?
    public val asLong: Long?
    public val asFloat: Float?
    public val asDouble: Double?
    public val asString: String?
}

public val JsonElement.json: Json get() = Json(this)

public val JsonElement.jsonObject: JsonObject?
    get() = if (this.isJsonObject) this.asJsonObject else null

public val JsonElement.jsonArray: JsonArray?
    get() = if (this.isJsonArray) this.asJsonArray else null

public val JsonElement.jsonPrimitive: JsonPrimitive?
    get() = if (this.isJsonPrimitive) this.asJsonPrimitive else null

public val JsonElement.jsonNull: JsonNull?
    get() = if (this.isJsonNull) this.asJsonNull else null
