/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/20 18:50:03
 *
 * MiraiPlugins/PluginModeRun/KotlinInitializer.kt
 */

package cn.mcres.karlatemp.mirai.pr

import cn.mcres.karlatemp.mirai.*
import cn.mcres.karlatemp.mirai.command.MCommand
import cn.mcres.karlatemp.mirai.command.KCommand
import java.lang.reflect.Modifier


fun initialize() {
    jar.forEach { entry ->
        if (entry.isClass) {
            val klass = entry.loadClass()
            klass.extends(MCommand::class.java) {
                val kc = getAnnotation(KCommand::class.java)
                if (kc != null) {
                    CommandMgr.register(kc.name, instance)
                }
            }
            klass.extends(AutoInitializer::class.java) {
                if (!isAnnotation && !isEnum && !isInterface && !Modifier.isAbstract(modifiers)) {
                    instance.initialize()
                }
            }
        }
    }
}