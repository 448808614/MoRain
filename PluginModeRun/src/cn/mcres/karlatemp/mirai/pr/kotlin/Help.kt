/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/05/31 24:22:36
 *
 * MiraiPlugins/PluginModeRun/Help.kt
 */

package cn.mcres.karlatemp.mirai.pr.kotlin

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken
import cn.mcres.karlatemp.mirai.command.KCommand
import cn.mcres.karlatemp.mirai.command.KotlinCommand
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageEvent
import java.util.*

@KCommand("help")
object Help : KotlinCommand() {
    override suspend fun invoke0(contact: Contact, sender: User, packet: MessageEvent, args: LinkedList<ArgumentToken>) {
        contact.sendMessage("爬爬爬哪有什么帮助")
    }
}
