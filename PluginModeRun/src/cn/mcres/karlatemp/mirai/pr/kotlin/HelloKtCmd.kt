/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/20 18:51:25
 *
 * MiraiPlugins/PluginModeRun/HelloKtCmd.kt
 */

package cn.mcres.karlatemp.mirai.pr.kotlin

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken
import cn.mcres.karlatemp.mirai.command.KCommand
import cn.mcres.karlatemp.mirai.command.KotlinCommand
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.PlainText
import java.util.*

@KCommand("hkt")
object HelloKtCmd : KotlinCommand() {
    override suspend fun invoke0(contact: Contact, sender: QQ, packet: ContactMessage, args: LinkedList<ArgumentToken>) {
        contact.sendMessage(PlainText("KKSK"))
    }
}