/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/18 12:24:53
 *
 * MiraiPlugins/MiraiPlugins/Console.java
 */

package cn.mcres.karlatemp.mirai;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import org.apache.hc.core5.concurrent.CompletedFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Future;

public abstract class Console extends Contact {
    static Console INSTANCE;

    public static Console getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public Bot getBot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getId() {
        return 0;
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
        return "ConsoleContact";
    }

    @NotNull
    @Override
    public CoroutineContext getCoroutineContext() {
        throw new UnsupportedOperationException();
    }

    protected abstract void write(String message);

    @NotNull
    @Override
    public MessageReceipt<Contact> sendMessage(@NotNull String message) throws EventCancelledException, IllegalStateException {
        MessageReceipt<Contact> receipt = new MessageReceipt<>(new MessageSource() {
            @Override
            public int length() {
                return 0;
            }

            @Override
            public char charAt(int index) {
                return 0;
            }

            @NotNull
            @Override
            public CharSequence subSequence(int i, int i1) {
                throw new UnsupportedOperationException();
            }

            @Override
            public char get(int i) {
                return 0;
            }

            @Override
            public int compareTo(@NotNull String s) {
                return 0;
            }

            @Override
            public int getLength() {
                return 0;
            }

            @Override
            public long getId() {
                return 0;
            }

            @Nullable
            @Override
            public Object ensureSequenceIdAvailable(@NotNull Continuation<? super Unit> continuation) {
                return null;
            }

            @Override
            public long getTime() {
                return 0;
            }

            @Override
            public long getSenderId() {
                return 0;
            }

            @Override
            public long getToUin() {
                return 0;
            }

            @Override
            public long getGroupId() {
                return 0;
            }

            @NotNull
            @Override
            public MessageChain getOriginalMessage() {
                return MessageUtils.newChain(message);
            }

            @Override
            public boolean eq(@NotNull Message message) {
                return false;
            }

            @Override
            public boolean eq(@NotNull String s) {
                return false;
            }

            @Override
            public boolean contains(@NotNull String s) {
                return false;
            }

            @NotNull
            @Override
            public CombinedMessage plus(@NotNull Message message) {
                throw new UnsupportedOperationException();
            }

            @NotNull
            @Override
            public CombinedMessage plus(@NotNull String s) {
                throw new UnsupportedOperationException();
            }

            @NotNull
            @Override
            public CombinedMessage plus(@NotNull SingleMessage singleMessage) {
                throw new UnsupportedOperationException();
            }

            @NotNull
            @Override
            public CombinedMessage plus(@NotNull CharSequence charSequence) {
                throw new UnsupportedOperationException();
            }
        }, ConsolePacket.INSTANCE.getSender(), null);
        write(message);
        return receipt;
    }

    @NotNull
    @Override
    public Future<MessageReceipt<? extends Contact>> sendMessageAsync(@NotNull String message) {
        return new CompletedFuture<>(sendMessage(message));
    }

    @NotNull
    @Override
    public Future<MessageReceipt<? extends Contact>> sendMessageAsync(@NotNull Message message) {
        return new CompletedFuture<>(sendMessage(message));
    }

    @NotNull
    @Override
    public MessageReceipt<Contact> sendMessage(@NotNull Message message) throws EventCancelledException, IllegalStateException {
        return sendMessage(message.toString());
    }
}
