/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/20 20:16:41
 *
 * MiraiPlugins/MiraiBootstrap/KotlinCommand.kt
 */

package cn.mcres.karlatemp.mirai.command

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.ContactMessage
import java.util.*
import kotlin.coroutines.Continuation

abstract class KotlinCommand : MCommand {
    open class Invoker {
        open fun invoke(kc: KotlinCommand,
                        contact: Contact,
                        sender: QQ,
                        packet: ContactMessage,
                        args: LinkedList<ArgumentToken>,
                        continuation: Continuation<Unit>): Unit = throw IllegalAccessError()

        companion object {
            lateinit var implements: Invoker
        }
    }

    final override fun invoke(contact: Contact, sender: QQ, packet: ContactMessage, args: LinkedList<ArgumentToken>) = throw IllegalAccessError()

    final override fun `$$$$invoke$$`(contact: Contact, sender: QQ, packet: ContactMessage, args: LinkedList<ArgumentToken>, key: String) {
        contact.launch {
            invoke0(contact, sender, packet, args)
        }
    }

    abstract suspend fun invoke0(contact: Contact, sender: QQ, packet: ContactMessage, args: LinkedList<ArgumentToken>);
}