/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/03 19:08:26
 *
 * MiraiPlugins/PluginModeRun/ImageC.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.Http;
import cn.mcres.karlatemp.mirai.arguments.ArgumentImageToken;
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import cn.mcres.karlatemp.mirai.pr.EvalContext;
import kotlinx.atomicfu.AtomicRef;
import kotlinx.coroutines.GlobalScope;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.internal.EventInternalJvmKt;
import net.mamoe.mirai.japt.Events;
import net.mamoe.mirai.japt.internal.EventsImplKt;
import net.mamoe.mirai.message.FriendMessage;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.MessagePacket;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.OfflineImage;
import net.mamoe.mirai.message.data.OnlineImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ImageC implements MCommand {
    public static BufferedImage read(Image i) throws IOException {
        if (i instanceof OnlineImage) {
            File f = Http.download("img/image-" + Base64.getEncoder().encodeToString(i.getImageId().getBytes(StandardCharsets.UTF_8)), ((OnlineImage) i).getOriginUrl());
            if (f == null) return null;
            return ImageIO.read(f);
        }
        return null;
    }

    public static void waitImage(LinkedList<ArgumentToken> tokens, MessagePacket<?, ?> packet, BiConsumer<BufferedImage, Throwable> image) {
        if (tokens != null) {
            if (!tokens.isEmpty()) {
                final ArgumentToken token = tokens.poll();
                if (token instanceof ArgumentImageToken) {
                    try {
                        image.accept(read(((ArgumentImageToken) token).image), null);
                    } catch (Throwable any) {
                        image.accept(null, any);
                    }
                    return;
                }
            }
        }
        AtomicReference<Listener<?>> listener = new AtomicReference<>();
        long current = System.currentTimeMillis();
        listener.set(EventInternalJvmKt._subscribeEventForJaptOnly(MessagePacket.class, GlobalScope.INSTANCE, c -> {
            if (System.currentTimeMillis() - current > 60000) {
                listener.get().complete();
                return;
            }
            if (packet instanceof GroupMessage) {
                if (c instanceof GroupMessage) {
                    if (c.getSubject().getId() != packet.getSubject().getId()) {
                        return;
                    }
                } else return;
            } else if (packet instanceof FriendMessage) {
                if (!(c instanceof FriendMessage)) {
                    return;
                }
            } else return;
            if (c.getSender().getId() == packet.getSender().getId()) {
                Image image0 = null;
                for (Message m : c.getMessage()) {
                    if (m instanceof Image) {
                        image0 = (Image) m;
                        break;
                    }
                }
                if (image0 != null) {
                    listener.get().complete();
                    try {
                        image.accept(read(image0), null);
                    } catch (Throwable any) {
                        image.accept(null, any);
                    }
                }
            }
        }));
        packet.getSubject().sendMessageAsync("请在1min内发送一张图片..");
    }

    public static BufferedImage toDark(BufferedImage image) {
        BufferedImage target = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        final Graphics2D graphics = target.createGraphics();
        graphics.drawImage(image, null, 0, 0);
        graphics.dispose();
        return target;
    }

    @Override
    public void invoke(Contact contact, QQ sender, MessagePacket<?, ?> packet, LinkedList<ArgumentToken> args) {
        if (args.isEmpty()) return;
        switch (args.poll().getAsString()) {
            case "gray": {
                waitImage(args, packet, (image, e) -> {
                    if (e != null) {
                        contact.sendMessageAsync(e.toString());
                        return;
                    }
                    if (image == null) {
                        contact.sendMessageAsync("无效图片.");
                        return;
                    }
                    contact.sendMessageAsync("正在处理图片.....");
                    image = toDark(image);
                    int counter = 3;
                    Image image0 = null;
                    do {
                        try {
                            image0 = contact.uploadImage(image);
                            break;
                        } catch (Exception ignore) {
                        }
                    } while (counter-- > 0);
                    if (image0 == null) {
                        contact.sendMessageAsync("上传图片失败");
                    } else {
                        contact.sendMessageAsync(image0);
                    }
                });
                break;
            }
        }
    }
}
