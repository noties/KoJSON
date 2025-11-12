package io.noties.kojson.sample.jvm.scratchpad

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

object GsonScratchpad {
    @JvmStatic
    fun main(args: Array<String>) {
        primitives()
    }

    fun primitives() {
        // would not be equals, as internally gson casts to double before comparing,
        //  thus some precision loss is possible in operations involving float numbers.
        val parsed = Gson().fromJson("42.1", JsonElement::class.java)
        val created = JsonPrimitive(42.1F)

        println("eq:${parsed == created} parsed:$parsed created:$created")
    }
}