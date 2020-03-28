/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/17 21:46:56
 *
 * MiraiPlugins/MiraiPlugins/MCommand.java
 */

package cn.mcres.karlatemp.mirai.command;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.MessagePacket;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public interface MCommand {
    default String permission() {
        return null;
    }

    void invoke(Contact contact, QQ sender, MessagePacket<?, ?> packet, LinkedList<ArgumentToken> args);
}
