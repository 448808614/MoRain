/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/05/14 12:20:21
 *
 * MiraiPlugins/MiraiBootstrap/Ignored.kt
 */

package cn.mcres.karlatemp.mirai

import com.google.gson.JsonParser
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.logging.Level

object IgnoredKt {
    @JvmStatic
    val ignoredGroups = HashSet<Long>()

    @JvmStatic
    fun reload() {
        ignoredGroups.clear()
        File("data/ignored-${LoginData.getLoginQQ()}.json").apply {
            if (isFile) {
                kotlin.runCatching {
                    InputStreamReader(FileInputStream(this), Charsets.UTF_8).use {
                        JsonParser.parseReader(it)
                    }.asJsonArray.forEach {
                        ignoredGroups.add(it.asLong)
                    }
                }.onFailure {
                    "IgnoredMap".logger().log(Level.WARNING, "Failed to load ignored list", it)
                }
            }
        }
    }
}