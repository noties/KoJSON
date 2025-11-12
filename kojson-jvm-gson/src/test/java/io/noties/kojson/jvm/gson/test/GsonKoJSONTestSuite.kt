package io.noties.kojson.jvm.gson.test

import com.google.gson.JsonElement
import io.noties.kojson.api.JsonImplementation
import io.noties.kojson.jvm.gson.JsonImplementationGson
import io.noties.kojson.test.KoJSONTestSuite

class GsonKoJSONTestSuite : KoJSONTestSuite<JsonElement>() {
    override fun createImplementation(): JsonImplementation<JsonElement> {
        return JsonImplementationGson
    }

    override fun createJsonRequiredExtensions(): JsonRequiredExtensions {
        return GsonJsonRequiredExtensions()
    }
}
