/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/23 14:17:51
 *
 * MiraiPlugins/MiraiBootstrap/JsonHelper.kt
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")

package cn.mcres.karlatemp.mirai

import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import kotlinx.io.errors.IOException
import java.io.StringWriter
import java.io.Writer

@DslMarker
@ForDsl
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class JsonBuilderDsl

@JsonBuilderDsl
class JsonBuilder(val writer: JsonWriter, initializer: JsonBuilder.(JsonBuilder) -> Unit) {

    init {
        initializer(this, this)
    }

    @Throws(IOException::class)
    @JsonBuilderDsl
    fun value(str: String) {
        writer.value(str)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    fun value(value: Double) {
        writer.value(value)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    fun value(value: Boolean) {
        writer.value(value)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    fun value(value: Number) {
        writer.value(value)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    fun json(json: String) {
        writer.jsonValue(json)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    fun name(name: String) {
        writer.name(name)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    fun array(initializer: JsonBuilder.(JsonBuilder) -> Unit) {
        writer.beginArray()
        initializer(this, this)
        writer.endArray()
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    fun obj(initializer: JsonBuilder.(JsonBuilder) -> Unit) {
        writer.beginObject()
        initializer(this, this)
        writer.endObject()
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    infix fun String.value(str: String) {
        name(this); this@JsonBuilder.value(str)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    infix fun String.value(value: Double) {
        name(this); this@JsonBuilder.value(value)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    infix fun String.value(value: Boolean) {
        name(this); this@JsonBuilder.value(value)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    infix fun String.value(value: Number) {
        name(this); this@JsonBuilder.value(value)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    infix fun String.json(json: String) {
        name(this); this@JsonBuilder.json(json)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    infix fun String.array(initializer: JsonBuilder.(JsonBuilder) -> Unit) {
        name(this); this@JsonBuilder.array(initializer)
    }

    @JsonBuilderDsl
    @Throws(IOException::class)
    infix fun String.obj(initializer: JsonBuilder.(JsonBuilder) -> Unit) {
        name(this); this@JsonBuilder.obj(initializer)
    }
}

fun <T : Writer> json(
        writer: T,
        isHtmlSafe: Boolean = false,
        isLenient: Boolean = false,
        serializeNulls: Boolean = false,
        indent: String = "",
        petty: Boolean = false,
        initializer: JsonBuilder.(JsonBuilder) -> Unit
): T {
    val jsonWriter = JsonWriter(writer)
    jsonWriter.isHtmlSafe = isHtmlSafe
    jsonWriter.isLenient = isLenient
    jsonWriter.serializeNulls = serializeNulls

    if (petty) jsonWriter.setIndent("  ")
    else jsonWriter.setIndent(indent)

    JsonBuilder(jsonWriter, initializer)
    return writer
}

fun json(
        isHtmlSafe: Boolean = false,
        isLenient: Boolean = false,
        serializeNulls: Boolean = false,
        indent: String = "",
        petty: Boolean = false,
        initializer: JsonBuilder.(JsonBuilder) -> Unit
): String = json(StringWriter(), isHtmlSafe, isLenient, serializeNulls, indent, petty, initializer).toString()

val globalGson = Gson()

inline fun Any.toJson(): String = globalGson.toJson(this)

inline fun Any.toPettyJson(): String {
    val stringWriter = StringWriter()
    val writer = JsonWriter(stringWriter)
    writer.isHtmlSafe = false
    writer.serializeNulls = false
    writer.setIndent("  ")
    globalGson.toJson(this, this.javaClass, writer)
    writer.close()
    return stringWriter.toString()
}
