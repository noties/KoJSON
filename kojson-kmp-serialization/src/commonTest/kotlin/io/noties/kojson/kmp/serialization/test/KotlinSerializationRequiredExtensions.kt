package io.noties.kojson.kmp.serialization.test

import io.noties.kojson.api.Json
import io.noties.kojson.api.JsonElement
import io.noties.kojson.api.JsonFactory
import io.noties.kojson.kmp.serialization.new
import io.noties.kojson.kmp.serialization.parse
import io.noties.kojson.test.KoJSONTestSuite.JsonRequiredExtensions

class KotlinSerializationRequiredExtensions: JsonRequiredExtensions {

    @Suppress("TestFunctionName")
    override fun <T : JsonElement> JsonElement_Companion_new(factory: JsonFactory.() -> T): T {
        return JsonElement.Companion.new(factory = factory)
    }

    @Suppress("TestFunctionName")
    override fun Json_Companion_parse(string: String): Json {
        return Json.Companion.parse(string = string)
    }
}