package io.noties.kojson.api

public abstract class JsonImplementation<T : Any> : JsonFactory {
    // for extensions
    public companion object;

    /**
     * Wrap native target element into [JsonElement]
     */
    public abstract fun of(type: T): JsonElement

    /**
     * Convert back to native target element (returned wrapped element)
     */
    public abstract fun unwrap(element: JsonElement): T

    /**
     * Best-effort JSON parsing according to the implementation internals.
     * For a more controlled version, parsing should be done by clients
     * according to the business rules and then converted to [JsonElement]
     * to be consumed further.
     */
    public abstract fun parse(json: String): JsonElement?

    /**
     * Convert to JSON string. Actual output depends on the implementation,
     * but generally should output raw JSON without any modifications (like prettifying it)
     */
    public abstract fun toJsonString(
        element: JsonElement
    ): String
}