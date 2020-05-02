/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/05/02 11:23:26
 *
 * MiraiPlugins/PluginModeRun/Rank.kt
 */

package cn.mcres.karlatemp.mirai.pr.kotlin.commands

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken
import cn.mcres.karlatemp.mirai.command.KCAlias
import cn.mcres.karlatemp.mirai.command.KCommand
import cn.mcres.karlatemp.mirai.command.KotlinCommand
import cn.mcres.karlatemp.mirai.event.MessageSendEvent
import cn.mcres.karlatemp.mirai.export
import cn.mcres.karlatemp.mirai.logger
import cn.mcres.karlatemp.mirai.on
import cn.mcres.karlatemp.mirai.pr.AutoInitializer
import cn.mcres.karlatemp.mirai.pr.CoreDisableEvent
import cn.mcres.karlatemp.mirai.sendTo
import cn.mcres.karlatemp.mxlib.tools.Unsafe
import cn.mcres.karlatemp.mxlib.util.RAFInputStream
import cn.mcres.karlatemp.mxlib.util.RAFOutputStream
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.toMessage
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.RandomAccessFile
import java.io.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalField
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors

@KCommand("rank")
@KCAlias("统计")
object Rank : KotlinCommand(), AutoInitializer {
    val lastEditTime = AtomicReference<ZonedDateTime>()

    class RankData : Serializable {
        companion object {
            @JvmStatic
            private val serialVersionUID: Long = 0xA147888947147
            private val offset: Long
            private val unsafe = Unsafe.getUnsafe()

            init {
                offset = RankData::class.java.getDeclaredField("ranks").let {
                    unsafe.objectFieldOffset(it)
                }
            }
        }

        private fun writeObject(writer: ObjectOutputStream) {
            writer.writeObject(ranks)
        }

        private fun readObject(reader: ObjectInputStream) {
            @Suppress("UNCHECKED_CAST")
            unsafe.putReference(this, offset, reader.readObject() as ConcurrentHashMap<out Long, AtomicInteger>)
        }

        val ranks = ConcurrentHashMap<Long, AtomicInteger>()
        fun getRank(sender: Long): AtomicInteger = ranks.computeIfAbsent(sender) { AtomicInteger() }
    }

    tailrec fun getGroupRank(group: Long): RankData {
        val last = lastEditTime.get()
        val now = Instant.now().atZone(ZoneId.systemDefault())
        if (last == null) {
            lastEditTime.compareAndSet(null, now)
            return getGroupRank(group)
        } else if (last.hour > now.hour) {
            if (lastEditTime.compareAndSet(last, now)) {
                data.clear()
            }
            return getGroupRank(group)
        }
        return data.computeIfAbsent(group) { RankData() }
    }

    val data = ConcurrentHashMap<Long, RankData>()
    val dataFile = File("data/rank.bin").also {
        with(it.parentFile ?: return@also) {
            if (!exists()) mkdirs()
        }
    }

    init {
        kotlin.runCatching {
            dataFile.takeIf { it.isFile }?.apply {
                ObjectInputStream(RAFInputStream(RandomAccessFile(this, "rw"))).use {
                    @Suppress("UNCHECKED_CAST")
                    data.putAll(it.readObject() as Map<out Long, RankData>)
                    lastEditTime.set(it.readObject() as? ZonedDateTime)
                }
            }
        }.onFailure {
            "Rank".logger().export(it, "Failed to load data from disk")
        }
        on<CoreDisableEvent> {
            kotlin.runCatching {
                @Suppress("ComplexRedundantLet")
                dataFile.also { dataFile.createNewFile() }.let { RandomAccessFile(it, "rw") }
                        .let { ObjectOutputStream(RAFOutputStream(it)) }.use {
                            it.writeObject(data)
                            it.writeObject(lastEditTime.get())
                        }
            }.onFailure {
                "Rank".logger().export(it, "Error in saving datas.")
            }
        }
    }

    override fun initialize() {
        on<MessageSendEvent> {
            with(event) {
                val subject = subject
                if (subject is Group) {
                    getGroupRank(subject.id).getRank(sender.id).getAndIncrement()
                }
            }
        }
    }

    override suspend fun invoke0(contact: Contact, sender: User, packet: ContactMessage, args: LinkedList<ArgumentToken>) {
        if (contact !is Group) {
            "Only group can use this command".toMessage() sendTo contact
            return
        }
        val rank = data[contact.id] ?: run {
            "No data in this group.".toMessage() sendTo contact
            return@invoke0
        }
        val members = contact.members
        val data = rank.ranks.entries.stream()
                .filter { members.getOrNull(it.key) != null }
                .sorted(Comparator.comparingInt {
                    it.value.get()
                }).limit(10).collect(Collectors.toCollection { LinkedList<Map.Entry<Long, AtomicInteger>>() })
        if (data.isEmpty()) {
            "No data in this group.".toMessage() sendTo contact
            return
        }
        val builder = StringBuilder()
        data.forEachIndexed { index, entry ->
            if (index != 0) builder.append('\n')
            builder.append("[ ").append(index + 1).append(" ] ").append(members[entry.key].nameCardOrNick).append(" : ").append(entry.value)
        }
        builder.toString().toMessage() sendTo contact
    }
}

