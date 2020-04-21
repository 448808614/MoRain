/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/21 19:08:39
 *
 * MiraiPlugins/PluginModeRun/SystemInfo.kt
 */

package cn.mcres.karlatemp.mirai.pr.kotlin

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken
import cn.mcres.karlatemp.mirai.command.KCommand
import cn.mcres.karlatemp.mirai.command.KotlinCommand
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.toMessage
import java.io.File
import java.util.*

@KCommand("about")
object SystemInfo : KotlinCommand() {
    private val manager = java.lang.management.ManagementFactory.getRuntimeMXBean()
    private val regex = ('\\' + File.pathSeparator).toRegex()
    val miraiTester = ".*/mirai-(.*)-([0-9._]+(|-[0-9A-Za-z_]+))\\.jar$".toRegex()
    override suspend fun invoke0(contact: Contact, sender: QQ, packet: ContactMessage, args: LinkedList<ArgumentToken>) {
        val builder = StringBuilder()
        builder.append("墨雨橙 Power❤by Mirai.\n")
        val path = manager.classPath
        val split = path.split(regex).map { it.replace('\\','/') }
        for(classpath in split){
            val result = miraiTester.matchEntire(classpath)?:continue
            builder.append("${result.groupValues[1]}: ${result.groupValues[2]}\n")
        }
        contact.sendMessage(builder.toString().toMessage())
    }
}