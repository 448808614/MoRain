/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/05 03:57:59
 *
 * MiraiPlugins/PluginModeRun/WK.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import cn.mcres.karlatemp.mirai.pr.WordKey;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class WK implements MCommand {
    @Override
    public String permission() {
        return "command.word-key";
    }

    /*
    list [?]
    rule [wk] [eq/contains] [add/del] ....
    remove [wk]
    register [global/group] [wk]
     */
    @Override
    public void invoke(@NotNull Contact contact, @NotNull User sender, @NotNull MessageEvent packet, @NotNull LinkedList<ArgumentToken> args) throws Exception {
        if (args.isEmpty()) return;
        var s1 = args.poll().getAsString();
        if (args.isEmpty() && !s1.equals("list")) return;
        String s2;
        {
            final ArgumentToken poll = args.poll();
            s2 = poll == null ? null : poll.getAsString();
        }
        if (s1.equals("list")) {
            var builder = new StringBuilder();
            for (var visit : WordKey.allWords.values()) {
                if (visit.group == 0) {
                    builder.append("Global: ").append(visit.uniqueId).append(", C=").append(visit.contains).append(", E=").append(visit.eq).append('\n');
                }
            }
            if (contact instanceof Group) {
                var gi = contact.getId();
                for (var visit : WordKey.allWords.values()) {
                    if (visit.group == gi) {
                        builder.append("Group: ").append(visit.uniqueId).append(", C=").append(visit.contains).append(", E=").append(visit.eq).append('\n');
                    }
                }
            }
            contact.sendMessageAsync(builder.toString());
        }
        assert s2 != null;
        switch (s1) {
            case "rule": {
                if (args.isEmpty()) break;
                var s3 = args.poll().getAsString();
                if (args.isEmpty()) break;
                var mode = args.poll().getAsString().endsWith("add");
                if (args.isEmpty()) break;
                final WordKey key = WordKey.allocateV(s2);
                var collect = s3.equalsIgnoreCase("eq") ? key.eq : key.contains;
                var list = new LinkedList<String>();
                while (!args.isEmpty()) {
                    var i = args.poll().getAsString();
                    if (mode)
                        collect.add(i);
                    else
                        collect.remove(i);
                    list.add(i);
                }
                contact.sendMessageAsync("IsEq: " + (collect == key.eq) + ", list=" + list + ", isAdd=" + mode + ", id=" + key.uniqueId);
                key.save();
                break;
            }
            case "remove": {
                WordKey.allocateV(s2).remove();
                break;
            }
            case "register": {
                if (args.isEmpty()) return;
                var s3 = args.poll().getAsString();
                WordKey wk;
                switch (s2) {
                    case "global": {
                        wk = WordKey.allocate(s3);
                        break;
                    }
                    case "group": {
                        if (!(packet instanceof GroupMessageEvent)) {
                            contact.sendMessageAsync("Only group can use group word key");
                            return;
                        }
                        wk = WordKey.allocate(s3);
                        wk.group = packet.getSubject().getId();
                        break;
                    }
                    default:
                        return;
                }
                var chain = Message.wait(contact, sender);
                if (chain == null) {
                    contact.sendMessageAsync("Timed out.... cancel");
                    break;
                }
                wk.messages.override(chain);
                wk.save();
                contact.sendMessageAsync("Success to register new keyword[" + wk.uniqueId + "]");
                break;
            }
        }
    }
}
