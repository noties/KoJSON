package io.noties.kojson.sample.jvm

import com.google.gson.Gson
import io.noties.kojson.jvm.gson.json
import com.google.gson.JsonElement as GsonElement

fun main() {

    val body = """
        {
          "key": {
            "sdjhf": {
              "sdfhjg": 4.9,
              "big_double": 340282350000000000000000000000000000001
            }
          },
          "dshjsjdfh": null
        }
    """.trimIndent()

    val element = Gson().fromJson(body, GsonElement::class.java)

    val json = element.json

    val bool = json["key"]["sdjhf"]["sdfhjg"].booleanValue
    val int = json["key"]["sdjhf"]["sdfhjg"].int
    val bigDouble = json["key"]["sdjhf"]["big_double"]
    println("bool:${bool} int:$int element:${json["dshjsjdfh"].element}")
    println("bigDouble int:${bigDouble.intValue} long:${bigDouble.longValue} float:${bigDouble.floatValue} double:${bigDouble.doubleValue}")

//    val back = json.gson
//    println("back:$back eq:${element == back}")

    println(json.element)
}
