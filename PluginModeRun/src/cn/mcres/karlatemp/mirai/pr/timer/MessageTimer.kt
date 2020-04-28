/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/22 15:07:40
 *
 * MiraiPlugins/PluginModeRun/MessageTimer.kt
 */

package cn.mcres.karlatemp.mirai.pr.timer

import cn.mcres.karlatemp.mirai.bot
import cn.mcres.karlatemp.mirai.logger
import cn.mcres.karlatemp.mirai.pr.AutoInitializer
import cn.mcres.karlatemp.mirai.pr.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.message.data.toMessage
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalUnit

object MessageTimer : AutoInitializer {
    object OffsetClock : Clock() {
        private val utc = systemUTC()
        override fun withZone(zone: ZoneId?): Clock {
            TODO("Not yet implemented")
        }

        override fun getZone(): ZoneId {
            return utc.zone
        }

        override fun instant(): Instant {
            return utc.instant().plusMillis(1000L)
        }
    }

    override fun initialize() {
        "Message Timer".logger().info("Initializing Message Timer")
        val zone = ZoneId.of("Asia/Shanghai")
        val hours = 1 * 60 * 60
        Main.getInstance().launch {
            "Message Timer".logger().info("Join Launch")
            suspend fun invokeDelay() {
                val now = Instant.now().atZone(zone)!!
                val timestamp = now.minute * 60 + now.second
                println("Delay ${hours - timestamp}")
                delay((hours - timestamp) * 1000L)
            }

            suspend fun next() {
                val now = Instant.now(OffsetClock).atZone(zone)!!
                val v: String? = when (now.hour) {
                    1 -> "1点了, 还不睡吗"
                    2 -> "两点了, 打算直接睡到中午吗?"
                    7 -> "早上好, 新的一天开始了"
                    12 -> "午好, 一半的时间已经过去了"
                    21 -> "9点了, 可爱的孩子们都该睡了"
                    23 -> "还不睡觉的话明天就没精神了哦~"
                    else -> null
                }
                if (v != null) {
                    kotlin.runCatching {
                        bot.getGroup(942025944L).sendMessage(v.toMessage())
                    }
                }
                invokeDelay()
                next()
            }
            invokeDelay()
            next()
        }
    }
}