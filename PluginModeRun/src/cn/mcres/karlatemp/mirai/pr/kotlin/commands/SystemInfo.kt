/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/23 15:25:22
 *
 * MiraiPlugins/PluginModeRun/SystemInfo.kt
 */

package cn.mcres.karlatemp.mirai.pr.kotlin.commands

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken
import cn.mcres.karlatemp.mirai.command.KCAlias
import cn.mcres.karlatemp.mirai.command.KCommand
import cn.mcres.karlatemp.mirai.command.KotlinCommand
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.toMessage
import java.io.File
import java.lang.management.ManagementFactory
import java.time.Instant
import java.time.ZoneId
import java.util.*

@KCommand("about")
@KCAlias("plugins")
object SystemInfo : KotlinCommand() {
    private val manager = ManagementFactory.getRuntimeMXBean()
    private val regex = ('\\' + File.pathSeparator).toRegex()
    val miraiTester = ".*/mirai-(.*)-([0-9._]+(|-[0-9A-Za-z_]+))\\.jar$".toRegex()
    override suspend fun invoke0(contact: Contact, sender: User, packet: MessageEvent, args: LinkedList<ArgumentToken>) {
        val runtime = manager
        val builder = StringBuilder()
        builder.append("墨雨橙 Power❤by Mirai.\n")
        val path = runtime.classPath
        val split = path.split(regex).map { it.replace('\\', '/') }
        for (classpath in split) {
            val result = miraiTester.matchEntire(classpath) ?: continue
            builder.append("${result.groupValues[1]}: ${result.groupValues[2]}\n")
        }
        builder.append("Kotlin: ${KotlinVersion.CURRENT}\n")
        builder.append("Working dir: ${File(".").absolutePath}\n")
        builder.append("\n${runtime.vmName} ${runtime.vmVendor} ${runtime.vmVersion}\n")
        builder.append("${runtime.specName} ${runtime.specVendor} ${runtime.specVersion}")
        contact.sendMessage(builder.toString().toMessage())
    }
}