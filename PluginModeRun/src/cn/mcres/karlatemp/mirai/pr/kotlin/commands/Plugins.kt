/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/23 15:25:09
 *
 * MiraiPlugins/PluginModeRun/Plugins.kt
 */

package cn.mcres.karlatemp.mirai.pr.kotlin.commands

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken
import cn.mcres.karlatemp.mirai.command.KCommand
import cn.mcres.karlatemp.mirai.command.KotlinCommand
import cn.mcres.karlatemp.mirai.plugin.PluginLoaderManager
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.toMessage
import java.util.*

@KCommand("plugins")
object Plugins : KotlinCommand() {
    override suspend fun invoke0(contact: Contact, sender: User, packet: MessageEvent, args: LinkedList<ArgumentToken>) {
        val plugins = PluginLoaderManager.plugins0!!
        val builder = StringBuilder()
        builder.append("已加载 ${plugins.size} 个插件")
        plugins.forEach { plugin ->
            builder.append("  ${plugin.name} v${plugin.version} - ${plugin.description}")
        }
        contact.sendMessage(builder.toString().toMessage())
    }
}