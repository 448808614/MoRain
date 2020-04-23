/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/17 21:46:56
 *
 * MiraiPlugins/MiraiPlugins/MCommand.java
 */

package cn.mcres.karlatemp.mirai.command;

import cn.mcres.karlatemp.mirai.AsyncExec;
import cn.mcres.karlatemp.mirai.Bootstrap;
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.permission.Permissible;
import cn.mcres.karlatemp.mirai.permission.PermissionManager;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.ContactMessage;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public interface MCommand {
    default String permission() {
        return null;
    }

    void invoke(
            @NotNull Contact contact,
            @NotNull User sender,
            @NotNull ContactMessage packet,
            @NotNull LinkedList<ArgumentToken> args) throws Exception;

    default void $$$$invoke$$(
            @NotNull Contact contact,
            @NotNull User sender,
            @NotNull ContactMessage packet,
            @NotNull LinkedList<ArgumentToken> args,
            String key) throws Exception {
        Permissible p = PermissionManager.PERMISSIBLE_THREAD_LOCAL.get();
        AsyncExec.service.execute(() -> Bootstrap.runCatching(key, contact, () -> {
            PermissionManager.PERMISSIBLE_THREAD_LOCAL.set(p);
            invoke(contact, sender, packet, args);
            return null;
        }));
    }
}
