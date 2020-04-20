/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/22 11:48:04
 *
 * MiraiPlugins/PluginModeRun/Raw.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.ContactMessage;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;

public class Raw implements MCommand {
    @Override
    public void invoke(@NotNull Contact contact, @NotNull QQ sender, @NotNull ContactMessage packet, @NotNull LinkedList<ArgumentToken> args) {
        MessageChainBuilder builder = new MessageChainBuilder();
        for (Message m : packet.getMessage()) {
            if (m instanceof PlainText) {
                builder.add("plain{" + ((PlainText) m).getStringValue() + "}");
            } else if (m instanceof At) {
                builder.add("at{qq=" + ((At) m).getTarget() + ", display=" + ((At) m).getDisplay() + ", encoded=" + Base64.getEncoder().encodeToString(((At) m).getDisplay().getBytes(StandardCharsets.UTF_8)) + "}");
            }
        }
        contact.sendMessageAsync(builder.asMessageChain());
    }
}
