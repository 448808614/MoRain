/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/24 22:45:51
 *
 * MiraiPlugins/MiraiBootstrap/ConsolePacket.java
 */

package cn.mcres.karlatemp.mirai;

import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.message.ContactMessage;
import net.mamoe.mirai.message.MessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public final class ConsolePacket extends MessageEvent {
    public static final ConsolePacket INSTANCE = new ConsolePacket();
    private static final User console_qq = new User() {
        @Override
        public long getId() {
            return 10086;
        }

        @NotNull
        @Override
        public String getNick() {
            return "Console";
        }

        @NotNull
        @Override
        public Bot getBot() {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public String toString() {
            return "ConsoleUser";
        }

        @NotNull
        @Override
        public CoroutineContext getCoroutineContext() {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public Future<MessageReceipt<Contact>> sendMessageAsync(@NotNull String message) {
            return Console.INSTANCE.sendMessageAsync(message);
        }

        @NotNull
        @Override
        public Future<MessageReceipt<Contact>> sendMessageAsync(@NotNull Message message) {
            return Console.INSTANCE.sendMessageAsync(message);
        }

        @NotNull
        @Override
        public MessageReceipt<Contact> sendMessage(@NotNull String message) {
            return Console.INSTANCE.sendMessage(message);
        }

        @NotNull
        @Override
        public MessageReceipt<Contact> sendMessage(@NotNull Message message) throws EventCancelledException, IllegalStateException {
            return Console.INSTANCE.sendMessage(message);
        }
    };

    @NotNull
    @Override
    public Bot getBot() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public MessageChain getMessage() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public User getSender() {
        return console_qq;
    }

    @NotNull
    @Override
    public Contact getSubject() {
        return Console.getInstance();
    }

    @NotNull
    @Override
    public String getSenderName() {
        return "Console";
    }

    @Override
    public int getTime() {
        return 0;
    }
}
