/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/05/02 12:10:21
 *
 * MiraiPlugins/PluginModeRun/CoreDisableEvent.kt
 */

package cn.mcres.karlatemp.mirai.pr

import cn.mcres.karlatemp.mxlib.event.Event
import cn.mcres.karlatemp.mxlib.event.HandlerList

object CoreDisableEvent : Event() {
    @Suppress("MemberVisibilityCanBePrivate")
    val handlers: HandlerList<CoreDisableEvent> = HandlerList()

    override fun getHandlerList(): HandlerList<CoreDisableEvent> {
        return handlers
    }
}