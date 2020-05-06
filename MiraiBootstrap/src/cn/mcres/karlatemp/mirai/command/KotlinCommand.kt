/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/20 20:16:41
 *
 * MiraiPlugins/MiraiBootstrap/KotlinCommand.kt
 */

package cn.mcres.karlatemp.mirai.command

import cn.mcres.karlatemp.mirai.AsyncExecKt
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken
import cn.mcres.karlatemp.mirai.permissible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.MessageEvent
import java.util.*

abstract class KotlinCommand : MCommand, CoroutineScope by AsyncExecKt.newScope {

    final override fun invoke(contact: Contact, sender: User, packet: MessageEvent, args: LinkedList<ArgumentToken>) = throw IllegalAccessError()

    final override fun `$$$$invoke$$`(contact: Contact, sender: User, packet: MessageEvent, args: LinkedList<ArgumentToken>, key: String) {
        val permissible = permissible()
        contact.launch {
            permissible(permissible)
            invoke0(contact, sender, packet, args)
        }
    }

    abstract suspend fun invoke0(contact: Contact, sender: User, packet: MessageEvent, args: LinkedList<ArgumentToken>);
}