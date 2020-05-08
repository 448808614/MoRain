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
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.queryUrl
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicReference
import java.util.jar.JarFile
import java.util.logging.Level
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible

@DslMarker
annotation class ForDsl

inline fun <reified E : Event> handlers(): HandlerList<E> {
    return E::class.handlers()
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
inline fun <T> Class<*>.extends(oth: Class<T>, then: Class<out T>.(Class<out T>) -> Unit): Boolean {
    return if (oth.isAssignableFrom(this)) {
        val `out` = this as Class<out T>
        then(`out`, `out`)
        true
    } else false
}

inline fun <reified E : ZipFile> E.forEach(action: E.(ZipEntry) -> Unit) {
    val iterator = entries()
    while (iterator.hasMoreElements()) action(this, iterator.nextElement())
}


@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <E : Event> KClass<E>.handlers(): HandlerList<E> {
    fun KClass<*>.findProperty() = members.filter { it.name == "handlers" }.filter {
        val result = it.returnType.classifier
        if (result is KClass<*>) {
            return@filter result.isSubclassOf(HandlerList::class)
        }
        false
    }.takeIf { it.size == 1 }?.getOrNull(0)

    fun KCallable<*>.read(instance: Any?): HandlerList<E> {
        this.isAccessible = true
        return when (this) {
            is KProperty0 -> {
                get() as HandlerList<E>
            }
            is KProperty1<*, *> -> {
                (this as KProperty1<Any?, HandlerList<E>>).get(instance)
            }
            else -> throw IllegalArgumentException("Find property $this but unknown how to get value")
        }
    }

    val property = findProperty()
    if (property != null) {
        return property.read(objectInstance)
    }
    val comp = companionObject ?: throw IllegalStateException("No property and companion found.")
    val cp = comp.findProperty() ?: throw IllegalStateException("No property and companion property found.")
    return cp.read(companionObjectInstance)
}

inline fun <reified E : Event> on(crossinline handler: E.(E) -> Unit) {
    handlers<E>().register { w -> w.handler(w) }
}

inline fun permissible(): Permissible = PermissionManager.PERMISSIBLE_THREAD_LOCAL.get()
inline fun permissible(override: Permissible) = PermissionManager.PERMISSIBLE_THREAD_LOCAL.set(override)

inline fun String.checkPermission() = permissible().hasPermission(this)

inline fun String.logger(): Logger = Logger.getLogger(this)
inline fun Logger.all(): Logger = also { level = Level.ALL }

inline fun Logger.export(throwable: Throwable,
                         message: String? = "An unexpected error occurred!",
                         level: Level = Level.SEVERE) {
    log(level, message, throwable)
}

inline val bot: Bot
    get() = Bootstrap.bot

val globalRandom = SecureRandom()

fun <T : Contact> ContactList<T>.random(): T {
    val s = size
    if (s == 0) throw NoSuchElementException()
    return iterator().get((Math.random() * s).toInt())
}

fun <T> Iterator<T>.get(counter: Int): T {
    repeat(counter) {
        next()
    }
    return next()
}

@ForDsl
suspend inline infix fun Message.sendTo(contact: Contact) {
    contact.sendMessage(this)
}

inline operator fun String.get(start: Int, end: Int): String = this.substring(start, end)

val standardLuckyUsers = listOf(
        LuckyUser(3279826484, "果粒酱"),
        LuckyUser(1838115958, "小星"),
        LuckyUser(602723113, "莫老"),
        LuckyUser(1810348213, "ksqeib"),
        LuckyUser(3390038158, "果粒橙"),
        LuckyUser(2191023046, "贺兰星辰"),
        LuckyUser(3053434956, "内鬼"),
        LuckyUser(3447124995, "墨雨橙")
)

fun Contact.lucky(): LuckyUser {
    if (globalRandom.nextDouble() > 0.1) {
        if (this is Group) {
            return this.members.random().let { LuckyUser(it.id, it.nameCardOrNick) }
        }
    }
    return standardLuckyUsers.random()
}

data class LuckyUser(val qq: Long, val name: String) {
    @ForDsl
    infix fun asNode(builder: ForwardMessageBuilder): ForwardMessageBuilder.BuilderNode {
        with(builder) {
            return qq named name
        }
    }
}

fun Image.queryUrlBlocking(): String {
    return kotlinx.coroutines.runBlocking {
        queryUrl()
    }
}

operator fun <V> AtomicReference<V>.setValue(ignored: Any, ignored0: kotlin.reflect.KProperty<*>, value: V) {
    set(value)
}

operator fun <V> AtomicReference<V>.getValue(ignored0: Any, ignored: kotlin.reflect.KProperty<*>): V = get()

