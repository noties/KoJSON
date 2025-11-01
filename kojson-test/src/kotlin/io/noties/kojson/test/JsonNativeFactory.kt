package io.noties.kojson.test

interface JsonNativeFactory<T : Any> {
    fun `null`(): T

    fun primitive(value: Boolean): T
    fun primitive(value: Int): T
    fun primitive(value: Long): T
    fun primitive(value: Float): T
    fun primitive(value: Double): T
    fun primitive(value: String): T

    fun `object`(): T

    fun array(): T

    fun parse(json: String): T
}
