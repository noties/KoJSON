package io.noties.kojson.kmp.serialization.test

import io.noties.kojson.api.JsonImplementation
import io.noties.kojson.kmp.serialization.JsonImplementationKotlinxSerialization
import io.noties.kojson.test.KoJSONTestSuite
import kotlinx.serialization.json.JsonElement

// IDE is not working at all, even though compilation works, IDE do not see those classes
//  CLASSIC.
class KmpSerializationKoJSONTestSuite: KoJSONTestSuite<JsonElement>() {
    override fun createImplementation(): JsonImplementation<JsonElement> {
        return JsonImplementationKotlinxSerialization
    }
}

// HAD to copy here, because IDE does not see those classes :'(
// un-comment if needed to work against it

//interface JsonNativeFactory<T : Any> {
//    fun `null`(): T
//
//    fun primitive(value: Boolean): T
//    fun primitive(value: Int): T
//    fun primitive(value: Long): T
//    fun primitive(value: Float): T
//    fun primitive(value: Double): T
//    fun primitive(value: String): T
//
//    fun `object`(): T
//
//    fun array(): T
//
//    fun parse(json: String): T
//}
//
//abstract class JsonkTestSuite<T : Any> {
//
//    protected abstract fun createFactory(): JsonNativeFactory<T>
//    protected abstract fun createImplementation(): JsonImplementation<T>
//
//}
