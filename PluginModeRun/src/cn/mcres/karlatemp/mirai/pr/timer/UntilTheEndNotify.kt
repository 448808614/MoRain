/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/26 23:58:12
 *
 * MiraiPlugins/PluginModeRun/UntilTheEndNotify.kt
 */

package cn.mcres.karlatemp.mirai.pr.timer

import cn.mcres.karlatemp.mirai.*
import cn.mcres.karlatemp.mirai.pr.AutoInitializer
import cn.mcres.karlatemp.mxlib.tools.URLEncoder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.message.data.toMessage
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.core5.concurrent.FutureCallback
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.logging.Level
import kotlin.NoSuchElementException

object UntilTheEndNotify : AutoInitializer {
    val update = "https://untiltheend.coding.net/p/UntilTheEnd/d/UntilTheEnd/git/raw/master/Update.txt"
    val version = "https://untiltheend.coding.net/p/UntilTheEnd/d/UntilTheEnd/git/raw/master/UTEversion.txt"

    private const val BEGIN = "--------------------------"

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
            postVersionUpdated(v, ov)
            sys_ver = v
        }

    private fun postVersionUpdated(latest: String, current: String) {
        Http.client.execute(SimpleHttpRequest.copy(HttpGet(update)), object : FutureCallback<SimpleHttpResponse> {
            override fun cancelled() {
                // Retry
                sys_ver = current
            }

            override fun completed(p0: SimpleHttpResponse) {
                if (p0.code != 200) {
                    sys_ver = current
                    return
                }
                val iterator = String(p0.bodyBytes, Charsets.UTF_8).let { lines ->
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
                }.joinToString("\n").toMessage().also {
                    AsyncExecKt.newScope.launch { it sendTo bot.getGroup(1051331429L) }
                }
            }

            override fun failed(p0: Exception) {
                sys_ver = current
                "UntilTheEnd Notify".logger().log(Level.SEVERE, "Failed to update version.", p0)
            }

        })
    }

    fun updateVersion() {
        Http.client.execute(SimpleHttpRequest.copy(HttpGet(version)), object : FutureCallback<SimpleHttpResponse> {
            override fun cancelled() {
            }

            override fun completed(p0: SimpleHttpResponse) {
                systemVersion = p0.bodyText
            }

            override fun failed(p0: Exception?) {
            }

        })
    }

    override fun initialize() {
        "UntilTheEnd Notify".logger().apply {
            info("Initialize Notify")
            info("Data file: $file")
        }
        AsyncExecKt.newScope.launch {
            while (true) {
                delay(1000L * 60)
                updateVersion()
            }
        }
    }
}