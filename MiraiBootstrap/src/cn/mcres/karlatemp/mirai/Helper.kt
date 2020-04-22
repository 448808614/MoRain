/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/20 17:05:57
 *
 * MiraiPlugins/MiraiBootstrap/Events.kt
 */

@file:Suppress("NOTHING_TO_INLINE")

package cn.mcres.karlatemp.mirai

import cn.mcres.karlatemp.mirai.permission.Permissible
import cn.mcres.karlatemp.mirai.permission.PermissionManager
import cn.mcres.karlatemp.mirai.plugin.PluginLoaderManager
import cn.mcres.karlatemp.mxlib.event.Event
import cn.mcres.karlatemp.mxlib.event.HandlerList
import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import net.mamoe.mirai.Bot
import java.io.StringWriter
import java.util.jar.JarFile
import java.util.logging.Level
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


inline fun <reified E : Event> handlers(): HandlerList<E> {
    return E::class.java.handlers()
}

inline fun <reified E> E.invoke(vararg invokers: E.(E) -> Unit): E {
    for (invoker in invokers) invoker.invoke(this, this)
    return this
}

inline val jar: JarFile get() = PluginLoaderManager.scanning.get()

inline val ZipEntry.isClass: Boolean
    get() = !isDirectory && !name.startsWith("META-INF") && name.endsWith(".class")

inline fun ZipEntry.loadClass(): Class<*> {
    val name = this.name
    return Class.forName(name.replace('/', '.').substring(0, name.length - 6))
}

inline val <T> Class<T>.instance: T
    get() = PluginLoaderManager.getInstance(this)

@Suppress("UNCHECKED_CAST")
inline fun <T> Class<*>.extends(oth: Class<T>, then: Class<out T>.(Class<out T>) -> Unit): Boolean =
        if (oth.isAssignableFrom(this)) {
            val `out` = this as Class<out T>
            then(`out`, `out`)
            true
        } else false


inline fun <reified E : ZipFile> E.forEach(action: E.(ZipEntry) -> Unit) {
    val iterator = entries()
    while (iterator.hasMoreElements()) action(this, iterator.nextElement())
}


@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <E : Event> Class<E>.handlers(): HandlerList<E> {
    return runCatching { this.getField("handlers") }.getOrElse { _ ->
        var curr: Class<*>? = this
        do {
            val c = curr!!
            if (!Event::class.java.isAssignableFrom(c)) break
            try {
                val field = c.getDeclaredField("handlers")
                field.isAccessible = true
                return@getOrElse field
            } catch (any: Throwable) {
            }
            curr = curr.superclass
        } while (curr != null)
        throw NoSuchFieldException()
    }.get(null) as HandlerList<E>
}

inline fun <reified E : Event> on(crossinline handler: E.(E) -> Unit) {
    handlers<E>().register { w -> w.handler(w) }
}

inline fun permissible(): Permissible = PermissionManager.PERMISSIBLE_THREAD_LOCAL.get()

inline fun String.checkPermission() = permissible().hasPermission(this)

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

inline fun String.logger(): Logger = Logger.getLogger(this)

inline fun Logger.export(throwable: Throwable,
                         message: String? = "An unexpected error occurred!",
                         level: Level = Level.SEVERE) {
    log(level, message, throwable)
}

inline val bot: Bot
    get() = Bootstrap.bot
