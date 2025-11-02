package io.noties.kojson.api

public interface JsonFactory {
    public fun JsonObject(): JsonObject

    public fun JsonArray(): JsonArray

    public fun JsonPrimitive(value: Boolean): JsonPrimitive
    public fun JsonPrimitive(value: Int): JsonPrimitive
    public fun JsonPrimitive(value: Long): JsonPrimitive
    public fun JsonPrimitive(value: Float): JsonPrimitive
    public fun JsonPrimitive(value: Double): JsonPrimitive
    public fun JsonPrimitive(value: String): JsonPrimitive

    // for uniformity with other functions
    public fun JsonNull(): JsonNull = JsonNull

    public fun new(
        block: JsonFactory.() -> JsonElement
    ): JsonElement {
        return block(this)
    }
}