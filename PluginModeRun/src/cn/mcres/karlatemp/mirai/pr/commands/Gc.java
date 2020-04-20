/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/15 02:09:24
 *
 * MiraiPlugins/PluginModeRun/Gc.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.ContactMessage;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class Gc implements MCommand {
    @Override
    public String permission() {
        return "command.gc";
    }

    @Override
    public void invoke(@NotNull Contact contact, @NotNull QQ sender, @NotNull ContactMessage packet, @NotNull LinkedList<ArgumentToken> args) throws Exception {
        System.gc();
    }
}
