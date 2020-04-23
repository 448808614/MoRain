/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/25 23:20:08
 *
 * MiraiPlugins/PluginModeRun/Logging.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.ContactMessage;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class Logging implements MCommand {
    @Override
    public String permission() {
        return "command.logging";
    }

    @Override
    public void invoke(@NotNull Contact contact, @NotNull User sender, @NotNull ContactMessage packet, @NotNull LinkedList<ArgumentToken> args) {
        if (args.isEmpty()) {
            cn.mcres.karlatemp.mirai.Logging.openFileLogging ^= true;
        } else {
            cn.mcres.karlatemp.mirai.Logging.openFileLogging = Boolean.parseBoolean(args.poll().getAsString());
        }
        contact.sendMessageAsync("Enable File mode: " + cn.mcres.karlatemp.mirai.Logging.openFileLogging);
    }
}
