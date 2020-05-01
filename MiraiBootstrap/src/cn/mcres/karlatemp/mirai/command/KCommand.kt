/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/20 18:52:23
 *
 * MiraiPlugins/PluginModeRun/KCommand.kt
 */

package cn.mcres.karlatemp.mirai.command

@Target(AnnotationTarget.CLASS)
annotation class KCommand(
        val name: String
)