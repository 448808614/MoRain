/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/05/16 13:06:30
 *
 * MiraiPlugins/PluginModeRun/QGroupHitokoto.kt
 */

package cn.mcres.karlatemp.mirai.pr.kotlin

import cn.mcres.karlatemp.mirai.*
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken
import cn.mcres.karlatemp.mirai.command.KCommand
import cn.mcres.karlatemp.mirai.command.KotlinCommand
import cn.mcres.karlatemp.mirai.event.MessageSendEvent
import cn.mcres.karlatemp.mirai.pr.AutoInitializer
import cn.mcres.karlatemp.mirai.pr.CoreDisableEvent
import cn.mcres.karlatemp.mxlib.util.RAFOutputStream
import com.google.gson.JsonParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import kotlin.collections.HashSet

object QGroupHitokoto : AutoInitializer {
    @KCommand("test551")
    object KCmd : KotlinCommand() {
        override suspend fun invoke0(contact: Contact, sender: User, packet: MessageEvent, args: LinkedList<ArgumentToken>) {
            contact.sendMessage(data.toString())
        }
    }

    private val data = ConcurrentHashMap<Long, ConcurrentHashMap<String, HashSet<String>>>()
    private val data_get = Function<Long, ConcurrentHashMap<String, HashSet<String>>> { ConcurrentHashMap() }
    private val data_get_get = Function<String, HashSet<String>> { HashSet() }

    // 『希望各位程序员能保持当初第一次输出hello world的兴奋，每条代码都有它存在的意义，不要被社会打倒。指不定哪天你就改变世界了。』 - 「一名苦逼程序员」
    private val regex = """『(.+)』 - 「(.+)」""".toRegex()
    private val store = File("data/q-hitokoto.json")
    fun store() {
        RAFOutputStream(RandomAccessFile(store.also { it.parentFile.mkdirs() }, "rw")).use {
            json(writer = OutputStreamWriter(it, Charsets.UTF_8), isHtmlSafe = false, petty = true) {
                obj {
                    data.forEach { (t: Long, u: ConcurrentHashMap<String, HashSet<String>>) ->
                        t.toString() obj {
                            u.forEach { (k: String, v: HashSet<String>) ->
                                k array {
                                    v.forEach { vvv ->
                                        value(vvv)
                                    }
                                }
                            }
                        }
                    }
                }
            }.close()
        }
    }

    fun reload() {
        data.clear()
        kotlin.runCatching {
            store.takeIf { it.isFile }?.apply {
                val obj = InputStreamReader(FileInputStream(this), Charsets.UTF_8).use {
                    JsonParser.parseReader(it).asJsonObject
                }
                obj.keySet().forEach {
                    val map = ConcurrentHashMap<String, HashSet<String>>()
                    data[it.toLong()] = map
                    obj[it].asJsonObject.apply {
                        keySet().forEach { value ->
                            map[value] = this.getAsJsonArray(value).let { array ->
                                val set = HashSet<String>()
                                array.forEach { element -> set.add(element.asString) }
                                set
                            }
                        }
                    }
                }
            }
        }.onFailure {
            "QGroupHitokoto".logger().export(it)
        }
    }

    override fun initialize() {
        reload()
        on<MessageSendEvent> {
            with(event as? GroupMessageEvent ?: return@on) {
                regex.find(this.message.contentToString())?.let {
                    println(it.groupValues)
                    data.computeIfAbsent(event.sender.id, data_get)
                            .computeIfAbsent(it.groupValues[2], data_get_get)
                            .add(it.groupValues[1])
                }
            }
        }
        on<CoreDisableEvent> {
            store()
        }
        AsyncExecKt.newScope.launch {
            while (true) {
                delay(5000)
                store()
            }
        }
    }
}