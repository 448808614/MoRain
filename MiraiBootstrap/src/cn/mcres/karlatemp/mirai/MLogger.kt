/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/05/18 13:43:40
 *
 * MiraiPlugins/MiraiBootstrap/MLogger.kt
 */

package cn.mcres.karlatemp.mirai

import cn.mcres.karlatemp.mxlib.logging.ILogger
import java.io.PrintStream
import java.lang.management.ThreadInfo
import java.util.logging.Handler
import java.util.logging.LogRecord

class MLogger(private vararg val loggers: ILogger) : ILogger {
    override fun getStackTraceElementMessage(track: StackTraceElement?): String {
        return loggers[0].getStackTraceElementMessage(track)
    }

    override fun printThreadInfo(info: ThreadInfo, fullFrames: Boolean, emptyPrefix: Boolean): ILogger {
        loggers.forEach { it.printThreadInfo(info, fullFrames, emptyPrefix) }
        return this
    }

    override fun printStackTrace(thr: Throwable, printStacks: Boolean, isError: Boolean): ILogger {
        loggers.forEach { it.printStackTrace(thr, printStacks, isError) }
        return this
    }

    override fun publish(record: LogRecord?, handler: Handler?): ILogger {
        loggers.forEach { it.publish(record, handler) }
        return this
    }

    override fun error(line: String?): ILogger {
        loggers.forEach { it.error(line) }
        return this
    }

    override fun getPrintStream(): PrintStream {
        TODO("Not yet implemented")

    }

    override fun printf(line: String?): ILogger {
        loggers.forEach { it.printf(line) }
        return this
    }

    override fun getErrorStream(): PrintStream {
        TODO("Not yet implemented")
    }

}