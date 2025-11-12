package io.noties.kojson.test

import io.noties.kojson.api.Json
import io.noties.kojson.api.JsonElement
import io.noties.kojson.api.JsonFactory
import io.noties.kojson.api.JsonImplementation
import io.noties.kojson.api.JsonNull
import io.noties.kojson.api.JsonObject
import io.noties.kojson.api.takeIfExists
import io.noties.kojson.api.takeIfExistsNotNull
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("TestFunctionName")
abstract class KoJSONTestSuite<T : Any> {

    companion object {
        // not very small value due to the Float checks (reduced precision)
        const val EPSILON: Double = 1e-5
    }

    //---------------------------------------------------
    //#region _
    //---------------------------------------------------

    //    // but... does it give any guarantee that nested class tests would be run afterwards?
//    // @BeforeClass is not supported in Kotlin/Common 🤯
//    @BeforeTest
//    fun before() {
//        println("HELLO FROM BEFORE SUITE:" + System.nanoTime())
//    }


    // there must be at least some amount of trust :)
    interface JsonRequiredExtensions {

        fun <T : JsonElement> JsonElement_Companion_new(factory: JsonFactory.() -> T): T

        /**
         * Sad. Kotlin-doc does not support `@inheritDoc`, which is useful in some situations.
         * [discussion](https://discuss.kotlinlang.org/t/ktdoc-dokka-inheritdoc/4965/3)
         *
         * @rethrows
         * @inheritDoc
         */
        fun Json_Companion_parse(string: String): Json
    }

    protected abstract fun createImplementation(): JsonImplementation<T>

    protected abstract fun createJsonRequiredExtensions(): JsonRequiredExtensions

    // as JsonImplementation should be stateless, we create it each time it is accessed,
    //  as it must not have any inner state
    @Suppress("MemberVisibilityCanBePrivate")
    protected val implementation: JsonImplementation<T> get() = createImplementation()

    // would it be executed? the answer might not be very pleasing
//    public inner class NativeTypeTest: io.noties.jsonk.test.suite.NativeTypeTest<T>(this)
//
//    public inner class JsonNullTest: io.noties.jsonk.test.suite.JsonNullTest<T>(this)
//
//    public inner class JsonTest: io.noties.jsonk.test.suite.JsonTest()

    /**
     * Kotlin was meant to be a reliable companion and an effective tool
     * to help you deliver software efficiently. Instead, it often feels
     * like an adversary—an environment where you spend more time
     * wrestling with its quirks than solving real problems. Its surprising and
     * sometimes baffling shortcomings make development unnecessarily complex.
     *
     * Even the error messages, which one would expect to be refined given
     * the expertise of the tooling company behind Kotlin, often add to the frustration
     * rather than alleviating it.
     *
     * Kotlin has become a language that demands not just proficiency
     * but genuine affection. If you don’t _love_ it, you’re in trouble—because
     * only strong emotional commitment can carry you through its maze
     * of inconsistencies and design oddities.
     *
     * It’s not a particularly well-balanced language. Many of its features,
     * while interesting in isolation, interact poorly in practice.
     * As a result, developers spend disproportionate amounts of time fighting
     * the language instead of focusing on building meaningful functionality.
     *
     * Eventually, you realize you’ve wasted hours resolving an issue
     * that exists solely due to Kotlin’s own limitations—only to find
     * that the “solution” has grown more complicated than the actual business
     * logic you were trying to implement.
     *
     * One can’t help but wonder: how did this become acceptable?
     * How are so many developers comfortable with this experience—and
     * what keeps them loyal to it?
     */

    // Inner class is no good, as as far as I understand, there is no guarantee that
    //  its tests are going to be run after parent class is initialized (and moreover
    //  there is no BEFORE-CLASS in koltin.test in Kotlin/Common, so only `@BeforeTest` is available
    //  which is run before each test IN PARENT class, no nested. Amazingly bad testing support.
//    class MyInnerTest {
//        @kotlin.test.BeforeTest
//        public fun before() {
//            println("HELLO FROM BEFORE INNER:" + System.nanoTime())
//        }
//
//        @Test
//        fun test() {
//            assertTrue("so true") { false }
//        }
//    }
//
//    @Suppress("MemberVisibilityCanBePrivate")
//    protected fun new(block: JsonImplementation<T>.() -> JsonElement): JsonElement {
//        return block(implementation)
//    }

    @Suppress("MemberVisibilityCanBePrivate", "ComplexRedundantLet")
    protected val SampleJson.json: Json get() = implementation.parse(rawJson)!!.let { Json(it) }

    //---------------------------------------------------
    //#endregion _
    //---------------------------------------------------


    //----------------------------------------------------------------------------------------------
    //#region [JsonNull]
    //----------------------------------------------------------------------------------------------

    @Test
    fun JsonNull_all_values_null() {

        val sample = object : SampleJson {
            val keys =
                listOf("boolean", "int", "long", "float", "double", "string", "object", "array")

            override val rawJson: String
                get() = """
                {
${keys.joinToString(separator = ",\n") { "\"${it}\": null" }}
                }
            """.trimIndent()
        }

        val json = sample.json

        for (key in sample.keys) {
            assertNull(json[key].boolean, key)
            assertNull(json[key].int, key)
            assertNull(json[key].long, key)
            assertNull(json[key].float, key)
            assertNull(json[key].double, key)
            assertNull(json[key].string, key)
            assertNull(json[key].array, key)
            assertNull(json[key].jsonArray, key)
        }
    }

    //----------------------------------------------------------------------------------------------
    //#endregion --[JsonNull]
    //----------------------------------------------------------------------------------------------

    //---------------------------------------------------
    //#region [Json]
    //---------------------------------------------------

    @Test
    fun Json_defaults() {
        val json = Json(null)

        assertEquals(false, json.booleanValue)
        assertEquals(0, json.intValue)
        assertEquals(0L, json.longValue)
        assertEquals(0F, json.floatValue)
        assertEquals(0.0, json.doubleValue)
        assertEquals("", json.stringValue)
        assertEquals(emptyList(), json.arrayValue)
        assertEquals(emptyList(), json.jsonArrayValue)
    }

    @Test
    fun Json_exists() {
        val sample = object : SampleJson {
            val keyNull = "null"

            override val rawJson: String
                get() = """
                    {
                      "$keyNull": null
                    }
                """.trimIndent()
        }

        val json = sample.json

        // `null` in JSON exists
        assertEquals(true, json[sample.keyNull].exists())
        assertEquals(false, json[sample.keyNull].existsNotNull())

        assertEquals(false, json["some_weird"].exists(), "${json["some_weird"].element}")
        assertEquals(false, json["some_weird"].existsNotNull())
    }

    @Test
    fun Json_existsNotNull() {
        data class Input(
            val key: String,
            val value: JsonElement?,
            val existsNotNull: Boolean
        )

        val inputs = listOf(
            Input(
                key = "null",
                value = JsonNull,
                existsNotNull = false
            ),
            Input(
                key = "bool",
                value = implementation.JsonPrimitive(true),
                existsNotNull = true
            ),
            Input(
                key = "int",
                value = implementation.JsonPrimitive(42),
                existsNotNull = true
            ),
            Input(
                key = "object",
                value = implementation.JsonObject(),
                existsNotNull = true
            ),
            Input(
                key = "array",
                value = implementation.JsonArray(),
                existsNotNull = true
            )
        )

        for ((key, value, existsNotNull) in inputs) {
            val element = Json(value)
            assertEquals(existsNotNull, element.existsNotNull(), "key:$key element:$value")
        }

        // ensure missing element (null root) reports false
        val missing = Json(null)
        assertEquals(false, missing.existsNotNull())
    }

    @Test
    fun Json_takeIfExists() {
        val sample = object : SampleJson {
            val keyNull = "key_null"
            val keyBool = "key_bool"
            val keyInt = "key_int"

            override val rawJson: String
                get() = """
                    {
                      "$keyNull": null,
                      "$keyBool": true,
                      "$keyInt": 42
                    }
                """.trimIndent()
        }

        val json = sample.json

        val nullElement = json[sample.keyNull]
        val boolElement = json[sample.keyBool]
        val intElement = json[sample.keyInt]
        val missingElement = json["missing"]

        run {
            val result = nullElement.takeIfExists()
            assertNotNull(result, "null element must be returned")
            assertTrue(result === nullElement)
        }

        run {
            val result = boolElement.takeIfExists()
            assertNotNull(result, "bool element must be returned")
            assertTrue(result === boolElement)
        }

        run {
            val result = intElement.takeIfExists()
            assertNotNull(result, "int element must be returned")
            assertTrue(result === intElement)
        }

        assertEquals(null, missingElement.takeIfExists(), "missing element must be null")
    }

    @Test
    fun Json_takeIfExistsNotNull() {
        val sample = object : SampleJson {
            val keyNull = "key_null"
            val keyBool = "key_bool"
            val keyInt = "key_int"

            override val rawJson: String
                get() = """
                    {
                      "$keyNull": null,
                      "$keyBool": true,
                      "$keyInt": 42
                    }
                """.trimIndent()
        }

        val json = sample.json

        val nullElement = json[sample.keyNull]
        val boolElement = json[sample.keyBool]
        val intElement = json[sample.keyInt]
        val missingElement = json["missing"]

        assertEquals(null, nullElement.takeIfExistsNotNull(), "null element must be filtered out")

        run {
            val result = boolElement.takeIfExistsNotNull()
            assertNotNull(result, "bool element must be returned")
            assertTrue(result === boolElement)
        }

        run {
            val result = intElement.takeIfExistsNotNull()
            assertNotNull(result, "int element must be returned")
            assertTrue(result === intElement)
        }

        assertEquals(null, missingElement.takeIfExistsNotNull(), "missing element must be null")
    }

//    @Test
//    fun Json_comments() {
//        // json engine must allow comments, isn't it too big stretch to assume that?
//        // yes, seems so
//    }

    //---------------------------------------------------
    //#endregion [Json]
    //---------------------------------------------------

    //---------------------------------------------------
    //#region [Native]
    //---------------------------------------------------

    @Test
    fun Native_JsonPrimitive_isJsonPrimitive() {
        val data = listOf(
            "false" to implementation.JsonPrimitive(false),
            "0" to implementation.JsonPrimitive(0),
            "1" to implementation.JsonPrimitive(1L),
            "2F" to implementation.JsonPrimitive(2F),
            "3_0" to implementation.JsonPrimitive(3.0),
            "(empty-string)" to implementation.JsonPrimitive(""),
            "\"hello json\"" to implementation.JsonPrimitive("hello json"),
        )

        for ((name, element) in data) {
            assertTrue(element.isJsonPrimitive, name)
            assertFalse(element.isJsonObject, name)
            assertFalse(element.isJsonArray, name)
        }
    }

    @Test
    fun Native_JsonObject_isJsonObject() {
        val element = implementation.JsonObject()
        assertFalse(element.isJsonPrimitive, "isJsonPrimitive=false")
        assertTrue(element.isJsonObject, "isJsonObject=true")
        assertFalse(element.isJsonArray, "isJsonArray=false")
    }

    @Test
    fun Native_JsonObject_add() {

        fun run(block: (JsonObject) -> Unit) {
            val element = implementation.JsonObject()
            block(element)
        }

        data class Input(
            val key: String,
            val element: JsonElement?
        )

        // setting null should add `JsonNull`
        run {
            val key = "key_null"
            it.add(key, null)

            assertEquals(JsonNull, it.get(key), "null")
        }

        val inputs = listOf(
            Input("key_null", implementation.JsonNull()),
            Input(key = "key_bool", implementation.JsonPrimitive(true)),
            Input(key = "key_int", implementation.JsonPrimitive(42)),
            Input(key = "key_object", implementation.JsonObject()),
            Input(key = "key_array", implementation.JsonArray())
        )

        for ((key, el) in inputs) {
            run {
                it.add(key, el)
                assertEquals(el, it.get(key), "key")
            }
        }
    }

    @Test
    fun Native_JsonArray_isJsonArray() {
        val element = implementation.JsonArray()
        assertFalse(element.isJsonPrimitive, "isJsonPrimitive=false")
        assertFalse(element.isJsonObject, "isJsonObject=false")
        assertTrue(element.isJsonArray, "isJsonArray=true")
    }

    @Test
    fun Native_JsonNull() {
        val element = implementation.JsonNull()
        assertFalse(element.isJsonPrimitive, "isJsonPrimitive=false")
        assertFalse(element.isJsonObject, "isJsonObject=false")
        assertFalse(element.isJsonArray, "isJsonArray=false")

        // singleton
        assertEquals(element, JsonNull)
        assertTrue(element === JsonNull)
    }

    //---------------------------------------------------
    //#endregion [Native]
    //---------------------------------------------------

    //---------------------------------------------------
    //#region [JsonPrimitive]
    //---------------------------------------------------

    @Test
    fun JsonPrimitive_Boolean() {

        data class BooleanInput(
            val key: String,
            val boolean: Boolean? = null,
            val booleanValue: Boolean
        ) {
            constructor(
                key: String,
                boolean: Boolean
            ) : this(key = key, boolean = boolean, booleanValue = boolean)
        }

        val sample = object : SampleJson {
            val keyBooleanTrue get() = "boolean_true"
            val keyBooleanFalse get() = "boolean_false"

            val keyNotBooleanInt get() = "n_boolean_int"
            val keyNotBooleanFloat get() = "n_boolean_float"
            val keyNotBooleanFloatRemainder get() = "n_boolean_float_remainder"
            val keyNotBooleanStringTrue get() = "n_boolean_string_true"
            val keyNotBooleanStringFalse get() = "n_boolean_string_false"
            val keyNotBooleanStringOom get() = "n_boolean_string_oom"
            val keyNotBooleanObject get() = "n_boolean_object"
            val keyNotBooleanArray get() = "n_boolean_array"
            val keyNotBooleanNull get() = "n_boolean_null"

            override val rawJson: String
                get() = """
                    {
                      "$keyBooleanTrue": true,
                      "$keyBooleanFalse": false,
                      "$keyNotBooleanInt": 1,
                      "$keyNotBooleanFloat": 1.0,
                      "$keyNotBooleanFloatRemainder": 1.25,
                      "$keyNotBooleanStringTrue": "true",
                      "$keyNotBooleanStringFalse": "false",
                      "$keyNotBooleanStringOom": "oom",
                      "$keyNotBooleanObject": {},
                      "$keyNotBooleanArray": [],
                      "$keyNotBooleanNull": null
                    }
                """.trimIndent()
        }

        val json = sample.json

        val inputs = listOf(
            // booleans
            BooleanInput(key = sample.keyBooleanTrue, boolean = true),
            BooleanInput(key = sample.keyBooleanFalse, boolean = false),

            // --------------------------------
            // non-booleans
            // --------------------------------
            // `1` is converted to boolean:true
            BooleanInput(key = sample.keyNotBooleanInt, booleanValue = true),

            // 1.0 is converted to 1, thus == true
            BooleanInput(key = sample.keyNotBooleanFloat, booleanValue = true),

            // only exact 1.0 is considered boolean true, all the rest are false
            BooleanInput(key = sample.keyNotBooleanFloatRemainder, booleanValue = false),

            // "true" is true
            // rest is false
            BooleanInput(key = sample.keyNotBooleanStringTrue, booleanValue = true),
            BooleanInput(key = sample.keyNotBooleanStringFalse, booleanValue = false),
            BooleanInput(key = sample.keyNotBooleanStringOom, booleanValue = false),

            BooleanInput(key = sample.keyNotBooleanObject, booleanValue = false),
            BooleanInput(key = sample.keyNotBooleanArray, booleanValue = false),
            BooleanInput(key = sample.keyNotBooleanNull, booleanValue = false),
        )

        for ((key, boolean, booleanValue) in inputs) {
            assertEquals(boolean, json[key].boolean, "boolean-$key")
            assertEquals(booleanValue, json[key].booleanValue, "booleanValue-$key")
        }
    }

    private class NumberInput(val key: String, block: NumberInput.() -> Unit = {}) {
        fun int(int: Int) = this.also {
            this.int = int
            this.intValue = int
        }

        fun intValue(intValue: Int) = this.also {
            this.int = null
            this.intValue = intValue
        }

        fun long(long: Long) = this.also {
            this.long = long
            this.longValue = long
        }

        fun longValue(longValue: Long) = this.also {
            this.long = null
            this.longValue = longValue
        }

        fun float(float: Float) = this.also {
            this.float = float
            this.floatValue = float
        }

        fun floatValue(floatValue: Float) = this.also {
            this.float = null
            this.floatValue = floatValue
        }

        fun double(double: Double) = this.also {
            this.double = double
            this.doubleValue = double
        }

        fun doubleValue(doubleValue: Double) = this.also {
            this.double = null
            this.doubleValue = doubleValue
        }

        fun notNumberValue() = this.also {
            this.int = null
            this.intValue = 0
            this.long = null
            this.longValue = 0L
            this.float = null
            this.floatValue = 0F
            this.double = null
            this.doubleValue = 0.0
        }

        var int: Int? = null
        var intValue: Int? = null

        var long: Long? = null
        var longValue: Long? = null

        var float: Float? = null
        var floatValue: Float? = null

        var double: Double? = null
        var doubleValue: Double? = null

        init {
            block(this)
        }
    }

    @Test
    fun JsonPrimitive_Number() {

        val sample = object : SampleJson {
            val keyInt get() = "key_int"
            val keyLong get() = "key_long"
            val keyFloat get() = "key_float"
            val keyDouble get() = "key_double"

            val keyBoolean get() = "key_boolean"
            val keyStringInt get() = "key_string_int"
            val keyStringFloat get() = "key_string_float"
            val keyObject get() = "key_object"
            val keyArray get() = "key_array"
            val keyNull get() = "key_null"

            override val rawJson: String
                get() = """
                    {
                      "$keyInt": 42,
                      "$keyLong": 100,
                      "$keyFloat": 4.9,
                      "$keyDouble": 6.7,
                      "$keyBoolean": true,
                      "$keyStringInt": "43",
                      "$keyStringFloat": "44.2",
                      "$keyObject": {},
                      "$keyArray": [],
                      "$keyNull": null
                    }
                """.trimIndent()
        }

        val json = sample.json

        val inputs = listOf(
            NumberInput(key = sample.keyInt) {
                int(42)
                long(42L)
                float(42F)
                double(42.0)
            },
            NumberInput(key = sample.keyLong) {
                int(100)
                long(100L)
                float(100F)
                double(100.0)
            },
            NumberInput(key = sample.keyFloat) {
                // floors
                int(4)
                long(4L)
                float(4.9F)
                double(4.9)
            },
            NumberInput(key = sample.keyDouble) {
                int(6)
                long(6L)
                float(6.7F)
                double(6.7)
            },
            NumberInput(key = sample.keyStringInt) {
                intValue(43)
                longValue(43L)
                floatValue(43F)
                doubleValue(43.0)
            },
            NumberInput(key = sample.keyStringFloat) {
                intValue(44)
                longValue(44L)
                floatValue(44.2F)
                doubleValue(44.2)
            },
            NumberInput(key = sample.keyObject) {
                notNumberValue()
            },
            NumberInput(key = sample.keyArray) {
                notNumberValue()
            },
            NumberInput(key = sample.keyNull) {
                notNumberValue()
            }
        )

        for (input in inputs) {
            val key = input.key

            assertEquals(input.int, json[key].int, "int-$key")
            assertEquals(input.intValue, json[key].intValue, "intValue-$key")

            assertEquals(input.long, json[key].long, "long-$key")
            assertEquals(input.longValue, json[key].longValue, "longValue-$key")

            assertEquals(input.float, json[key].float, "float-$key")
            assertEquals(input.floatValue, json[key].floatValue, "floatValue-$key")

            assertEquals(input.double, json[key].double, "double-$key")
            assertEquals(input.doubleValue, json[key].doubleValue, "doubleValue-$key")
        }
    }

    private data class StringInput(
        val key: String,
        val string: String? = null,
        val stringValue: String
    ) {
        constructor(key: String, string: String) : this(
            key = key,
            string = string,
            stringValue = string
        )
    }

    @Test
    fun JsonPrimitive_String() {

        val sample = object : SampleJson {
            val keyString get() = "k_string"
            val keyStringNotEmpty get() = "k_string_not_empty"
            val keyBoolean get() = "k_boolean"
            val keyInt get() = "k_int"
            val keyLong get() = "k_long"
            val keyFloat get() = "k_float"
            val keyDouble get() = "k_double"
            val keyObject get() = "k_object"
            val keyArray get() = "k_array"
            val keyNull get() = "k_null"

            override val rawJson: String
                get() = """
                {
                  "$keyString": "",
                  "$keyStringNotEmpty": "hey ho",
                  "$keyBoolean": true,
                  "$keyInt": 42,
                  "$keyLong": 2147483648,
                  "$keyFloat": 4.9,
                  "$keyDouble": 2.3,
                  "$keyObject": {},
                  "$keyArray": [],
                  "$keyNull": null
                }
            """.trimIndent()
        }

        val json = sample.json

        val inputs = listOf(
            StringInput(key = sample.keyString, string = ""),
            StringInput(key = sample.keyStringNotEmpty, string = "hey ho"),
            StringInput(key = sample.keyBoolean, stringValue = "true"),
            StringInput(key = sample.keyInt, stringValue = "42"),
            StringInput(key = sample.keyLong, stringValue = "2147483648"),
            StringInput(key = sample.keyFloat, stringValue = "4.9"),
            StringInput(key = sample.keyDouble, stringValue = "2.3"),
            StringInput(key = sample.keyObject, stringValue = ""),
            StringInput(key = sample.keyArray, stringValue = ""),
            StringInput(key = sample.keyNull, stringValue = ""),
        )

        for ((key, string, stringValue) in inputs) {
            assertEquals(string, json[key].string, "string-$key")
            assertEquals(stringValue, json[key].stringValue, "string-value-$key")
        }
    }

    //---------------------------------------------------
    //#endregion [JsonPrimitive]
    //---------------------------------------------------

    //---------------------------------------------------
    //#region [JsonArray]
    //---------------------------------------------------

    @Test
    fun JsonArray() {
        val sample = object : SampleJson {
            val keyNull = "key_null"
            val keyBool = "key_boolean"
            val keyNumberInt = "key_number_int"
            val keyNumberLong = "key_number_long"
            val keyNumberFloat = "key_number_float"
            val keyNumberDouble = "key_number_double"
            val keyString = "key_string"
            val keyObject = "{}"
            val keyArray = "[]"

            override val rawJson: String
                get() = """
                    {
                      "$keyNull": null,
                      "$keyBool": true,
                      "$keyNumberInt": 42,
                      "$keyNumberLong": 2147483648,
                      "$keyNumberFloat": 4.9,
                      "$keyNumberDouble": 5.1,
                      "$keyString": "",
                      "$keyObject": {},
                      "$keyArray": []
                    }
                """.trimIndent()
        }

        val json = sample.json

        data class Input(val key: String, val isArray: Boolean)

        val inputs = listOf(
            Input(key = sample.keyNull, isArray = false),
            Input(key = sample.keyBool, isArray = false),
            Input(key = sample.keyNumberInt, isArray = false),
            Input(key = sample.keyNumberLong, isArray = false),
            Input(key = sample.keyNumberFloat, isArray = false),
            Input(key = sample.keyNumberDouble, isArray = false),
            Input(key = sample.keyString, isArray = false),
            Input(key = sample.keyObject, isArray = false),
            Input(key = sample.keyArray, isArray = true),
        )

        for ((key, isArray) in inputs) {
            if (isArray) {
                assertEquals(emptyList(), json[key].array, "is-array-$key")
                assertEquals(emptyList(), json[key].arrayValue, "is-array-value-$key")
                assertEquals(emptyList(), json[key].jsonArray, "is-json-array-$key")
                assertEquals(emptyList(), json[key].jsonArrayValue, "is-json-array-value-$key")
            } else {
                assertNull(json[key].array, "null-$key")
                assertEquals(emptyList(), json[key].arrayValue, "not-array-value-$key")
            }
        }
    }

    //---------------------------------------------------
    //#endregion [JsonArray]
    //---------------------------------------------------

    //---------------------------------------------------
    //#region [JsonObject]
    //---------------------------------------------------

    @Test
    fun JsonObject_any() {
        val sample = object : SampleJson {
            val keyNull = "key_null"
            val keyBool = "key_boolean"
            val keyNumberInt = "key_number_int"
            val keyNumberLong = "key_number_long"
            val keyNumberFloat = "key_number_float"
            val keyNumberDouble = "key_number_double"
            val keyString = "key_string"
            val keyObject = "{}"
            val keyArray = "[]"

            override val rawJson: String
                get() = """
                    {
                      "$keyNull": null,
                      "$keyBool": true,
                      "$keyNumberInt": 42,
                      "$keyNumberLong": 2147483648,
                      "$keyNumberFloat": 4.9,
                      "$keyNumberDouble": 5.1,
                      "$keyString": "",
                      "$keyObject": {},
                      "$keyArray": []
                    }
                """.trimIndent()

            val keys = setOf(
                keyNull,
                keyBool,
                keyNumberInt,
                keyNumberLong,
                keyNumberFloat,
                keyNumberDouble,
                keyString,
                keyObject,
                keyArray,
            )
        }

        // does not contain anything
        val json = sample.json

        val notExistingKey = "whatever_42"

        for (key in sample.keys) {
            val el = json[key][notExistingKey]

            assertNull(el.element)
            assertEquals(false, el.exists())
            assertEquals(false, el.existsNotNull())

            assertNull(el.boolean)
            assertNull(el.int)
            assertNull(el.long)
            assertNull(el.float)
            assertNull(el.double)
            assertNull(el.string)
            assertNull(el.array)
            assertNull(el.jsonArray)
        }
    }

    @Test
    fun JsonObject_exists() {
        val sample = object : SampleJson {
            val keyNull = "some.object.that.equals.key_null"
            val keyBool = "that.this.key_boolean"
            val keyNumberInt = "some.yes.that.key_int"
            val keyNumberLong = "that.this.key_long"
            val keyNumberFloat = "some.object.this.key_float"
            val keyNumberDouble = "key_double"
            val keyString = "some.key_string"
            val keyObject = "some.object.yes.yes.no.key_object"
            val keyArray = "some.42.key_array"

            fun build(): String {
                // value can be:
                //  - map<String, Any> (which in turn can be map<string, any>, or actual value)
                //  - string (actual value)
                val root = mutableMapOf<String, Any>()

                fun el(path: String, value: String) {
                    val components = path.split(".")
                    if (components.size == 1) {
                        root[components[0]] = value
                    } else {
                        // last one is the key of teh value
                        val key = components.last()

                        val last = components
                            .dropLast(1)
                            .fold(root) { acc, value ->
                                acc.putIfAbsent(value, mutableMapOf<String, Any>())
                                @Suppress("UNCHECKED_CAST")
                                acc[value] as MutableMap<String, Any>
                            }
                        last[key] = value
                    }
                }

                el(keyNull, "null")
                el(keyBool, "true")
                el(keyNumberInt, "42")
                el(keyNumberLong, "9999999999")
                el(keyNumberFloat, "4.9")
                el(keyNumberDouble, "3.3")
                el(keyString, "\"okay\"")
                el(keyObject, "{}")
                el(keyArray, "[]")

                val builder = StringBuilder()

                fun render(value: Any) {
                    when (value) {
                        is Map<*, *> -> {
                            builder.append("{")
                            val length = builder.length
                            for ((key, value) in value) {

                                if (length != builder.length) {
                                    builder.append(",")
                                }

                                builder
                                    .append("\"")
                                    .append(key)
                                    .append("\"")
                                    .append(":")

                                render(value!!)
                            }
                            builder.append("}")
                        }

                        is String -> {
                            builder.append(value)
                        }

                        else -> {
                            error("Unexpected value:$value")
                        }
                    }
                }

                render(root)

                return builder.toString()
            }

            override val rawJson: String get() = build()
        }

        val json = sample.json

        data class Input(val path: String, val exists: Boolean, val existsNotNull: Boolean)

        val inputs = listOf(
            // existing
            Input(path = sample.keyNull, exists = true, existsNotNull = false),
            Input(path = sample.keyBool, exists = true, existsNotNull = true),
            Input(path = sample.keyNumberInt, exists = true, existsNotNull = true),
            Input(path = sample.keyNumberLong, exists = true, existsNotNull = true),
            Input(path = sample.keyNumberFloat, exists = true, existsNotNull = true),
            Input(path = sample.keyNumberDouble, exists = true, existsNotNull = true),
            Input(path = sample.keyString, exists = true, existsNotNull = true),
            Input(path = sample.keyObject, exists = true, existsNotNull = true),
            Input(path = sample.keyArray, exists = true, existsNotNull = true),

            // not existing
            Input(path = "not.existing.key_whatever", exists = false, existsNotNull = false),
            Input(path = "not.some", exists = false, existsNotNull = false),
            Input(path = "this.that.noop", exists = false, existsNotNull = false),
        )

        fun el(path: String): Json {
            return path.split(".")
                .fold(json) { a, v ->
                    a[v]
                }
        }

        for ((path, exists, existsNotNull) in inputs) {
            val el = el(path)
            assertEquals(exists, el.exists(), "exists-$path")
            assertEquals(existsNotNull, el.existsNotNull(), "existsNotNull-$path")
        }
    }

    //---------------------------------------------------
    //#endregion [JsonObject]
    //---------------------------------------------------

    @Test
    fun Json_boolean() {
        data class Input(
            val key: String,
            val value: Boolean?,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "bool_false",
                value = false,
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = true,
                create = { this[it] = true }
            ),
            Input(
                key = "int",
                value = null,
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = null,
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = null,
                create = { this[it] = 42.0F }
            ),
            Input(
                key = "double",
                value = null,
                create = { this[it] = 42.0 }
            ),
            Input(
                key = "string",
                value = null,
                create = { this[it] = "42" }
            ),
            Input(
                key = "object",
                value = null,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = null,
                create = { this[it] = implementation.JsonArray() }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            assertEquals(value, json[key].boolean, "key:$key json:$jsonObject")
        }
    }

    @Test
    fun Json_booleanValue() {
        data class Input(
            val key: String,
            val value: Boolean?,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "bool_false",
                value = false,
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = true,
                create = { this[it] = true }
            ),
            Input(
                key = "int",
                value = false,
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = false,
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = false,
                create = { this[it] = 42.0F }
            ),
            Input(
                key = "double",
                value = false,
                create = { this[it] = 42.0 }
            ),
            Input(
                key = "string",
                value = false,
                create = { this[it] = "42" }
            ),
            Input(
                key = "int_true",
                value = true,
                create = { this[it] = 1 }
            ),
            Input(
                key = "int_false",
                value = false,
                create = { this[it] = 0 }
            ),
            Input(
                key = "long_true",
                value = true,
                create = { this[it] = 1L }
            ),
            Input(
                key = "long_false",
                value = false,
                create = { this[it] = 0L }
            ),
            Input(
                key = "float_true",
                value = true,
                create = { this[it] = 1.0F }
            ),
            Input(
                key = "float_false",
                value = false,
                create = { this[it] = 0.0F }
            ),
            Input(
                key = "double_true",
                value = true,
                create = { this[it] = 1.0 }
            ),
            Input(
                key = "double_false",
                value = false,
                create = { this[it] = 0.0 }
            ),
            Input(
                key = "string_true",
                value = true,
                create = { this[it] = "true" }
            ),
            Input(
                key = "string_false",
                value = false,
                create = { this[it] = "false" }
            ),
            Input(
                key = "object",
                value = false,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = false,
                create = { this[it] = implementation.JsonArray() }
            ),
            Input(
                key = "null",
                value = false,
                create = { this[it] = null }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            assertEquals(value, json[key].booleanValue, "key:$key json:$jsonObject")
        }
    }

    @Test
    fun Json_int() {
        data class Input(
            val key: String,
            val value: Int?,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "int",
                value = 42,
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = 42,
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = 42,
                create = { this[it] = 42.0F }
            ),
            Input(
                key = "float_fraction",
                value = 42,
                create = { this[it] = 42.9F }
            ),
            Input(
                key = "double",
                value = 42,
                create = { this[it] = 42.0 }
            ),
            Input(
                key = "double_fraction",
                value = 42,
                create = { this[it] = 42.9 }
            ),
            Input(
                key = "bool_false",
                value = null,
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = null,
                create = { this[it] = true }
            ),
            Input(
                key = "string",
                value = null,
                create = { this[it] = "42" }
            ),
            Input(
                key = "object",
                value = null,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = null,
                create = { this[it] = implementation.JsonArray() }
            ),
            Input(
                key = "null",
                value = null,
                create = { this[it] = JsonNull }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            assertEquals(value, json[key].int, "key:$key json:$jsonObject")
        }
    }

    @Test
    fun Json_intValue() {
        data class Input(
            val key: String,
            val value: Int,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "int",
                value = 42,
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = 42,
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = 42,
                create = { this[it] = 42.0F }
            ),
            Input(
                key = "float_fraction",
                value = 42,
                create = { this[it] = 42.9F }
            ),
            Input(
                key = "double",
                value = 42,
                create = { this[it] = 42.0 }
            ),
            Input(
                key = "double_fraction",
                value = 42,
                create = { this[it] = 42.9 }
            ),
            Input(
                key = "string_int",
                value = 43,
                create = { this[it] = "43" }
            ),
            Input(
                key = "string_float",
                value = 44,
                create = { this[it] = "44.9" }
            ),
            Input(
                key = "bool_false",
                value = 0,
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = 0,
                create = { this[it] = true }
            ),
            Input(
                key = "object",
                value = 0,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = 0,
                create = { this[it] = implementation.JsonArray() }
            ),
            Input(
                key = "null",
                value = 0,
                create = { this[it] = JsonNull }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            assertEquals(value, json[key].intValue, "key:$key json:$jsonObject")
        }
    }

    @Test
    fun Json_long() {
        data class Input(
            val key: String,
            val value: Long?,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "int",
                value = 42L,
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = 42L,
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = 42L,
                create = { this[it] = 42.0F }
            ),
            Input(
                key = "float_fraction",
                value = 42L,
                create = { this[it] = 42.9F }
            ),
            Input(
                key = "double",
                value = 42L,
                create = { this[it] = 42.0 }
            ),
            Input(
                key = "double_fraction",
                value = 42L,
                create = { this[it] = 42.9 }
            ),
            Input(
                key = "bool_false",
                value = null,
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = null,
                create = { this[it] = true }
            ),
            Input(
                key = "string",
                value = null,
                create = { this[it] = "42" }
            ),
            Input(
                key = "object",
                value = null,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = null,
                create = { this[it] = implementation.JsonArray() }
            ),
            Input(
                key = "null",
                value = null,
                create = { this[it] = null }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            assertEquals(value, json[key].long, "key:$key json:$jsonObject")
        }
    }

    @Test
    fun Json_longValue() {
        data class Input(
            val key: String,
            val value: Long,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "int",
                value = 42L,
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = 42L,
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = 42L,
                create = { this[it] = 42.0F }
            ),
            Input(
                key = "float_fraction",
                value = 42L,
                create = { this[it] = 42.9F }
            ),
            Input(
                key = "double",
                value = 42L,
                create = { this[it] = 42.0 }
            ),
            Input(
                key = "double_fraction",
                value = 42L,
                create = { this[it] = 42.9 }
            ),
            Input(
                key = "string_int",
                value = 43L,
                create = { this[it] = "43" }
            ),
            Input(
                key = "string_float",
                value = 44L,
                create = { this[it] = "44.9" }
            ),
            Input(
                key = "bool_false",
                value = 0L,
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = 0L,
                create = { this[it] = true }
            ),
            Input(
                key = "object",
                value = 0L,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = 0L,
                create = { this[it] = implementation.JsonArray() }
            ),
            Input(
                key = "null",
                value = 0L,
                create = { this[it] = JsonNull }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            assertEquals(value, json[key].longValue, "key:$key json:$jsonObject")
        }
    }

    @Test
    fun Json_float() {
        data class Input(
            val key: String,
            val value: Float?,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "int",
                value = 42F,
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = 42F,
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = 42.9F,
                create = { this[it] = 42.9F }
            ),
            Input(
                key = "double",
                value = 42.9F,
                create = { this[it] = 42.9 }
            ),
            Input(
                key = "bool_false",
                value = null,
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = null,
                create = { this[it] = true }
            ),
            Input(
                key = "string",
                value = null,
                create = { this[it] = "42.9" }
            ),
            Input(
                key = "object",
                value = null,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = null,
                create = { this[it] = implementation.JsonArray() }
            ),
            Input(
                key = "null",
                value = null,
                create = { this[it] = null }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            val actual = json[key].float
            if (value != null && actual != null) {
                assertTrue(
                    abs(actual - value) < EPSILON,
                    "key:$key json:$jsonObject expected:$value actual:$actual"
                )
            } else {
                assertEquals(value, actual, "key:$key json:$jsonObject")
            }
        }
    }

    @Test
    fun Json_floatValue() {
        data class Input(
            val key: String,
            val value: Float,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "int",
                value = 42F,
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = 42F,
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = 42.9F,
                create = { this[it] = 42.9F }
            ),
            Input(
                key = "double",
                value = 42.9F,
                create = { this[it] = 42.9 }
            ),
            Input(
                key = "string_int",
                value = 43F,
                create = { this[it] = "43" }
            ),
            Input(
                key = "string_float",
                value = 44.9F,
                create = { this[it] = "44.9" }
            ),
            Input(
                key = "bool_false",
                value = 0F,
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = 0F,
                create = { this[it] = true }
            ),
            Input(
                key = "object",
                value = 0F,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = 0F,
                create = { this[it] = implementation.JsonArray() }
            ),
            Input(
                key = "null",
                value = 0F,
                create = { this[it] = JsonNull }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            val actual = json[key].floatValue
            assertTrue(
                abs(actual - value) < EPSILON,
                "key:$key json:$jsonObject expected:$value actual:$actual"
            )
        }
    }

    @Test
    fun Json_double() {
        data class Input(
            val key: String,
            val value: Double?,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "int",
                value = 42.0,
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = 42.0,
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = 42.9,
                create = { this[it] = 42.9F }
            ),
            Input(
                key = "double",
                value = 42.9,
                create = { this[it] = 42.9 }
            ),
            Input(
                key = "bool_false",
                value = null,
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = null,
                create = { this[it] = true }
            ),
            Input(
                key = "string",
                value = null,
                create = { this[it] = "42.9" }
            ),
            Input(
                key = "object",
                value = null,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = null,
                create = { this[it] = implementation.JsonArray() }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            val actual = json[key].double
            if (value != null && actual != null) {
                assertTrue(
                    abs(actual - value) < EPSILON,
                    "key:$key json:$jsonObject expected:$value actual:$actual"
                )
            } else {
                assertEquals(value, actual, "key:$key json:$jsonObject")
            }
        }
    }

    @Test
    fun Json_doubleValue() {
        data class Input(
            val key: String,
            val value: Double,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "int",
                value = 42.0,
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = 42.0,
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = 42.9,
                create = { this[it] = 42.9F }
            ),
            Input(
                key = "double",
                value = 42.9,
                create = { this[it] = 42.9 }
            ),
            Input(
                key = "string_int",
                value = 43.0,
                create = { this[it] = "43" }
            ),
            Input(
                key = "string_float",
                value = 44.9,
                create = { this[it] = "44.9" }
            ),
            Input(
                key = "bool_false",
                value = 0.0,
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = 0.0,
                create = { this[it] = true }
            ),
            Input(
                key = "object",
                value = 0.0,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = 0.0,
                create = { this[it] = implementation.JsonArray() }
            ),
            Input(
                key = "null",
                value = 0.0,
                create = { this[it] = JsonNull }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            val actual = json[key].doubleValue
            assertTrue(
                abs(actual - value) < EPSILON,
                "key:$key json:$jsonObject expected:$value actual:$actual"
            )
        }
    }

    @Test
    fun Json_string() {
        data class Input(
            val key: String,
            val value: String?,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "string",
                value = "value",
                create = { this[it] = "value" }
            ),
            Input(
                key = "string_empty",
                value = "",
                create = { this[it] = "" }
            ),
            Input(
                key = "int",
                value = null,
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = null,
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = null,
                create = { this[it] = 42.9F }
            ),
            Input(
                key = "double",
                value = null,
                create = { this[it] = 42.9 }
            ),
            Input(
                key = "bool_false",
                value = null,
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = null,
                create = { this[it] = true }
            ),
            Input(
                key = "object",
                value = null,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = null,
                create = { this[it] = implementation.JsonArray() }
            ),
            Input(
                key = "null",
                value = null,
                create = { this[it] = JsonNull }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            assertEquals(value, json[key].string, "key:$key json:$jsonObject")
        }
    }

    @Test
    fun Json_stringValue() {
        data class Input(
            val key: String,
            val value: String,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "string",
                value = "value",
                create = { this[it] = "value" }
            ),
            Input(
                key = "string_empty",
                value = "",
                create = { this[it] = "" }
            ),
            Input(
                key = "int",
                value = "42",
                create = { this[it] = 42 }
            ),
            Input(
                key = "long",
                value = "42",
                create = { this[it] = 42L }
            ),
            Input(
                key = "float",
                value = "42.9",
                create = { this[it] = 42.9F }
            ),
            Input(
                key = "double",
                value = "42.9",
                create = { this[it] = 42.9 }
            ),
            Input(
                key = "bool_false",
                value = "false",
                create = { this[it] = false }
            ),
            Input(
                key = "bool_true",
                value = "true",
                create = { this[it] = true }
            ),
            Input(
                key = "object",
                value = "",
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "array",
                value = "",
                create = { this[it] = implementation.JsonArray() }
            ),
            Input(
                key = "null",
                value = "",
                create = { this[it] = JsonNull }
            ),
        )

        for ((key, value, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            assertEquals(value, json[key].stringValue, "key:$key json:$jsonObject")
        }
    }

    @Test
    fun Json_array() {
        data class Input(
            val key: String,
            val expectedValues: List<String>?,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "array",
                expectedValues = listOf("one", "two"),
                create = {
                    val array = implementation.JsonArray().apply {
                        add(implementation.JsonPrimitive("one"))
                        add(implementation.JsonPrimitive("two"))
                    }
                    this[it] = array
                }
            ),
            Input(
                key = "empty_array",
                expectedValues = emptyList(),
                create = {
                    val array = implementation.JsonArray()
                    this[it] = array
                }
            ),
            Input(
                key = "int",
                expectedValues = null,
                create = { this[it] = 42 }
            ),
            Input(
                key = "bool",
                expectedValues = null,
                create = { this[it] = true }
            ),
            Input(
                key = "object",
                expectedValues = null,
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "null",
                expectedValues = null,
                create = { this[it] = JsonNull }
            ),
        )

        for ((key, expectedValues, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            val actual = json[key].array
            if (expectedValues == null) {
                assertEquals(null, actual, "key:$key json:$jsonObject")
            } else {
                assertNotNull(actual, "key:$key json:$jsonObject")
                assertEquals(expectedValues, actual.map { it.stringValue }, "key:$key json:$jsonObject")
            }
        }
    }

    @Test
    fun Json_arrayValue() {
        data class Input(
            val key: String,
            val expectedValues: List<String>,
            val create: JsonObject.(String) -> Unit
        )

        val inputs = listOf(
            Input(
                key = "array",
                expectedValues = listOf("one", "two"),
                create = {
                    val array = implementation.JsonArray().apply {
                        add(implementation.JsonPrimitive("one"))
                        add(implementation.JsonPrimitive("two"))
                    }
                    this[it] = array
                }
            ),
            Input(
                key = "empty_array",
                expectedValues = emptyList(),
                create = {
                    val array = implementation.JsonArray()
                    this[it] = array
                }
            ),
            Input(
                key = "int",
                expectedValues = emptyList(),
                create = { this[it] = 42 }
            ),
            Input(
                key = "bool",
                expectedValues = emptyList(),
                create = { this[it] = false }
            ),
            Input(
                key = "object",
                expectedValues = emptyList(),
                create = { this[it] = implementation.JsonObject() }
            ),
            Input(
                key = "null",
                expectedValues = emptyList(),
                create = { this[it] = JsonNull }
            ),
        )

        for ((key, expectedValues, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            create.invoke(jsonObject, key)

            val actual = json[key].arrayValue
            assertEquals(expectedValues, actual.map { it.stringValue }, "key:$key json:$jsonObject")
        }
    }

    @Test
    fun Json_jsonArray() {
        data class Input(
            val key: String,
            val create: JsonObject.(String) -> List<JsonElement?>?
        )

        val inputs = listOf(
            Input(
                key = "array",
                create = {
                    val array = implementation.JsonArray()
                    val elements = listOf<JsonElement?>(
                        implementation.JsonPrimitive("one"),
                        JsonNull,
                        implementation.JsonPrimitive("two")
                    )
                    elements.forEach { element -> array.add(element) }
                    this[it] = array
                    elements
                }
            ),
            Input(
                key = "empty_array",
                create = {
                    val array = implementation.JsonArray()
                    this[it] = array
                    emptyList()
                }
            ),
            Input(
                key = "int",
                create = {
                    this[it] = 42
                    null
                }
            ),
            Input(
                key = "bool",
                create = {
                    this[it] = true
                    null
                }
            ),
            Input(
                key = "object",
                create = {
                    this[it] = implementation.JsonObject()
                    null
                }
            ),
            Input(
                key = "null",
                create = {
                    this[it] = JsonNull
                    null
                }
            ),
        )

        for ((key, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            val expected = create.invoke(jsonObject, key)

            val actual = json[key].jsonArray
            if (expected == null) {
                assertEquals(null, actual, "key:$key json:$jsonObject")
            } else {
                assertNotNull(actual, "key:$key json:$jsonObject")
                assertEquals(expected.size, actual.size, "size-$key json:$jsonObject")
                expected.zip(actual).forEachIndexed { index, (exp, act) ->
                    assertEquals(exp, act, "key:$key index:$index json:$jsonObject")
                }
            }
        }
    }

    @Test
    fun Json_jsonArrayValue() {
        data class Input(
            val key: String,
            val create: JsonObject.(String) -> List<JsonElement>
        )

        val inputs = listOf(
            Input(
                key = "array",
                create = {
                    val array = implementation.JsonArray()
                    val elements = listOf<JsonElement>(
                        implementation.JsonPrimitive("one"),
                        JsonNull,
                        implementation.JsonPrimitive("two")
                    )
                    elements.forEach { element -> array.add(element) }
                    this[it] = array
                    elements
                }
            ),
            Input(
                key = "empty_array",
                create = {
                    val array = implementation.JsonArray()
                    this[it] = array
                    emptyList()
                }
            ),
            Input(
                key = "int",
                create = {
                    this[it] = 42
                    emptyList()
                }
            ),
            Input(
                key = "bool",
                create = {
                    this[it] = false
                    emptyList()
                }
            ),
            Input(
                key = "object",
                create = {
                    this[it] = implementation.JsonObject()
                    emptyList()
                }
            ),
            Input(
                key = "null",
                create = {
                    this[it] = JsonNull
                    emptyList()
                }
            ),
        )

        for ((key, create) in inputs) {
            val jsonObject = implementation.JsonObject()
            val json = Json(jsonObject)
            val expected = create.invoke(jsonObject, key)

            val actual = json[key].jsonArrayValue
            assertEquals(expected.size, actual.size, "size-$key json:$jsonObject")
            expected.zip(actual).forEachIndexed { index, (exp, act) ->
                assertEquals(exp, act, "key:$key index:$index json:$jsonObject")
            }
        }
    }

    @Test
    fun JsonImplementation_native() {
        val elements = listOf(
            implementation.JsonNull(),
            implementation.JsonObject(),
            implementation.JsonArray(),
            implementation.JsonPrimitive(42),
            implementation.JsonPrimitive(421L),
            implementation.JsonPrimitive(42.1F),
            implementation.JsonPrimitive(42.12),
            implementation.JsonPrimitive(true),
            implementation.JsonPrimitive("yes"),
        )

        for (element in elements) {
            val native = implementation.unwrap(element)
            val wrapped = implementation.of(native)
            assertEquals(
                expected = element,
                actual = wrapped,
                message = listOf(
                    "element" to element,
                    "native" to native,
                    "wrapped" to wrapped
                ).joinToString(separator = ", ") {
                    "${it.first}:${it.second}"
                }
            )
        }
    }

    @Test
    fun JsonImplementation_parse() {
        data class Input(
            val json: String,
            val expected: JsonElement
        )

        val inputs = listOf(
            Input(
                json = "null",
                expected = implementation.JsonNull()
            ),
            Input(
                json = "42",
                expected = implementation.JsonPrimitive(42)
            ),
            // hm, will it work? we do not do any additional checks
            //  so now it depends on the implementation
            Input(
                json = "421",
                expected = implementation.JsonPrimitive(421L)
            ),
            Input(
                json = "true",
                expected = implementation.JsonPrimitive(true)
            ),
            // this fails for gson, as they internally convert to doubleValue,
            //  which I assume looses some precision, which results in equals=false
//            Input(
//                json = "42.1",
//                expected = implementation.JsonPrimitive(42.1F)
//            ),
            Input(
                json = "42.12",
                expected = implementation.JsonPrimitive(42.12)
            ),
            Input(
                json = "\"a-string\"",
                expected = implementation.JsonPrimitive("a-string")
            ),
            Input(
                json = "[]",
                expected = implementation.JsonArray()
            ),
            Input(
                json = "[1, true, \"no\", null, {}]",
                expected = implementation.JsonArray().also {
                    it.add(implementation.JsonPrimitive(1))
                    it.add(implementation.JsonPrimitive(true))
                    it.add(implementation.JsonPrimitive("no"))
                    it.add(implementation.JsonNull())
                    it.add(implementation.JsonObject())
                }
            ),
            Input(
                json = "{}",
                expected = implementation.JsonObject()
            ),
            Input(
                json = "{ \"key\": 1 }",
                expected = implementation.JsonObject().also {
                    it.addProperty("key", 1)
                }
            )
        )

        for ((json, expected) in inputs) {
            val element = implementation.parse(json)
            assertEquals(expected, element, json)
        }
    }

    @Test
    fun JsonRequiredExtensions_new() {
        // check results match

        val implementation = implementation
        val jre = createJsonRequiredExtensions()

        val inputs = listOf<JsonFactory.() -> JsonElement>(
            { JsonObject() },
            { JsonArray() },
            { JsonNull() },
            { JsonPrimitive(42) },
            { JsonPrimitive(421L) },
            { JsonPrimitive(4212.7F) },
            { JsonPrimitive(4212.79) },
            { JsonPrimitive("4212.794") }
        )

        for (input in inputs) {
            val native = input(implementation)
            val extension = jre.JsonElement_Companion_new(factory = input)
            assertEquals(native, extension, input(implementation).toString())
        }
    }

    @Test
    fun JsomRequiredExtensions_parse() {
        val implementation = this.implementation
        val jsonRequiredExtensions = this.createJsonRequiredExtensions()

        data class Input(
            val element: JsonElement,
            val json: String
        )

        val inputs = listOf(
            Input(
                element = implementation.JsonNull(),
                json = "null"
            ),
            Input(
                element = implementation.JsonPrimitive(true),
                json = "true"
            ),
            Input(
                element = implementation.JsonPrimitive(42L),
                json = "42"
            ),
            Input(
                element = implementation.JsonPrimitive(42.9),
                json = "42.9"
            ),
            Input(
                element = implementation.JsonPrimitive("hello"),
                json = "\"hello\""
            ),
            Input(
                element = implementation.JsonArray(),
                json = "[]"
            ),
            Input(
                element = implementation.JsonArray().also {
                    it.add(implementation.JsonPrimitive(1))
                    it.add(implementation.JsonPrimitive(true))
                    it.add(implementation.JsonPrimitive("hey"))
                    it.add(implementation.JsonArray())
                },
                json = "[1, true, \"hey\", []]"
            ),
            Input(
                element = implementation.JsonObject(),
                json = "{}"
            ),
            Input(
                element = implementation.JsonObject().also {
                    it.addProperty("int", 42)
                    it.addProperty("bool", true)
                    it.addProperty("string", "oops")
                    it.add("nested", implementation.JsonObject())
                },
                json = listOf(
                    "int" to "42",
                    "bool" to "true",
                    "string" to "\"oops\"",
                    "nested" to "{}"
                ).joinToString(
                    separator = ",",
                    prefix = "{",
                    postfix = "}"
                ) {
                    "${it.first}:${it.second}"
                }
            )
        )

        for ((expected, jsonString) in inputs) {
            val json = jsonRequiredExtensions.Json_Companion_parse(string = jsonString)
            assertEquals(expected, json.element, jsonString)
        }
    }
}
