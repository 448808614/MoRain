/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/21 19:38:37
 *
 * MiraiPlugins/MiraiBootstrap/ConsoleImpl.kt
 */

package cn.mcres.karlatemp.mirai

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineImage
import net.mamoe.mirai.utils.ExternalImage

object ConsoleImpl : Console() {
    override suspend fun uploadImage(image: ExternalImage): OfflineImage {
        throw IllegalStateException()
    }

    override fun write(message: String?) {
        println(message)
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Contact> {
        println(message.contentToString())
        return MessageReceipt(source, ConsolePacket.INSTANCE.sender, null)
    }
}