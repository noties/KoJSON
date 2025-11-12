package io.noties.kojson.sample.jvm.scratchpad

import io.noties.kojson.api.Json
import io.noties.kojson.api.JsonElement
import io.noties.kojson.kmp.serialization.new
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun main() {
    KotlinSerialization.heyToString()
}

object KotlinSerialization {
    fun heyToString() {
        val jo = JsonObject(
            mapOf(
                "hello" to JsonPrimitive(42),
                "world" to JsonPrimitive("world"),
                "whatever" to JsonNull,
                "nested" to JsonObject(
                    mapOf(
                        "yes" to JsonPrimitive("no")
                    )
                )
            )
        )
        println("toString:${jo.toString()}")
    }

    fun imports() {
        JsonElement.new { JsonObject() }
    }
}