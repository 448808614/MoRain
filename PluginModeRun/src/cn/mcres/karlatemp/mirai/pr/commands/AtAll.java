/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/22 16:36:26
 *
 * MiraiPlugins/PluginModeRun/AtAll.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.ContactMessage;

import java.util.LinkedList;

public class AtAll implements MCommand {
    @Override
    public void invoke(Contact contact, QQ sender, ContactMessage packet, LinkedList<ArgumentToken> args) {
        contact.sendMessageAsync(net.mamoe.mirai.message.data.AtAll.INSTANCE.plus("Message testing"));
    }
}
