/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/24 23:15:06
 *
 * MiraiPlugins/PluginModeRun/TestPerm.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import cn.mcres.karlatemp.mirai.permission.Permissible;
import cn.mcres.karlatemp.mirai.permission.PermissibleLink;
import cn.mcres.karlatemp.mirai.permission.PermissionManager;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.ContactMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;

public class TestPerm implements MCommand {

    @Override
    public void invoke(@NotNull Contact contact, @NotNull User sender, @NotNull ContactMessage packet, @NotNull LinkedList<ArgumentToken> args) {
        StringBuilder sb = new StringBuilder();
        final Permissible permissible = PermissionManager.PERMISSIBLE_THREAD_LOCAL.get();
        if (permissible instanceof PermissibleLink) {
            final Collection<Permissible> ps = ((PermissibleLink) permissible).getPs();
            for (Permissible p : ps) {
                sb.append(" > ").append(p.toString());
            }
            contact.sendMessageAsync(sb.toString());
            sb.setLength(0);
        } else {
            contact.sendMessageAsync(permissible.toString() + " > " + permissible.getClass());
            Thread.dumpStack();
        }
        for (ArgumentToken tok : args) {
            sb.append("> ").append(tok.getAsString()).append(" > ").append(permissible.hasPermission(tok.getAsString())).append('\n');
        }
        contact.sendMessageAsync(sb.toString());
    }
}
