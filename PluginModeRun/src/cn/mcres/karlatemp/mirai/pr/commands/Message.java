/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/04 02:53:22
 *
 * MiraiPlugins/PluginModeRun/Message.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import cn.mcres.karlatemp.mirai.permission.PermissionManager;
import cn.mcres.karlatemp.mirai.pr.listener.MemberJLListener;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.japt.Events;
import net.mamoe.mirai.message.*;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class Message implements MCommand {
    public static MessageChain wait(Contact contact, User sender) throws ExecutionException, InterruptedException {
        long current = System.currentTimeMillis();
        AtomicReference<Listener<?>> listener = new AtomicReference<>();
        CompletableFuture<MessageChain> chain = new CompletableFuture<>();
        boolean isGroup = contact instanceof Group;
        var cc = contact.getId();
        var si = sender.getId();
        listener.set(Events.subscribeAlways(MessageEvent.class, event -> {
            if (System.currentTimeMillis() - current > 60000) {
                listener.get().complete();
                chain.complete(null);
                return;
            }
            if (isGroup) {
                if (event instanceof GroupMessageEvent) {
                    var gm = (GroupMessageEvent) event;
                    if (gm.getGroup().getId() == cc) {
                        if (gm.getSender().getId() == si) {
                            chain.complete(event.getMessage());
                            listener.get().complete();
                        }
                    }
                }
            } else if (event instanceof FriendMessageEvent) {
                if (si == event.getSender().getId()) {
                    chain.complete(event.getMessage());
                    listener.get().complete();
                }
            }
        }));
        return chain.get();
    }

    @Override
    public String permission() {
        return "command.message";
    }


    @Override
    public void invoke(@NotNull Contact contact, @NotNull User sender, @NotNull MessageEvent packet, @NotNull LinkedList<ArgumentToken> args) throws ExecutionException, InterruptedException {
        if (args.isEmpty()) return;
        if (!(packet instanceof GroupMessageEvent)) {
            contact.sendMessageAsync("This command only use for group.");
            return;
        }
        GroupMessageEvent group = (GroupMessageEvent) packet;
        switch (args.poll().getAsString()) {
            case "remove": {
                if (args.isEmpty()) {
                    contact.sendMessageAsync("/message remove [join/leave]");
                    return;
                }
                String type = args.poll().getAsString();
                switch (type) {
                    case "join":
                    case "leave": {
                        MemberJLListener.remove(group.getGroup().getId(), type.equals("join"));
                        break;
                    }
                }
            }
            case "add": {
                if (args.isEmpty()) {
                    contact.sendMessageAsync("/message add [join/leave]");
                    return;
                }
                switch (args.peek().getAsString()) {
                    case "join":
                    case "leave": {
                        @SuppressWarnings("ConstantConditions")
                        boolean isJoin = args.poll().getAsString().equalsIgnoreCase("join");
                        boolean isScript = false;
                        if (!args.isEmpty()) {
                            if (args.poll().getAsString().equals("script")) {
                                if (!PermissionManager.PERMISSIBLE_THREAD_LOCAL.get().hasPermission("command.message.script")) {
                                    contact.sendMessageAsync("不可以!");
                                    return;
                                }
                                isScript = true;
                            }
                        }
                        contact.sendMessageAsync("请发送要发送的内容....");
                        MessageChain chain = wait(group.getGroup(), group.getSender());
                        if (chain == null) {
                            contact.sendMessageAsync("超时了...");
                        } else {
                            MemberJLListener.override(group.getGroup().getId(), isJoin, isScript, chain);
                        }
                    }
                    break;
                }
            }
        }
    }
}
