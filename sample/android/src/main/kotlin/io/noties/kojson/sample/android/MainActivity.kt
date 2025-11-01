package io.noties.kojson.sample.android

import android.app.Activity
import android.os.Bundle
import io.noties.kojson.kmp.serialization.json
import io.noties.kojson.kmp.serialization.kson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class MainActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val body = """
        {
          "key": {
            "sdjhf": {
              "sdfhjg": 12
            }
          },
          "dshjsjdfh": null
        }
    """.trimIndent()

        val kson = Json
        val ksonElement = kson.decodeFromString<JsonElement>(body)

        val json = ksonElement.json

        val bool = json["key"]["sdjhf"]["sdfhjg"].booleanValue
        val int = json["key"]["sdjhf"]["sdfhjg"].int
        println("bool:${bool} int:$int element:${json["dshjsjdfh"].element}")

        val back = json.kson
        println("back:$back eq:${ksonElement == back}")
    }
}
