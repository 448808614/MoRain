/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/20 19:35:53
 *
 * MiraiPlugins/MiraiBootstrap/Bootstrap.kt
 */

package cn.mcres.karlatemp.mirai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.Message
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object BotSuspendWrap {
    @JvmStatic
    fun sendMessage(contact: Contact, msg: Message) {
        contact.launch { contact.sendMessage(msg) }
    }
}

fun initialize(bot: Bot) {
    bot.subscribeAlways<MessageEvent>(
            priority = Listener.EventPriority.MONITOR,
            concurrency = Listener.ConcurrencyKind.CONCURRENT,
            coroutineContext = AsyncExecKt.dispatcher
    ) {
        Bootstrap.accept(this)
    }
}

object AsyncExecKt {
    private val allContexts = ConcurrentLinkedDeque<CoroutineContext>()
    private val allScopes = ConcurrentLinkedDeque<CoroutineScope>()
    fun stop() {
        kotlin.runCatching {
            dispatcher.cancel()
        }
        allScopes.removeIf {
            kotlin.runCatching {
                it.cancel()
            }
            true
        }
        allContexts.removeIf {
            kotlin.runCatching {
                it.cancel()
            }
            true
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val dispatcher = AsyncExec.service.asCoroutineDispatcher()
    val newContext: CoroutineContext get() = (dispatcher + EmptyCoroutineContext).also { allContexts.add(it) }
    val newScope: CoroutineScope
        get() = CoroutineScope(dispatcher).also { allScopes.add(it) }
}