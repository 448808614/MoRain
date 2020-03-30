/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/24 22:45:51
 *
 * MiraiPlugins/MiraiBootstrap/ConsolePacket.java
 */

package cn.mcres.karlatemp.mirai;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.data.FriendNameRemark;
import net.mamoe.mirai.data.PreviousNameList;
import net.mamoe.mirai.data.Profile;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.message.MessagePacket;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Future;

public final class ConsolePacket extends MessagePacket<QQ, Contact> {
    public static final ConsolePacket INSTANCE = new ConsolePacket();
    private static final QQ console_qq = new QQ() {
        @Override
        public long getId() {
            return 10086;
        }

        @NotNull
        @Override
        public String getNick() {
            return "Console";
        }

        @Nullable
        @Override
        public Object queryProfile(@NotNull Continuation<? super Profile> continuation) {
            return null;
        }

        @Nullable
        @Override
        public Object queryPreviousNameList(@NotNull Continuation<? super PreviousNameList> continuation) {
            return null;
        }

        @Nullable
        @Override
        public Object queryRemark(@NotNull Continuation<? super FriendNameRemark> continuation) {
            return null;
        }

        @NotNull
        @Override
        public Bot getBot() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            return o == this;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @NotNull
        @Override
        public String toString() {
            return "ConsoleQQ";
        }

        @NotNull
        @Override
        public CoroutineContext getCoroutineContext() {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public Future<MessageReceipt<? extends Contact>> sendMessageAsync(@NotNull String message) {
            return Console.INSTANCE.sendMessageAsync(message);
        }

        @NotNull
        @Override
        public Future<MessageReceipt<? extends Contact>> sendMessageAsync(@NotNull Message message) {
            return Console.INSTANCE.sendMessageAsync(message);
        }

        @NotNull
        @Override
        public MessageReceipt<? extends Contact> sendMessage(@NotNull String message) {
            return Console.INSTANCE.sendMessage(message);
        }

        @NotNull
        @Override
        public MessageReceipt<? extends Contact> sendMessage(@NotNull Message message) throws EventCancelledException, IllegalStateException {
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
    public QQ getSender() {
        return console_qq;
    }

    @NotNull
    @Override
    public Contact getSubject() {
        return Console.getInstance();
    }
}
