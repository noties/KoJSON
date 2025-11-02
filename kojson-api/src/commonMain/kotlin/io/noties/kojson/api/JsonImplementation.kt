package io.noties.kojson.api

public abstract class JsonImplementation<T : Any> : JsonFactory {
    // for extensions
    public companion object;

    // Intended to receive only real json element object
    public abstract fun of(type: T): JsonElement

    public abstract fun unwrap(element: JsonElement): T
}