/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/20 19:35:53
 *
 * MiraiPlugins/MiraiBootstrap/Bootstrap.kt
 */

package cn.mcres.karlatemp.mirai

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.Message
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface EventPoster {
    suspend fun post(msg: ContactMessage)
}

object BotSuspendWrap {
    @JvmStatic
    fun sendMessage(contact: Contact, msg: Message) {
        contact.launch { contact.sendMessage(msg) }
    }
}

fun initialize(bot: Bot, poster: EventPoster) {
    bot.subscribeAlways<FriendMessage> {
        poster.post(this)
    }
    bot.subscribeAlways<GroupMessage> {
        poster.post(this)
    }
}

object AsyncExecKt {

    @Suppress("MemberVisibilityCanBePrivate")
    val dispatcher = AsyncExec.service.asCoroutineDispatcher()

    val newScope: CoroutineScope
        get() = CoroutineScope(dispatcher + EmptyCoroutineContext)
}