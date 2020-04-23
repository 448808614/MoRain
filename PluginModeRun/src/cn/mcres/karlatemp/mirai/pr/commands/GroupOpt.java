/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/15 09:31:26
 *
 * MiraiPlugins/PluginModeRun/GroupOpt.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import cn.mcres.karlatemp.mirai.pr.GroupSettings;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.ContactMessage;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class GroupOpt implements MCommand {
    @Override
    public String permission() {
        return "command.group-opt";
    }

    @Override
    public void invoke(@NotNull Contact contact, @NotNull User sender, @NotNull ContactMessage packet, @NotNull LinkedList<ArgumentToken> args) throws Exception {
        if (!(contact instanceof Group)) {
            contact.sendMessageAsync("只能在群里使用......");
            return;
        }
        if (args.isEmpty()) {
            contact.sendMessageAsync(""
                    + "/group-opt set [key] [value]\n"
                    + "/group-opt list\n"
                    + "/group-opt query [key]");
            return;
        }
        String opt = args.poll().getAsString();
        switch (opt) {
            case "query": {
                if (args.isEmpty()) return;
                String tok = args.poll().getAsString();
                GroupSettings settings = GroupSettings.getSettings(contact.getId());
                final var element = settings.data.get(tok);
                if (element == null) {
                    contact.sendMessageAsync("[" + tok + "]无值");
                } else {
                    contact.sendMessageAsync("[" + tok + "] = " + element.getAsString());
                }
                break;
            }
            case "list": {
                GroupSettings settings = GroupSettings.getSettings(contact.getId());
                final var keySet = settings.data.keySet();
                if (keySet.isEmpty()) {
                    contact.sendMessageAsync("没有任何配置");
                } else {
                    StringBuilder builder = new StringBuilder();
                    for (var key : keySet) {
                        builder.append(key).append(" = ").append(settings.data.get(key).getAsString()).append('\n');
                    }
                    contact.sendMessageAsync(builder.toString());
                }
                break;
            }
            case "set": {
                if (args.isEmpty()) return;
                String tok = args.poll().getAsString();
                GroupSettings settings = GroupSettings.getSettings(contact.getId());
                if (args.isEmpty()) {
                    settings.data.remove(tok);
                    contact.sendMessageAsync("成功重置[" + tok + "]");
                } else {
                    String val = args.poll().getAsString();
                    settings.data.addProperty(tok, val);
                    contact.sendMessageAsync("成功设置[" + tok + "] = " + val);
                }
            }
        }
    }
}
