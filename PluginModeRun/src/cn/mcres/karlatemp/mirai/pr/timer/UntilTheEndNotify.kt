/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/26 23:58:12
 *
 * MiraiPlugins/PluginModeRun/UntilTheEndNotify.kt
 */

package cn.mcres.karlatemp.mirai.pr.timer

import cn.mcres.karlatemp.mirai.*
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken
import cn.mcres.karlatemp.mirai.command.KCommand
import cn.mcres.karlatemp.mirai.command.KotlinCommand
import cn.mcres.karlatemp.mirai.pr.AutoInitializer
import cn.mcres.karlatemp.mirai.pr.SecurityI
import cn.mcres.karlatemp.mxlib.tools.URLEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.toMessage
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.core5.concurrent.FutureCallback
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import kotlin.NoSuchElementException

object UntilTheEndNotify : AutoInitializer {
    val update = "https://untiltheend.coding.net/p/UntilTheEnd/d/UntilTheEnd/git/raw/master/Update.txt"
    val version = "https://untiltheend.coding.net/p/UntilTheEnd/d/UntilTheEnd/git/raw/master/UTEversion.txt"
    val scope = AsyncExecKt.newScope
    var disabled = false
    const val BEGIN = "--------------------------"

    val logger = "UntilTheEnd Notify".logger()

    private val file = File("data/ute.txt").also {
        it.parentFile?.apply {
            if (!exists()) mkdirs()
        }
        if (!it.isFile) it.createNewFile()
    }
    private var sys_ver = file.readText().trim()
    var systemVersion: String
        get() = sys_ver
        set(value) {
            val v = value.trim()
            val ov = sys_ver
            if (v == ov) return
            file.writeText(v)
            sys_ver = v
        }
    val callbacks = mutableListOf<suspend CoroutineScope.(String) -> Unit>()

    private fun reconnect() {
        if (disabled) return
        scope.launch {
            delay(1000L * 60)
            if (disabled) return@launch
            callUpdate()
        }
    }

    private fun testFileExists(version: String) {
        // https://gitee.com/api/v5/repos/Karlatemp-bot/UntilTheEndReleases/contents/releases/UntilTheEnd%20v5.7.2.5-Release.jar?access_token={SEC}&ref=master
        val uri = "https://gitee.com/api/v5/repos/Karlatemp-bot/UntilTheEndReleases/contents/releases/UntilTheEnd%20v$version.jar?access_token=${
        SecurityI.security["gitee-token"]}&ref=master"
        Http.client.execute(SimpleHttpRequest.copy(HttpGet(uri)).also {
            it.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
        }, object : FutureCallback<SimpleHttpResponse> {
            override fun cancelled() = reconnect()

            override fun failed(p0: Exception) {
                logger.log(Level.SEVERE, "Failed to check file.", p0)
                reconnect()
            }

            override fun completed(p0: SimpleHttpResponse) {
                if (p0.code == 200) {
                    postVersionUpdated(version)
                } else {
                    reconnect()
                    logger.fine("It it not work.")
                    logger.fine(p0.bodyText)
                }
            }
        })
    }

    private fun postVersionUpdated(latest: String) {
        Http.client.execute(SimpleHttpRequest.copy(HttpGet(update)), object : FutureCallback<SimpleHttpResponse> {
            override fun cancelled() = reconnect()

            override fun completed(p0: SimpleHttpResponse) {
                if (p0.code != 200) {
                    scope.launch {
                        delay(1000L * 60)
                        callUpdate()
                    }
                    return
                }
                val iterator = String(p0.bodyBytes, Charsets.UTF_8).also {
                    UntilTheEndCommand.updateMessage = it
                }.let { lines ->
                    var starter = 0
                    object : Iterator<String> {
                        override fun hasNext(): Boolean = starter <= lines.length

                        override fun next(): String {
                            if (starter <= lines.length) {
                                val next = lines.indexOf('\n', starter)
                                return if (next == -1) {
                                    starter = lines.length
                                    lines.substring(starter)
                                } else {
                                    val result = lines.substring(starter, next)
                                    starter = next + 1
                                    result
                                }
                            }
                            throw NoSuchElementException()
                        }
                    }
                }
                while (iterator.hasNext()) {
                    if (iterator.next().startsWith(BEGIN)) break
                }
                LinkedList<String>().also {
                    while (iterator.hasNext()) {
                        val next = iterator.next()
                        if (next.startsWith(BEGIN)) break
                        it.add(next)
                    }
                }.also {
                    val urlEnc = URLEncoder.encode(latest, Charsets.UTF_8)
                    // https://gitee.com/Karlatemp-bot/UntilTheEndReleases/blob/master/releases/UntilTheEnd%20v5.7.2.4-Release.jar
                    // https://github.com/UntilTheEndDev/UntilTheEndReleases/blob/master/shadow/until-the-end/UntilTheEnd%20v5.7.2.4-Release.jar
                    it.add("============")
                    it.add("https://gitee.com/Karlatemp-bot/UntilTheEndReleases/blob/master/releases/UntilTheEnd%20v$urlEnc.jar")
                    it.add("https://github.com/UntilTheEndDev/UntilTheEndReleases/blob/master/shadow/until-the-end/UntilTheEnd%20v$urlEnc.jar")
                }.joinToString("\n").also { updateMsg ->
                    callbacks.forEach {
                        scope.launch {
                            it.invoke(this, updateMsg)
                        }
                    }
                    systemVersion = latest
                    scope.launch {
                        delay(1000L * 60)
                        callUpdate()
                    }
                    reconnect()
                }
            }

            override fun failed(p0: Exception) {
                logger.log(Level.SEVERE, "Failed to update version.", p0)
                reconnect()
            }

        })
    }

    fun callUpdate() {
        Http.client.execute(SimpleHttpRequest.copy(HttpGet(version)), object : FutureCallback<SimpleHttpResponse> {
            override fun cancelled() {
                reconnect()
            }

            override fun completed(p0: SimpleHttpResponse) {
                val latest = p0.bodyText.trim()
                // logger.fine("System ver $systemVersion, latest version $latest")
                if (systemVersion != latest) {
                    testFileExists(latest)
                } else reconnect()
            }

            override fun failed(p0: Exception) {
                logger.log(Level.SEVERE, "Failed to get latest version.", p0)
                reconnect()
            }

        })
    }

    override fun initialize() {
        callbacks.add {
            it.toMessage() sendTo bot.getGroup(1051331429L)
        }
        logger.all().fine("Start up")
        reconnect()
    }
}

@KCommand("ute")
object UntilTheEndCommand : KotlinCommand() {
    private val updateMessageFile = File("data/uteUpdate.txt")
    var versions by AtomicReference<LinkedList<String>>()
    var updates by AtomicReference<Map<String, String>>()
    var updateMessage = updateMessageFile.readText(Charsets.UTF_8).trim()
        set(v) {
            val value = v.trim()
            updateMessageFile.writeText(value, Charsets.UTF_8)
            field = value
            update()
        }
    private val pattern = "(\\r|)\\n".toRegex()
    private fun update() {
        val list = LinkedList(updateMessage.split(pattern))
        list.removeFirst()
        list.removeFirst()
        val versions = LinkedList<String>()
        val updates = mutableMapOf<String, String>()

        val buffer = LinkedList<String>()

        fun updateBuffer() {
            val version = buffer.poll() ?: error("Version not found!")
            val updateMsg = buffer.joinToString("\n")
            buffer.clear()
            versions.add(version)
            updates[version] = updateMsg
        }

        list.forEach { line ->
            if (line.startsWith(UntilTheEndNotify.BEGIN)) {
                updateBuffer()
                return@forEach
            }
            buffer.add(line)
        }
        updateBuffer()

        this.updates = updates
        this.versions = versions
    }

    init {
        update()
    }

    override suspend fun invoke0(contact: Contact, sender: User, packet: MessageEvent, args: LinkedList<ArgumentToken>) {
        if (args.isEmpty()) {
            // Latest
            return ("Latest version: ${UntilTheEndNotify.systemVersion}\n" + updates[UntilTheEndNotify.systemVersion])
                    .toMessage() sendTo contact
        }
        println(args)
        val type = args.poll().asString
        println(type)
        val versions = versions
        val updates = updates
        if (type == "all") {
            buildForwardMessage(contact) {
                fun me() = 3279826484L named "果粒酱~酱~"
                versions.forEach {
                    me() says "$it\n${updates[it]}"
                }
            } sendTo contact
        } else {
            val index = versions.indexOf(type)
            if (index == -1) {
                "Version $type not found".toMessage() sendTo contact
            } else {
                buildForwardMessage(contact) {
                    fun me() = 3279826484L named "果粒酱~酱~"
                    LinkedList(versions.subList(0, index + 1)).descendingIterator().forEach {
                        me() says "$it\n${updates[it]}"
                    }
                } sendTo contact
            }
        }
    }
}
