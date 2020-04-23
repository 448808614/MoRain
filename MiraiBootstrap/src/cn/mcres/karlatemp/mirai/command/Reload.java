/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/21 15:52:58
 *
 * MiraiPlugins/MiraiBootstrap/Reload.java
 */

package cn.mcres.karlatemp.mirai.command;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.plugin.PluginManager;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.ContactMessage;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class Reload implements MCommand {
    @Override
    public String permission() {
        return "command.reload";
    }

    @Override
    public void invoke(@NotNull Contact contact, @NotNull User sender, @NotNull ContactMessage packet, @NotNull LinkedList<ArgumentToken> args) {
        PluginManager.reload();
    }
}
