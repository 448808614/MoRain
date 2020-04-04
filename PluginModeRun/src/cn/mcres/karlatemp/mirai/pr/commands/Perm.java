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
import cn.mcres.karlatemp.mirai.permission.PermissionAttach;
import cn.mcres.karlatemp.mirai.permission.PermissionBase;
import cn.mcres.karlatemp.mirai.permission.PermissionManager;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.ContactMessage;

import java.util.LinkedList;
import java.util.Objects;

public class Perm implements MCommand {
    @Override
    public String permission() {
        return "command.perm";
    }

    @Override
    public void invoke(Contact contact, QQ sender, ContactMessage packet, LinkedList<ArgumentToken> args) {
        if (args.isEmpty()) {
            return;
        }
        try {
            switch (args.poll().getAsString()) {
                case "u": {
                    if (args.isEmpty()) {
                        contact.sendMessageAsync("" +
                                "Permission Manager>\n" +
                                "/perm u [add/del] [qq] ...");
                        break;
                    }
                    String v = args.poll().getAsString();
                    switch (v) {
                        case "del":
                        case "add": {
                            boolean status = v.equals("add");
                            if (args.isEmpty()) return;
                            long qq = args.poll().getAsLong();
                            PermissionBase pb = PermissionManager.allocateUserV(qq);
                            final PermissionAttach attach = pb.registerAttach();
                            boolean a = true;
                            while (!args.isEmpty()) {
                                String perm = args.poll().getAsString();
                                attach.setPermission(perm, status);
                                a = false;
                            }
                            pb.recalculatePermissions();
                            if (a) {
                                contact.sendMessageAsync("没做出任何处理.....");
                            } else {
                                contact.sendMessageAsync(status ? "已为" + qq + "添加权限" : "已经删除" + qq + "的权限");
                            }
                            break;
                        }
                    }
                    break;
                }
                case "g": {
                    if (args.isEmpty()) {
                        contact.sendMessageAsync("" +
                                "Permission Manager>\n" +
                                "/perm g [add/del] [name] ...");
                        break;
                    }
                    String v = args.poll().getAsString();
                    switch (v) {
                        case "del":
                        case "add": {
                            boolean status = v.equals("add");
                            if (args.isEmpty()) return;
                            String name = args.poll().getAsString();
                            PermissionBase pb = PermissionManager.allocateGroupV(name);
                            final PermissionAttach attach = pb.registerAttach();
                            boolean a = true;
                            while (!args.isEmpty()) {
                                String perm = args.poll().getAsString();
                                attach.setPermission(perm, status);
                                a = false;
                            }
                            pb.recalculatePermissions();
                            if (a) {
                                contact.sendMessageAsync("没做出任何处理.....");
                            } else {
                                contact.sendMessageAsync(status ? "已为" + name + "添加权限" : "已经删除" + name + "的权限");
                            }
                            break;
                        }
                    }
                    break;
                }
                case "l": {
                    if (args.size() < 2) return;
                    switch (args.poll().getAsString()) {
                        case "u": {
                            long qq = Objects.requireNonNull(args.poll()).getAsLong();
                            if (!args.isEmpty()) {
                                PermissionManager.allocateUserV(qq).setName(Objects.requireNonNull(args.poll()).getAsString());
                            } else {
                                final PermissionBase base = PermissionManager.users.get(qq);
                                String group = base == null ? "default" : base.getName() == null ? "default" : base.getName();
                                contact.sendMessageAsync(qq + " = " + group);
                            }
                            break;
                        }
                        case "g": {
                            String name = Objects.requireNonNull(args.poll()).getAsString();
                            if (!args.isEmpty()) {
                                String next = Objects.requireNonNull(args.poll()).getAsString();
                                if (next.equals("<none>")) next = null;
                                PermissionBase group = (PermissionBase) PermissionManager.allocateGroupV(name)
                                        .setName(next);
                                if (next != null) {
                                    final PermissionBase base = PermissionManager.groups.get(next);
                                    if (base != group) group.setParent(base);
                                }
                            } else {
                                final PermissionBase base = PermissionManager.groups.get(name);
                                String group = base == null ? "default" : base.getName() == null ? "default" : base.getName();
                                contact.sendMessageAsync(name + " = " + group);
                            }
                            break;
                        }
                    }
                    break;
                }
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
