/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/24 22:56:19
 *
 * MiraiPlugins/PluginModeRun/Perm.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import cn.mcres.karlatemp.mirai.permission.PermissionManager;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.MessagePacket;

import java.util.LinkedList;

public class Perm implements MCommand {
    @Override
    public String permission() {
        return "command.perm";
    }

    @Override
    public void invoke(Contact contact, QQ sender, MessagePacket<?, ?> packet, LinkedList<ArgumentToken> args) {
        if (args.isEmpty()) {
            return;
        }
        try {
            switch (args.poll().getAsString()) {
                case "reload": {
                    PermissionManager.reload();
                    contact.sendMessageAsync("Reload done");
                    break;
                }
                case "save": {
                    PermissionManager.save();
                    contact.sendMessageAsync("Save done");
                    break;
                }
            }
        } catch (Throwable err) {
            contact.sendMessageAsync(err.toString());
        }
    }
}
